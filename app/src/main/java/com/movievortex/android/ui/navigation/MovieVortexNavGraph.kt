package com.movievortex.android.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movievortex.android.controller.AnimeController
import com.movievortex.android.controller.DetailController
import com.movievortex.android.controller.DownloadController
import com.movievortex.android.controller.HomeController
import com.movievortex.android.controller.LibraryController
import com.movievortex.android.controller.PlayerController
import com.movievortex.android.controller.SearchController
import com.movievortex.android.controller.SettingsController
import com.movievortex.android.model.LiveChannel
import com.movievortex.android.model.MediaEpisode
import com.movievortex.android.model.MediaType
import com.movievortex.android.model.MovieVortexMedia
import com.movievortex.android.model.StreamingPlatform
import com.movievortex.android.ui.anime.AnimeScreen
import com.movievortex.android.ui.catalog.PlatformCatalogScreen
import com.movievortex.android.ui.components.MainDestination
import com.movievortex.android.ui.components.AuthModal
import com.movievortex.android.ui.components.MovieVortexBottomBar
import com.movievortex.android.ui.components.MovieVortexTopBar
import com.movievortex.android.ui.detail.DetailScreen
import com.movievortex.android.ui.dns.DnsIntroScreen
import com.movievortex.android.ui.downloads.DownloadsScreen
import com.movievortex.android.ui.home.HomeScreen
import com.movievortex.android.ui.livetv.LiveTvScreen
import com.movievortex.android.ui.library.HistoryScreen
import com.movievortex.android.ui.library.WatchlistScreen
import com.movievortex.android.ui.player.MovieVortexPlayerScreen
import com.movievortex.android.ui.search.SearchScreen
import com.movievortex.android.ui.settings.SettingsScreen
import com.movievortex.android.ui.splash.MovieVortexSplashScreen
import com.movievortex.android.ui.theme.VortexColors

sealed class AppScreen {
    object Splash : AppScreen()
    object DnsIntro : AppScreen()
    object Main : AppScreen()
    object Search : AppScreen()
    object Watchlist : AppScreen()
    object History : AppScreen()
    data class PlatformCatalog(val platform: StreamingPlatform) : AppScreen()
    data class Detail(val media: MovieVortexMedia) : AppScreen()
    data class Player(val media: MovieVortexMedia, val episode: MediaEpisode? = null, val offlinePath: String? = null) : AppScreen()
}

