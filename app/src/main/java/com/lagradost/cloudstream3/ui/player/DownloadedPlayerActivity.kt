package com.lagradost.cloudstream3.ui.player

import android.content.Intent
import android.net.Uri
import java.io.File
import androidx.navigation.fragment.NavHostFragment
import com.lagradost.safefile.SafeFile
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavOptions
import com.lagradost.cloudstream3.ui.settings.Globals.EMULATOR
import com.lagradost.cloudstream3.ui.settings.Globals.TV
import com.lagradost.cloudstream3.ui.settings.Globals.isLayout
import com.lagradost.cloudstream3.APIHolder
import com.lagradost.cloudstream3.AnimeLoadResponse
import com.lagradost.cloudstream3.CommonActivity
import com.lagradost.cloudstream3.MainActivity
import com.lagradost.cloudstream3.MovieLoadResponse
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.TvSeriesLoadResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.mvvm.safe
import com.lagradost.cloudstream3.ui.APIRepository
import com.lagradost.cloudstream3.ui.player.OfflinePlaybackHelper.playLink
import com.lagradost.cloudstream3.ui.player.OfflinePlaybackHelper.playUri
import com.lagradost.cloudstream3.ui.result.ResultEpisode
import com.lagradost.cloudstream3.ui.result.VideoWatchState
import com.lagradost.cloudstream3.utils.BackPressedCallbackHelper.attachBackPressedCallback
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.Coroutines.main
import com.lagradost.cloudstream3.utils.UIHelper.enableEdgeToEdgeCompat
import com.lagradost.cloudstream3.utils.UIHelper.navigate
import com.lagradost.cloudstream3.utils.UIHelper.hideSystemUI
import com.lagradost.cloudstream3.utils.UIHelper.showSystemUI
import android.content.res.Configuration
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.movievortex.android.core.ServerManager
import com.lagradost.cloudstream3.ActorData
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.utils.downloader.DownloadQueueManager
import com.lagradost.cloudstream3.utils.downloader.DownloadObjects

class DownloadedPlayerActivity : AppCompatActivity() {
    companion object {
        const val TAG = "DownloadedPlayerActivity"
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        CommonActivity.dispatchKeyEvent(this, event) ?: super.dispatchKeyEvent(event)

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
        CommonActivity.onKeyDown(this, keyCode, event) ?: super.onKeyDown(keyCode, event)

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        CommonActivity.onUserLeaveHint(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (isSameIntent(intent)) return
        setIntent(intent)
        Log.i(TAG, "onNewIntent")
        handleIntent(intent)
    }

    private fun isSameIntent(newIntent: Intent): Boolean {
        if (newIntent.getBooleanExtra("IS_NOTIFICATION_CLICK", false)) return true
        val old = intent ?: return false
        val oldContent = old.getStringExtra("CONTENT_URL") ?: old.getStringExtra("URL")
        val newContent = newIntent.getStringExtra("CONTENT_URL") ?: newIntent.getStringExtra("URL")
        if (oldContent != null && oldContent == newContent) return true

        val oldUri = old.data ?: old.clipData?.getItemAt(0)?.uri
        val newUri = newIntent.data ?: newIntent.clipData?.getItemAt(0)?.uri
        if (oldUri != null && oldUri == newUri) return true

        val oldText = safe { old.getStringExtra(Intent.EXTRA_TEXT) }
        val newText = safe { newIntent.getStringExtra(Intent.EXTRA_TEXT) }
        return oldText != null && oldText == newText
    }

    private var currentMarkServer: String = ServerManager.MARK_1
    private var currentMediaTitle: String? = null
    private var currentContentUrl: String? = null
    private var currentAllEpisodes: List<ResultEpisode> = emptyList()
    private var currentPlayingEpisodeIndex: Int = 0
    private var currentSelectedSeason: Int = 1
    private var currentLoadResponse: com.lagradost.cloudstream3.LoadResponse? = null
    private val triedServers: MutableSet<String> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val autoFullScreen = prefs.getBoolean("pref_auto_fullscreen", false)
        if (autoFullScreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else if (!isLayout(TV or EMULATOR)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
        CommonActivity.loadThemes(this)
        CommonActivity.init(this)
        enableEdgeToEdgeCompat()
        setContentView(R.layout.empty_layout)
        Log.i(TAG, "onCreate")

        setupWindowInsets()
        setupServerTabs()
        setupDownloadButton()
        updateLayoutForOrientation(resources.configuration.orientation)
        handleIntent(intent)

        attachBackPressedCallback("DownloadedPlayerActivity") {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && !isLayout(TV or EMULATOR)) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                finish()
            }
        }
    }

