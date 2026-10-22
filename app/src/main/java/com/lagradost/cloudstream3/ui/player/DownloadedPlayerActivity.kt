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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
        if (!directUrl.isNullOrBlank()) {
            playDirectStream(directUrl, directTitle)
            return
        }

        val apiName = intent.getStringExtra(MainActivity.API_NAME_EXTRA_KEY)
        val contentUrl = intent.getStringExtra("CONTENT_URL") ?: intent.getStringExtra("URL")
        if (apiName != null && contentUrl != null) {
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
        val spinner = findViewById<ProgressBar>(R.id.player_loading_spinner)
        spinner?.isVisible = true

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
                    main {
                        spinner?.isVisible = false
                        android.widget.Toast.makeText(
                            this@DownloadedPlayerActivity,
                            "Connecting streaming provider... please retry",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
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
                        main {
                            spinner?.isVisible = false
                            finish()
                        }
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
                        spinner?.isVisible = false
                        findViewById<TextView>(R.id.inline_player_title)?.text = loadResponse.name
                        val meta = buildString {
                            loadResponse.year?.let { append("$it  •  ") }
                            if (loadResponse is TvSeriesLoadResponse) append("TV Series") else append("Movie")
                            loadResponse.rating?.let { append("  •  ★ $it") }
                        }
                        findViewById<TextView>(R.id.inline_player_meta)?.text = meta
                        findViewById<TextView>(R.id.inline_player_overview)?.text = loadResponse.plot ?: ""

                        if (episodes.size > 1) {
                            val header = findViewById<TextView>(R.id.inline_episodes_header)
                            val recycler = findViewById<RecyclerView>(R.id.inline_episodes_recycler)
                            header?.visibility = View.VISIBLE
                            recycler?.visibility = View.VISIBLE
                            recycler?.layoutManager = LinearLayoutManager(this@DownloadedPlayerActivity)
                            recycler?.adapter = InlineEpisodeAdapter(episodes, startIndex) { clickedIndex ->
                                val gen = RepoLinkGenerator(episodes, page = loadResponse)
                                navigateToPlayer(GeneratorPlayer.newInstance(gen, clickedIndex))
                            }
                        }

                        navigateToPlayer(GeneratorPlayer.newInstance(generator, startIndex))
                    }
                } else {
                    main {
                        spinner?.isVisible = false
                        android.widget.Toast.makeText(
                            this@DownloadedPlayerActivity,
                            "Unable to resolve stream from provider. Retrying...",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                logError(e)
                main {
                    spinner?.isVisible = false
                    android.widget.Toast.makeText(
                        this@DownloadedPlayerActivity,
                        "Player error: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CommonActivity.setActivityInstance(this)
    }
}

class InlineEpisodeAdapter(
    private val episodes: List<ResultEpisode>,
    private var currentPlayingIndex: Int,
    private val onEpisodeClick: (Int) -> Unit
) : RecyclerView.Adapter<InlineEpisodeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.ep_number)
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
        holder.number.text = (ep.episode ?: (position + 1)).toString()
        holder.title.text = ep.name ?: "Episode ${ep.episode ?: (position + 1)}"
        holder.desc.text = ep.description ?: ""
        holder.desc.visibility = if (ep.description.isNullOrBlank()) View.GONE else View.VISIBLE

        val isSelected = position == currentPlayingIndex
        holder.itemView.setBackgroundColor(if (isSelected) 0x25FFFFFF else 0x00000000)
        holder.playIcon.alpha = if (isSelected) 1.0f else 0.4f

        holder.itemView.setOnClickListener {
            val prev = currentPlayingIndex
            currentPlayingIndex = position
            notifyItemChanged(prev)
            notifyItemChanged(currentPlayingIndex)
            onEpisodeClick(position)
        }
    }

    override fun getItemCount(): Int = episodes.size
}