fun launchCloudStreamPlayer(
    context: Context,
    media: MovieVortexMedia,
    episode: MediaEpisode? = null
) {
    try {
        val intent = Intent(context, com.lagradost.cloudstream3.ui.player.DownloadedPlayerActivity::class.java).apply {
            if (media.type == MediaType.LiveStream || media.providerApiName == "LiveTV" || media.rawUrl.endsWith(".m3u8") || media.rawUrl.contains(".m3u8")) {
                putExtra("DIRECT_URL", media.rawUrl)
                putExtra("LIVE_URL", media.rawUrl)
                putExtra("TITLE", media.title)
            } else {
                putExtra(com.lagradost.cloudstream3.MainActivity.API_NAME_EXTRA_KEY, media.providerApiName)
                putExtra("CONTENT_URL", media.rawUrl)
                putExtra("TITLE", media.title)
                putExtra("MEDIA_OBJECT", media)
                if (episode != null) {
                    putExtra("EPISODE", episode.episodeNumber)
                    putExtra("SEASON", episode.seasonNumber)
                    putExtra("EPISODE_DATA", episode.dataUrl)
                }
            }
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("MovieVortex", "Error launching player", e)
    }
}

fun launchLiveStreamPlayer(context: Context, channel: LiveChannel) {
    try {
        val intent = Intent(context, com.lagradost.cloudstream3.ui.player.DownloadedPlayerActivity::class.java).apply {
            putExtra("DIRECT_URL", channel.streamUrl)
            putExtra("LIVE_URL", channel.streamUrl)
            putExtra("TITLE", channel.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("MovieVortex", "Error launching live player", e)
    }
}

fun launchOfflinePlayer(context: Context, localFilePath: String?, title: String? = null, itemId: String? = null) {
    var resolvedPath: String? = localFilePath?.takeIf { it.isNotBlank() && (it.startsWith("content://") || File(it).exists()) }

    if (resolvedPath.isNullOrBlank() && !title.isNullOrBlank()) {
        val found = com.movievortex.android.controller.findLocalMediaFile(context, title)
        if (found != null && found.exists()) {
            resolvedPath = found.absolutePath
        }
    }

    if (resolvedPath.isNullOrBlank()) {
        val intId = itemId?.toIntOrNull()
        if (intId != null) {
            val info = com.lagradost.cloudstream3.utils.downloader.VideoDownloadManager.getDownloadFileInfo(context, intId)
            if (info != null) {
                resolvedPath = info.path.toString()
            }
        }
    }

    if (resolvedPath.isNullOrBlank()) {
        val appDirs = listOfNotNull(
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS),
            context.filesDir,
            File("/storage/emulated/0/Download/MovieVortex")
        )
        val clean = title?.replace(Regex("[^a-zA-Z0-9]"), "")?.lowercase()
        for (dir in appDirs) {
            if (dir.exists()) {
                val match = dir.walkTopDown().firstOrNull { f ->
                    f.isFile && (f.extension.equals("mp4", true) || f.extension.equals("mkv", true)) &&
                            (clean != null && f.nameWithoutExtension.replace(Regex("[^a-zA-Z0-9]"), "").lowercase().contains(clean))
                }
                if (match != null) {
                    resolvedPath = match.absolutePath
                    break
                }
            }
        }
    }

    if (resolvedPath.isNullOrBlank()) {
        android.widget.Toast.makeText(context, "Downloaded media file not found on device", android.widget.Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val uri = if (resolvedPath.startsWith("content://")) {
            Uri.parse(resolvedPath)
        } else {
            val file = File(resolvedPath)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                try {
                    androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                } catch (_: Exception) {
                    Uri.fromFile(file)
                }
            } else {
                Uri.fromFile(file)
            }
        }
        val intent = Intent(context, com.lagradost.cloudstream3.ui.player.DownloadedPlayerActivity::class.java).apply {
            putExtra("OFFLINE_PATH", resolvedPath)
            if (!title.isNullOrBlank()) {
                putExtra("TITLE", title)
            }
            action = Intent.ACTION_VIEW
            setDataAndType(uri, "video/*")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("MovieVortex", "Error launching offline player", e)
    }
}

@Composable
fun MovieVortexNavGraph(
    homeController: HomeController = viewModel(),
    animeController: AnimeController = viewModel(),
    searchController: SearchController = viewModel(),
    detailController: DetailController = viewModel(),
    playerController: PlayerController = viewModel(),
    downloadController: DownloadController = viewModel(),
    libraryController: LibraryController = viewModel(),
    settingsController: SettingsController,
    isFirstLaunch: Boolean = false,
    initialDestination: MainDestination = MainDestination.MoviesTv,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
    var currentDestination by remember(initialDestination) { mutableStateOf(initialDestination) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Alex Vortex") }
    var showAuthModal by remember { mutableStateOf(false) }
    var authModalReason by remember { mutableStateOf("Sign in to sync your Watchlist and continue watching.") }

    androidx.compose.runtime.LaunchedEffect(initialDestination) {
        currentDestination = initialDestination
        if (initialDestination == MainDestination.Downloads) {
            currentScreen = AppScreen.Main
        }
    }

    val downloadState by downloadController.uiState.collectAsState()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is AppScreen.Splash -> {
                MovieVortexSplashScreen(
                    onSplashFinished = {
                        currentScreen = if (isFirstLaunch) AppScreen.DnsIntro else AppScreen.Main
                    }
                )
            }

            is AppScreen.DnsIntro -> {
                DnsIntroScreen(
                    settingsController = settingsController,
                    onContinue = { currentScreen = AppScreen.Main }
                )
            }

            is AppScreen.Main -> {
                Scaffold(
                    topBar = {
                        MovieVortexTopBar(
                            onSearchClick = { currentDestination = MainDestination.Search },
                            isTransparent = currentDestination == MainDestination.Home || currentDestination == MainDestination.LiveTv
                        )
                    },
                    bottomBar = {
                        MovieVortexBottomBar(
                            currentDestination = currentDestination,
                            onDestinationSelected = { currentDestination = it },
                            activeDownloadCount = downloadState.activeDownloadsCount
                        )
                    },
                    containerColor = VortexColors.BackgroundOled
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(targetState = currentDestination, label = "tab_crossfade") { dest ->
                            when (dest) {
                                MainDestination.Home -> {
                                    HomeScreen(
                                        homeController = homeController,
                                        onMediaClick = { media ->
                                            launchCloudStreamPlayer(context, media, null)
                                        },
                                        onPlayClick = { media ->
                                            launchCloudStreamPlayer(context, media, null)
                                        },
                                        onPlatformClick = { platform ->
                                            currentScreen = AppScreen.PlatformCatalog(platform)
                                        },
                                        onRequireAuth = { reason ->
                                            authModalReason = reason
                                            showAuthModal = true
                                        },
                                        onNavigateToLiveTv = {
                                            currentDestination = MainDestination.LiveTv
                                        }
                                    )
                                }

                                MainDestination.LiveTv -> {
                                    LiveTvScreen(
                                        onChannelClick = { channel ->
                                            launchLiveStreamPlayer(context, channel)
                                        }
                                    )
                                }

                                MainDestination.Search -> {
                                    SearchScreen(
                                        searchController = searchController,
                                        onMediaClick = { media ->
                                            launchCloudStreamPlayer(context, media, null)
                                        },
                                        onBackClick = { currentDestination = MainDestination.Home }
                                    )
                                }

                                MainDestination.Downloads -> {
                                    DownloadsScreen(
                                        downloadController = downloadController,
                                        onBrowseClick = { currentDestination = MainDestination.Home },
                                        onPlayOffline = { item ->
                                            launchOfflinePlayer(context, item.localFilePath, item.title, item.id)
                                        }
                                    )
                                }

                                MainDestination.Me -> {
                                    SettingsScreen(
                                        settingsController = settingsController,
                                        onNavigateToDownloads = { currentDestination = MainDestination.Downloads },
                                        onOpenDnsSetup = { currentScreen = AppScreen.DnsIntro },
                                        onOpenWatchlist = { currentScreen = AppScreen.Watchlist },
                                        onOpenHistory = { currentScreen = AppScreen.History },
                                        onRequireAuth = { reason ->
                                            authModalReason = reason
                                            showAuthModal = true
                                        },
                                        isLoggedIn = isLoggedIn,
                                        userName = userName
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is AppScreen.Search -> {
                SearchScreen(
                    searchController = searchController,
                    onMediaClick = { media ->
                        launchCloudStreamPlayer(context, media, null)
                    },
                    onBackClick = { currentScreen = AppScreen.Main }
                )
            }

            is AppScreen.PlatformCatalog -> {
                val homeState by homeController.uiState.collectAsState()
                PlatformCatalogScreen(
                    platform = screen.platform,
                    catalogItems = homeState.exploreGrid,
                    onMediaClick = { media ->
                        launchCloudStreamPlayer(context, media, null)
                    },
                    onBackClick = { currentScreen = AppScreen.Main }
                )
            }

            is AppScreen.Detail -> {
                launchCloudStreamPlayer(context, screen.media, null)
                currentScreen = AppScreen.Main
            }

            is AppScreen.Watchlist -> {
                WatchlistScreen(
                    libraryController = libraryController,
                    onMediaClick = { media ->
                        launchCloudStreamPlayer(context, media, null)
                    },
                    onBackClick = { currentScreen = AppScreen.Main }
                )
            }

            is AppScreen.History -> {
                HistoryScreen(
                    onMediaClick = { media ->
                        launchCloudStreamPlayer(context, media, null)
                    },
                    onBackClick = { currentScreen = AppScreen.Main }
                )
            }

            is AppScreen.Player -> {
                launchCloudStreamPlayer(context, screen.media, screen.episode)
                currentScreen = AppScreen.Main
            }
        }
    }

    if (showAuthModal) {
        AuthModal(
            actionReason = authModalReason,
            onDismiss = { showAuthModal = false },
            onLoginSuccess = { user ->
                isLoggedIn = true
                userName = user
                showAuthModal = false
            }
        )
    }
}