    private fun setupServerTabs() {
        val mark1Tab = findViewById<TextView>(R.id.server_tab_mark_1) ?: return
        val mark2Tab = findViewById<TextView>(R.id.server_tab_mark_2) ?: return
        val mark3Tab = findViewById<TextView>(R.id.server_tab_mark_3) ?: return
        val mark4Tab = findViewById<TextView>(R.id.server_tab_mark_4) ?: return

        val tabs = listOf(
            ServerManager.MARK_1 to mark1Tab,
            ServerManager.MARK_2 to mark2Tab,
            ServerManager.MARK_3 to mark3Tab,
            ServerManager.MARK_4 to mark4Tab
        )

        for ((mark, view) in tabs) {
            view.setOnClickListener {
                if (currentMarkServer != mark) {
                    triedServers.clear()
                    triedServers.add(mark)
                    updateServerTabsUI(mark)
                    onServerSwitched(mark)
                }
            }
        }

        updateServerTabsUI(currentMarkServer)
    }

    private fun updateServerTabsUI(selectedMark: String) {
        currentMarkServer = selectedMark
        val mark1Tab = findViewById<TextView>(R.id.server_tab_mark_1)
        val mark2Tab = findViewById<TextView>(R.id.server_tab_mark_2)
        val mark3Tab = findViewById<TextView>(R.id.server_tab_mark_3)
        val mark4Tab = findViewById<TextView>(R.id.server_tab_mark_4)

        val tabs = listOf(
            ServerManager.MARK_1 to mark1Tab,
            ServerManager.MARK_2 to mark2Tab,
            ServerManager.MARK_3 to mark3Tab,
            ServerManager.MARK_4 to mark4Tab
        )

        for ((mark, view) in tabs) {
            if (view == null) continue
            if (mark == selectedMark) {
                // Pure B&W: Solid White pill with Black text
                view.setBackgroundResource(R.drawable.bg_server_tab_active)
                view.setTextColor(Color.BLACK)
                view.setTypeface(null, Typeface.BOLD)
            } else {
                view.setBackgroundResource(R.drawable.bg_server_tab_inactive)
                view.setTextColor(Color.parseColor("#B0B0C0"))
                view.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    private fun triggerAutoServerFallback(reason: String) {
        triedServers.add(currentMarkServer)
        val nextMark = ServerManager.ALL_SERVERS.firstOrNull { it !in triedServers }
        if (nextMark != null) {
            main {
                android.widget.Toast.makeText(
                    this@DownloadedPlayerActivity,
                    "Switching automatically to $nextMark...",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                updateServerTabsUI(nextMark)
                onServerSwitched(nextMark)
            }
        } else {
            main {
                android.widget.Toast.makeText(
                    this@DownloadedPlayerActivity,
                    "All streaming servers unavailable. Please try another title.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun onServerSwitched(markName: String) {
        triedServers.add(markName)
        val providerName = ServerManager.toProviderName(markName)
        val title = currentMediaTitle ?: intent.getStringExtra("TITLE")
        if (title.isNullOrBlank()) {
            android.widget.Toast.makeText(this, "Switched to $markName", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        ioSafe {
            try {
                var api = APIHolder.getApiFromNameNull(providerName)
                    ?: APIHolder.apis.firstOrNull { it.name.equals(providerName, ignoreCase = true) }

                if (api == null) {
                    try {
                        com.movievortex.android.core.VortexPluginBootstrap.bootstrap(this@DownloadedPlayerActivity)
                    } catch (_: Exception) {}
                    api = APIHolder.getApiFromNameNull(providerName)
                        ?: APIHolder.apis.firstOrNull { it.name.equals(providerName, ignoreCase = true) }
                }

                if (api != null) {
                    val searchRes = api.search(title)
                    val match = searchRes?.firstOrNull()
                    if (match != null) {
                        main {
                            playProviderContent(providerName, match.url, intent)
                        }
                        return@ioSafe
                    }
                }

                // Fallback: try loading with the new provider and fallback url
                val fallbackUrl = currentContentUrl
                if (fallbackUrl != null && api != null) {
                    main {
                        playProviderContent(providerName, fallbackUrl, intent)
                    }
                    return@ioSafe
                }

                triggerAutoServerFallback("Stream unavailable on $markName.")
            } catch (e: Exception) {
                triggerAutoServerFallback("Error connecting to $markName.")
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLayoutForOrientation(newConfig.orientation)
        setupWindowInsets()
    }

    private fun setupWindowInsets() {
        val root = findViewById<View>(R.id.player_root_layout) ?: return
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBarInset = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT && !isLayout(TV or EMULATOR)) {
                view.setPadding(0, statusBarInset.top, 0, navBarInset.bottom)
            } else {
                view.setPadding(0, 0, 0, 0)
            }
            insets
        }
    }

    private fun updateLayoutForOrientation(orientation: Int) {
        val root = findViewById<ConstraintLayout>(R.id.player_root_layout) ?: return
        val navHost = findViewById<View>(R.id.nav_host_fragment) ?: return
        val detailsScroll = findViewById<View>(R.id.player_details_scroll) ?: return

        val set = ConstraintSet()
        set.clone(root)

        if (orientation == Configuration.ORIENTATION_LANDSCAPE || isLayout(TV or EMULATOR)) {
            // Fullscreen landscape
            detailsScroll.visibility = View.GONE
            set.clear(R.id.nav_host_fragment, ConstraintSet.BOTTOM)
            set.connect(R.id.nav_host_fragment, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            set.connect(R.id.nav_host_fragment, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            set.connect(R.id.nav_host_fragment, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            set.connect(R.id.nav_host_fragment, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            set.setDimensionRatio(R.id.nav_host_fragment, null)
            set.constrainHeight(R.id.nav_host_fragment, 0)
            set.constrainWidth(R.id.nav_host_fragment, 0)
            hideSystemUI()
        } else {
            // Portrait inline: 16:9 at top, details & episodes below
            detailsScroll.visibility = View.VISIBLE
            set.clear(R.id.nav_host_fragment, ConstraintSet.BOTTOM)
            set.connect(R.id.nav_host_fragment, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            set.connect(R.id.nav_host_fragment, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            set.connect(R.id.nav_host_fragment, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            set.setDimensionRatio(R.id.nav_host_fragment, "16:9")
            set.constrainHeight(R.id.nav_host_fragment, 0)
            set.constrainWidth(R.id.nav_host_fragment, 0)

            set.connect(R.id.player_details_scroll, ConstraintSet.TOP, R.id.nav_host_fragment, ConstraintSet.BOTTOM)
            set.connect(R.id.player_details_scroll, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            set.connect(R.id.player_details_scroll, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            set.connect(R.id.player_details_scroll, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            set.constrainHeight(R.id.player_details_scroll, 0)
            set.constrainWidth(R.id.player_details_scroll, 0)
            showSystemUI()
        }
        set.applyTo(root)
    }

    private fun handleIntent(intent: Intent) {
        val directUrl = intent.getStringExtra("DIRECT_URL") ?: intent.getStringExtra("LIVE_URL")
        val directTitle = intent.getStringExtra("TITLE")
        currentMediaTitle = directTitle
        if (!directUrl.isNullOrBlank()) {
            playDirectStream(directUrl, directTitle)
            return
        }

        val apiName = intent.getStringExtra(MainActivity.API_NAME_EXTRA_KEY)
        val contentUrl = intent.getStringExtra("CONTENT_URL") ?: intent.getStringExtra("URL")
        currentContentUrl = contentUrl
        if (apiName != null && contentUrl != null) {
            currentMarkServer = ServerManager.toDisplayName(apiName)
            triedServers.clear()
            triedServers.add(currentMarkServer)
            updateServerTabsUI(currentMarkServer)
            playProviderContent(apiName, contentUrl, intent)
            return
        }

        val offlinePath = intent.getStringExtra("OFFLINE_PATH")
        val offlineTitle = intent.getStringExtra("TITLE")
        if (!offlinePath.isNullOrBlank()) {
            playOfflineDirect(offlinePath, offlineTitle)
            return
        }

        val data = intent.data
        if (OfflinePlaybackHelper.playIntent(activity = this, intent = intent)) {
            return
        }

        if (
            intent.action == Intent.ACTION_SEND ||
            intent.action == Intent.ACTION_OPEN_DOCUMENT ||
            intent.action == Intent.ACTION_VIEW
        ) {
            val extraText = safe { intent.getStringExtra(Intent.EXTRA_TEXT) }
            val cd = intent.clipData
            val item = if (cd != null && cd.itemCount > 0) cd.getItemAt(0) else null
            val url = item?.text?.toString()
            when {
                item?.uri != null -> playUriSafely(item.uri)
                url != null -> playLink(this, url)
                data != null -> playUriSafely(data)
                extraText != null -> playLink(this, extraText)
                else -> finish()
            }
        } else if (data?.scheme == "content") {
            playUriSafely(data)
        } else finish()
    }

    private fun playOfflineDirect(path: String, title: String?) {
        val uri = if (path.startsWith("content://")) {
            Uri.parse(path)
        } else {
            Uri.fromFile(File(path))
        }
        val displayName = title ?: (if (!path.startsWith("content://")) File(path).nameWithoutExtension.replace("_", " ") else "Downloaded Movie")
        val extractorUri = ExtractorUri(
            uri = uri,
            name = displayName,
            id = path.hashCode()
        )
        val args = GeneratorPlayer.newInstance(
            DownloadFileGenerator(listOf(extractorUri)),
            0
        )
        runOnUiThread {
            findViewById<TextView>(R.id.inline_player_title)?.text = displayName
            findViewById<TextView>(R.id.inline_player_meta)?.text = "Offline Download"
            findViewById<TextView>(R.id.inline_player_overview)?.text = displayName
        }
        navigateToPlayer(args)
    }

    private fun playDirectStream(url: String, title: String?) {
        val displayName = title ?: "Live TV"
        val args = GeneratorPlayer.newInstance(
            LinkGenerator(
                listOf(BasicLink(url, displayName)),
                extract = false,
                id = url.hashCode()
            ),
            0
        )
        runOnUiThread {
            findViewById<TextView>(R.id.inline_player_title)?.text = displayName
            findViewById<TextView>(R.id.inline_player_meta)?.text = "Live Stream"
            findViewById<TextView>(R.id.inline_player_overview)?.text = "Playing live stream"
        }
        navigateToPlayer(args)
    }

    private fun playUriSafely(uri: Uri) {
        val name = SafeFile.fromUri(this, uri)?.name() ?: uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Downloaded Video"
        val extractorUri = ExtractorUri(
            uri = uri,
            name = name,
            id = uri.hashCode()
        )
        val args = GeneratorPlayer.newInstance(
            DownloadFileGenerator(listOf(extractorUri)),
            0
        )
        navigateToPlayer(args)
    }

    private fun navigateToPlayer(args: Bundle) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.navigation_player, inclusive = true, saveState = false)
            .build()
        window.decorView.post {
            try {
                val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                val navController = navHost?.navController
                if (navController != null) {
                    if (navController.currentDestination?.id == R.id.navigation_player) {
                        navController.navigate(R.id.navigation_player, args, navOptions)
                    } else {
                        val graph = navController.navInflater.inflate(R.navigation.mobile_navigation)
                        graph.setStartDestination(R.id.navigation_player)
                        navController.setGraph(graph, args)
                    }
                } else {
                    this.navigate(R.id.global_to_navigation_player, args, navOptions)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to player fragment", e)
                try {
                    this.navigate(R.id.global_to_navigation_player, args)
                } catch (t: Throwable) {
                    Log.e(TAG, "Fallback navigation failed", t)
                }
            }
        }
    }

    private fun playProviderContent(apiName: String, url: String, intent: Intent) {
        ioSafe {
            try {
                var api = APIHolder.getApiFromNameNull(apiName)
                    ?: APIHolder.apis.firstOrNull { url.startsWith(it.mainUrl) }
                    ?: APIHolder.apis.firstOrNull()

                if (api == null) {
                    try {
                        com.movievortex.android.core.VortexPluginBootstrap.bootstrap(this@DownloadedPlayerActivity)
                    } catch (_: Exception) {}
                    api = APIHolder.getApiFromNameNull(apiName)
                        ?: APIHolder.apis.firstOrNull { url.startsWith(it.mainUrl) }
                        ?: APIHolder.apis.firstOrNull()
                }

                if (api == null) {
                    triggerAutoServerFallback("Provider $currentMarkServer not available.")
                    return@ioSafe
                }

                val repo = APIRepository(api)
                val res = repo.load(url)
                if (res is Resource.Success) {
                    val loadResponse = res.value
                    val episodes: List<ResultEpisode> = when (loadResponse) {
                        is MovieLoadResponse -> {
                            listOf(
                                ResultEpisode(
                                    headerName = loadResponse.name,
                                    name = loadResponse.name,
                                    poster = loadResponse.posterUrl,
                                    episode = 1,
                                    seasonIndex = null,
                                    season = null,
                                    data = loadResponse.dataUrl.ifBlank { url },
                                    apiName = api.name,
                                    id = (api.name + url).hashCode(),
                                    index = 0,
                                    position = 0L,
                                    duration = 0L,
                                    score = loadResponse.score,
                                    description = loadResponse.plot,
                                    isFiller = false,
                                    tvType = TvType.Movie,
                                    parentId = url.hashCode(),
                                    videoWatchState = VideoWatchState.None
                                )
                            )
                        }
                        is TvSeriesLoadResponse -> {
                            loadResponse.episodes.mapIndexed { idx, ep ->
                                ResultEpisode(
                                    headerName = ep.name ?: "Episode ${ep.episode ?: (idx + 1)}",
                                    name = ep.name,
                                    poster = ep.posterUrl,
                                    episode = ep.episode ?: (idx + 1),
                                    seasonIndex = ep.season,
                                    season = ep.season,
                                    data = ep.data,
                                    apiName = api.name,
                                    id = ep.data.hashCode(),
                                    index = idx,
                                    position = 0L,
                                    duration = 0L,
                                    score = ep.score,
                                    description = ep.description,
                                    isFiller = false,
                                    tvType = TvType.TvSeries,
                                    parentId = url.hashCode(),
                                    videoWatchState = VideoWatchState.None
                                )
                            }
                        }
                        is AnimeLoadResponse -> {
                            loadResponse.episodes.values.flatten().mapIndexed { idx, ep ->
                                ResultEpisode(
                                    headerName = ep.name ?: "Episode ${ep.episode ?: (idx + 1)}",
                                    name = ep.name,
                                    poster = ep.posterUrl,
                                    episode = ep.episode ?: (idx + 1),
                                    seasonIndex = ep.season,
                                    season = ep.season,
                                    data = ep.data,
                                    apiName = api.name,
                                    id = ep.data.hashCode(),
                                    index = idx,
                                    position = 0L,
                                    duration = 0L,
                                    score = ep.score,
                                    description = ep.description,
                                    isFiller = false,
                                    tvType = TvType.Anime,
                                    parentId = url.hashCode(),
                                    videoWatchState = VideoWatchState.None
                                )
                            }
                        }
                        else -> emptyList()
                    }

                    if (episodes.isEmpty()) {
                        triggerAutoServerFallback("No streaming links found on $currentMarkServer.")
                        return@ioSafe
                    }

                    val targetEpNumber = intent.getIntExtra("EPISODE", -1)
                    val targetEpData = intent.getStringExtra("EPISODE_DATA")
                    val startIndex = when {
                        targetEpData != null -> episodes.indexOfFirst { it.data == targetEpData }.coerceAtLeast(0)
                        targetEpNumber != -1 -> episodes.indexOfFirst { it.episode == targetEpNumber }.coerceAtLeast(0)
                        else -> 0
                    }

                    val generator = RepoLinkGenerator(episodes, page = loadResponse)

                    main {
                        currentLoadResponse = loadResponse
                        currentMediaTitle = loadResponse.name
                        currentAllEpisodes = episodes

                        findViewById<TextView>(R.id.inline_player_title)?.text = loadResponse.name
                        val meta = buildString {
                            loadResponse.year?.let { append("$it  •  ") }
                            if (loadResponse is TvSeriesLoadResponse) append("TV Series") else append("Movie")
                            loadResponse.score?.let { append("  •  ★ $it") }
                        }
                        findViewById<TextView>(R.id.inline_player_meta)?.text = meta
                        findViewById<TextView>(R.id.inline_player_overview)?.text = loadResponse.plot ?: ""
                        findViewById<TextView>(R.id.inline_download_text)?.text = if (loadResponse is MovieLoadResponse) "Download Movie" else "Download Episode"

                        // Star Cast Details
                        setupCast(loadResponse.actors)

                        // Related Movies Recommendations
                        setupRelatedMovies(loadResponse.recommendations)

                        if (episodes.isNotEmpty() && (loadResponse is TvSeriesLoadResponse || loadResponse is AnimeLoadResponse || episodes.size > 1)) {
                            val seasons = episodes.mapNotNull { it.season }.distinct().sorted()
                            val availableSeasons = if (seasons.isEmpty()) listOf(1) else seasons
                            setupSeasonTabs(availableSeasons, episodes, loadResponse, startIndex)
                        } else {
                            findViewById<View>(R.id.inline_seasons_section)?.visibility = View.GONE
                        }

                        navigateToPlayer(GeneratorPlayer.newInstance(generator, startIndex))
                    }
                } else {
                    triggerAutoServerFallback("Failed to resolve stream on $currentMarkServer.")
                }
            } catch (e: Exception) {
                logError(e)
                triggerAutoServerFallback("Error loading $currentMarkServer: ${e.message}")
            }
        }
    }

    private fun setupDownloadButton() {
        val downloadBtn = findViewById<View>(R.id.inline_download_button) ?: return
        downloadBtn.setOnClickListener {
            val resp = currentLoadResponse
            val ep = currentAllEpisodes.getOrNull(currentPlayingEpisodeIndex) ?: currentAllEpisodes.firstOrNull()
            if (resp != null && ep != null) {
                val isMovie = resp is MovieLoadResponse
                try {
                    DownloadQueueManager.addToQueue(
                        DownloadObjects.DownloadQueueItem(
                            episode = ep,
                            isMovie = isMovie,
                            resultName = resp.name,
                            resultType = resp.type,
                            resultPoster = resp.posterUrl,
                            apiName = resp.apiName,
                            resultId = (resp.apiName + resp.url).hashCode(),
                            resultUrl = resp.url
                        ).toWrapper()
                    )
                    android.widget.Toast.makeText(
                        this,
                        "Download started: ${ep.name ?: resp.name}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    findViewById<TextView>(R.id.inline_download_text)?.text = "Download Queued"
                } catch (e: Exception) {
                    android.widget.Toast.makeText(
                        this,
                        "Download error: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                android.widget.Toast.makeText(this, "Stream details loading...", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCast(actors: List<ActorData>?) {
        val castSection = findViewById<View>(R.id.inline_cast_section) ?: return
        val castRecycler = findViewById<RecyclerView>(R.id.inline_cast_recycler) ?: return
        if (actors.isNullOrEmpty()) {
            castSection.visibility = View.GONE
            return
        }
        castSection.visibility = View.VISIBLE
        castRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        castRecycler.adapter = InlineCastAdapter(actors)
    }

    private fun setupRelatedMovies(recommendations: List<SearchResponse>?) {
        val relatedSection = findViewById<View>(R.id.inline_related_section) ?: return
        val relatedRecycler = findViewById<RecyclerView>(R.id.inline_related_recycler) ?: return
        if (recommendations.isNullOrEmpty()) {
            relatedSection.visibility = View.GONE
            return
        }
        relatedSection.visibility = View.VISIBLE
        relatedRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        relatedRecycler.adapter = InlineRelatedAdapter(recommendations) { item ->
            currentContentUrl = item.url
            currentMediaTitle = item.name
            currentMarkServer = ServerManager.toDisplayName(item.apiName)
            updateServerTabsUI(currentMarkServer)
            triedServers.clear()
            triedServers.add(currentMarkServer)
            playProviderContent(item.apiName, item.url, intent)
        }
    }

    private fun setupSeasonTabs(
        seasons: List<Int>,
        allEpisodes: List<ResultEpisode>,
        loadResponse: com.lagradost.cloudstream3.LoadResponse,
        initialPlayingIndex: Int
    ) {
        val seasonsSection = findViewById<View>(R.id.inline_seasons_section) ?: return
        val seasonsContainer = findViewById<LinearLayout>(R.id.inline_seasons_container) ?: return
        val seasonsScroll = findViewById<View>(R.id.inline_seasons_scroll) ?: return
        val recycler = findViewById<RecyclerView>(R.id.inline_episodes_recycler) ?: return

        seasonsSection.visibility = View.VISIBLE
        seasonsContainer.removeAllViews()

        if (seasons.size <= 1) {
            seasonsScroll.visibility = View.GONE
        } else {
            seasonsScroll.visibility = View.VISIBLE
        }

        currentPlayingEpisodeIndex = initialPlayingIndex
        val playingEp = allEpisodes.getOrNull(initialPlayingIndex)
        val initialSeason = playingEp?.season ?: seasons.firstOrNull() ?: 1

        fun loadSeasonEpisodes(seasonNum: Int) {
            currentSelectedSeason = seasonNum
            val filtered = allEpisodes.filter { (it.season ?: 1) == seasonNum }
            val displayEpisodes = if (filtered.isNotEmpty()) filtered else allEpisodes

            recycler.layoutManager = LinearLayoutManager(this@DownloadedPlayerActivity)
            recycler.adapter = InlineEpisodeAdapter(displayEpisodes, currentPlayingEpisodeIndex) { clickedEp ->
                currentPlayingEpisodeIndex = clickedEp.index
                val gen = RepoLinkGenerator(allEpisodes, page = loadResponse)
                navigateToPlayer(GeneratorPlayer.newInstance(gen, clickedEp.index))
            }

            for (i in 0 until seasonsContainer.childCount) {
                val tabView = seasonsContainer.getChildAt(i) as? TextView ?: continue
                val tagSeason = tabView.tag as? Int ?: (i + 1)
                if (tagSeason == seasonNum) {
                    tabView.setBackgroundResource(R.drawable.bg_season_tab_active)
                    tabView.setTextColor(Color.WHITE)
                    tabView.setTypeface(null, Typeface.BOLD)
                } else {
                    tabView.setBackgroundResource(R.drawable.bg_season_tab_inactive)
                    tabView.setTextColor(Color.parseColor("#B0B0C0"))
                    tabView.setTypeface(null, Typeface.NORMAL)
                }
            }
        }

        for (s in seasons) {
            val tab = TextView(this).apply {
                text = "Season $s"
                textSize = 13f
                tag = s
                val padH = (16 * resources.displayMetrics.density).toInt()
                val padV = (8 * resources.displayMetrics.density).toInt()
                setPadding(padH, padV, padH, padV)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = (8 * resources.displayMetrics.density).toInt()
                }
                layoutParams = params
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    loadSeasonEpisodes(s)
                }
            }
            seasonsContainer.addView(tab)
        }

        loadSeasonEpisodes(initialSeason)
    }

    override fun onResume() {
        super.onResume()
        CommonActivity.setActivityInstance(this)
    }
}

class InlineEpisodeAdapter(
    private val episodes: List<ResultEpisode>,
    private var currentPlayingIndex: Int,
    private val onEpisodeClick: (ResultEpisode) -> Unit
) : RecyclerView.Adapter<InlineEpisodeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view.findViewById(R.id.episode_item_root)
        val thumbnail: ImageView = view.findViewById(R.id.ep_thumbnail)
        val title: TextView = view.findViewById(R.id.ep_title)
        val desc: TextView = view.findViewById(R.id.ep_desc)
        val playIcon: ImageView = view.findViewById(R.id.ep_play_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_inline_episode, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ep = episodes[position]
        val epNum = ep.episode ?: (position + 1)
        val epTitleText = if (!ep.name.isNullOrBlank() && ep.name != "Episode $epNum") {
            "$epNum. ${ep.name}"
        } else {
            "Episode $epNum"
        }
        holder.title.text = epTitleText
        holder.desc.text = ep.description ?: ""
        holder.desc.visibility = if (ep.description.isNullOrBlank()) View.GONE else View.VISIBLE

        if (!ep.poster.isNullOrBlank()) {
            holder.thumbnail.load(ep.poster)
        } else {
            holder.thumbnail.setImageResource(R.drawable.example_poster)
        }

        val isSelected = ep.index == currentPlayingIndex
        if (isSelected) {
            holder.root.setBackgroundResource(R.drawable.bg_episode_item_active)
            holder.playIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
        } else {
            holder.root.setBackgroundResource(R.drawable.bg_episode_item_inactive)
            holder.playIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"))
        }

        holder.root.setOnClickListener {
            val prev = currentPlayingIndex
            currentPlayingIndex = ep.index
            notifyDataSetChanged()
            onEpisodeClick(ep)
        }
    }

    override fun getItemCount(): Int = episodes.size
}

class InlineCastAdapter(
    private val actors: List<ActorData>
) : RecyclerView.Adapter<InlineCastAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.cast_image)
        val name: TextView = view.findViewById(R.id.cast_name)
        val role: TextView = view.findViewById(R.id.cast_role)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_inline_cast, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = actors[position]
        holder.name.text = item.actor.name
        val roleStr = item.roleString ?: item.role?.name ?: ""
        holder.role.text = roleStr
        holder.role.visibility = if (roleStr.isBlank()) View.GONE else View.VISIBLE

        val imgUrl = item.actor.image
        if (!imgUrl.isNullOrBlank()) {
            holder.image.load(imgUrl)
        } else {
            holder.image.setImageResource(R.drawable.example_poster)
        }
    }

    override fun getItemCount(): Int = actors.size
}

class InlineRelatedAdapter(
    private val items: List<SearchResponse>,
    private val onItemClick: (SearchResponse) -> Unit
) : RecyclerView.Adapter<InlineRelatedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view.findViewById(R.id.related_item_root)
        val poster: ImageView = view.findViewById(R.id.related_poster)
        val title: TextView = view.findViewById(R.id.related_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_inline_related, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.name
        val posterUrl = item.posterUrl
        if (!posterUrl.isNullOrBlank()) {
            holder.poster.load(posterUrl)
        } else {
            holder.poster.setImageResource(R.drawable.example_poster)
        }
        holder.root.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
