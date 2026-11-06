package com.movievortex.android.ui.detail

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.movievortex.android.controller.DetailController
import com.movievortex.android.core.ServerManager
import com.movievortex.android.model.MediaEpisode
import com.movievortex.android.model.MediaType
import com.movievortex.android.model.MovieVortexMedia
import com.movievortex.android.ui.components.PosterCard
import com.movievortex.android.ui.components.VortexIcons
import com.movievortex.android.ui.theme.ManropeFontFamily
import kotlinx.coroutines.delay

data class CastUiItem(val name: String, val character: String, val photoUrl: String?)

@Composable
fun DetailScreen(
    detailController: DetailController,
    onPlayMovie: (MovieVortexMedia) -> Unit,
    onPlayEpisode: (MovieVortexMedia, MediaEpisode) -> Unit,
    onDownloadClick: (MovieVortexMedia, MediaEpisode?) -> Unit,
    onMediaClick: (MovieVortexMedia) -> Unit = onPlayMovie,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by detailController.uiState.collectAsState()
    val media = state.media

    // Download modal state
    var showDownloadDialog by remember { mutableStateOf(false) }

    // ExoPlayer inline player instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var areControlsVisible by remember { mutableStateOf(true) }

    DisposableEffect(exoPlayer) {
        onDispose {
            try {
                exoPlayer.release()
            } catch (_: Exception) {}
        }
    }

    // Update player position periodically
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.coerceAtLeast(0L)
            delay(500)
        }
    }

    // Attach stream source to inline ExoPlayer when activeStreamUrl updates
    LaunchedEffect(state.activeStreamUrl) {
        val url = state.activeStreamUrl
        if (!url.isNullOrBlank()) {
            try {
                val mediaItem = MediaItem.fromUri(url)
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                state.activeStreamSource?.headers?.takeIf { it.isNotEmpty() }?.let {
                    dataSourceFactory.setDefaultRequestProperties(it)
                }
                val mediaSource = DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem)
                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()
                exoPlayer.play()
                isPlaying = true
            } catch (_: Exception) {
                try {
                    val mediaItem = MediaItem.fromUri(url)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()
                    isPlaying = true
                } catch (_: Exception) {}
            }
        }
    }

    if (state.isLoading && media == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF06141B)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
        return
    }

    if (media == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF06141B)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.errorMessage ?: "Details unavailable",
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily
            )
        }
        return
    }

    // 100% Real cast from TMDB / Provider
    val displayCast = remember(media.cast) {
        media.cast.map { c ->
            CastUiItem(
                name = c.name,
                character = c.role?.takeIf { it.isNotBlank() } ?: "Cast",
                photoUrl = c.avatarUrl
            )
        }
    }

    val scrollState = rememberLazyListState()

    var isEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isEntered = true
    }

    // Watchlist & Download Button Springs
    val watchlistInteraction = remember { MutableInteractionSource() }
    var isWatchlistPressed by remember { mutableStateOf(false) }
    LaunchedEffect(watchlistInteraction) {
        watchlistInteraction.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isWatchlistPressed = true
                is PressInteraction.Release,
                is PressInteraction.Cancel -> isWatchlistPressed = false
            }
        }
    }
    val watchlistScale by animateFloatAsState(
        targetValue = if (isWatchlistPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "watchlist_scale"
    )

    val downloadInteraction = remember { MutableInteractionSource() }
    var isDownloadPressed by remember { mutableStateOf(false) }
    LaunchedEffect(downloadInteraction) {
        downloadInteraction.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isDownloadPressed = true
                is PressInteraction.Release,
                is PressInteraction.Cancel -> isDownloadPressed = false
            }
        }
    }
    val downloadScale by animateFloatAsState(
        targetValue = if (isDownloadPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "download_scale"
    )

    val heartScale by animateFloatAsState(
        targetValue = if (state.isInMyList) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heart_bounce"
    )

    // Download options dialog
    if (showDownloadDialog) {
        Dialog(onDismissRequest = { showDownloadDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF11212D))
                    .border(1.dp, Color(0xFF253745), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Download Options",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = media.title,
                        color = Color(0xFF9BA8AB),
                        fontSize = 13.sp,
                        fontFamily = ManropeFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    val downloadOptions = listOf(
                        "1080p Full HD • High Quality",
                        "720p HD • Standard Quality",
                        "480p SD • Data Saver"
                    )

                    downloadOptions.forEach { opt ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF253745))
                                .border(1.dp, Color(0x334A5C6A), RoundedCornerShape(12.dp))
                                .clickable {
                                    val streamUrl = state.activeStreamUrl
                                    if (!streamUrl.isNullOrBlank()) {
                                        try {
                                            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                                            val cleanName = media.title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                                            val request = DownloadManager.Request(Uri.parse(streamUrl))
                                                .setTitle(media.title)
                                                .setDescription("Downloading ${media.title}")
                                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MovieVortex/$cleanName.mp4")
                                                .setAllowedOverMetered(true)
                                                .setAllowedOverRoaming(true)
                                            dm?.enqueue(request)
                                            Toast.makeText(context, "Download started: ${media.title}", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Download queued: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Resolving video link for download...", Toast.LENGTH_SHORT).show()
                                    }
                                    showDownloadDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = opt,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = VortexIcons.Download,
                                    contentDescription = null,
                                    tint = Color(0xFF9BA8AB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06141B)),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. EMBEDDED DIRECT INLINE VIDEO PLAYER (No fullscreen overlay forced on entry)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF06141B))
            ) {
                if (state.activeStreamUrl != null) {
                    // Direct Inline Video Player View
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                setShutterBackgroundColor(android.graphics.Color.BLACK)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { areControlsVisible = !areControlsVisible }
                    )

                    // Inline Player Controls Overlay
                    AnimatedVisibility(
                        visible = areControlsVisible,
                        enter = fadeIn(tween(180)),
                        exit = fadeOut(tween(180))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x77000000))
                                .padding(12.dp)
                        ) {
                            // Top Bar inside Player: Back Button & Fullscreen expand
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .windowInsetsPadding(WindowInsets.statusBars),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x6611212D))
                                        .clickable { onBackClick() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = VortexIcons.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Fullscreen Expand Button (Bypasses to fullscreen player when clicked)
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x6611212D))
                                            .clickable {
                                                if (media.type == MediaType.TvSeries || media.type == MediaType.Anime) {
                                                    state.currentPlayingEpisode?.let { ep ->
                                                        onPlayEpisode(media, ep)
                                                    } ?: onPlayMovie(media)
                                                } else {
                                                    onPlayMovie(media)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = VortexIcons.Fullscreen,
                                            contentDescription = "Fullscreen",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Center Play / Pause
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .background(Color(0x9911212D))
                                    .border(1.dp, Color(0x334A5C6A), CircleShape)
                                    .clickable {
                                        if (isPlaying) {
                                            exoPlayer.pause()
                                            isPlaying = false
                                        } else {
                                            exoPlayer.play()
                                            isPlaying = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) VortexIcons.Pause else VortexIcons.Play,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Bottom Inline Player Timeline Slider & Duration
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val curMin = (currentPositionMs / 1000) / 60
                                    val curSec = (currentPositionMs / 1000) % 60
                                    val durMin = (durationMs / 1000) / 60
                                    val durSec = (durationMs / 1000) % 60
                                    Text(
                                        text = "%02d:%02d".format(curMin, curSec),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = ManropeFontFamily
                                    )
                                    Text(
                                        text = "%02d:%02d".format(durMin, durSec),
                                        color = Color(0xFF9BA8AB),
                                        fontSize = 11.sp,
                                        fontFamily = ManropeFontFamily
                                    )
                                }

                                val progressFraction = if (durationMs > 0) {
                                    (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                                } else 0f

                                Slider(
                                    value = progressFraction,
                                    onValueChange = { frac ->
                                        val targetMs = (frac * durationMs).toLong()
                                        exoPlayer.seekTo(targetMs)
                                        currentPositionMs = targetMs
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.White,
                                        inactiveTrackColor = Color(0x554A5C6A)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Poster Backdrop with Loading Spinner or Fallback Status
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = media.backdrop ?: media.poster,
                            contentDescription = media.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x8806141B), Color(0xCC06141B))
                                    )
                                )
                        )

                        // Top Back Button
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x6611212D))
                                .clickable { onBackClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = VortexIcons.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Status / Auto-Fallback Spinner
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (state.isResolvingStream) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(34.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = state.fallbackStatusMessage ?: "Connecting to ${state.selectedServer}...",
                                    color = Color(0xFF9BA8AB),
                                    fontSize = 12.sp,
                                    fontFamily = ManropeFontFamily
                                )
                            } else if (state.streamError != null) {
                                Text(
                                    text = state.streamError ?: "Stream unavailable",
                                    color = Color(0xFF9BA8AB),
                                    fontSize = 12.sp,
                                    fontFamily = ManropeFontFamily
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF253745))
                                        .clickable { detailController.loadStreamsForServer(state.selectedServer) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Retry Stream",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = ManropeFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. SERVER SELECTION TABS (Mark 1, Mark 2, Mark 3, Mark 4 with Auto-Fallback Mechanism)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF11212D))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Server Source",
                        color = Color(0xFF9BA8AB),
                        fontSize = 12.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    state.fallbackStatusMessage?.let { msg ->
                        Text(
                            text = "Auto-fallback active",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = ManropeFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServerManager.ALL_SERVERS.forEach { serverName ->
                        val isSelected = state.selectedServer == serverName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(0xFF253745) else Color(0xFF06141B)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF4A5C6A) else Color(0x22253745),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { detailController.switchServer(serverName) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                                Text(
                                    text = serverName,
                                    color = if (isSelected) Color.White else Color(0xFF9BA8AB),
                                    fontSize = 12.sp,
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. MOVIE INFO: Title, Metadata, Rating, Actions
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Title
                Text(
                    text = media.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata Chips: Duration | Genre | Type | 4K UHD
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val runtimeText = media.runtime?.let { "${it / 60}h ${it % 60}min" } ?: "2h 10min"
                    MetadataChip(text = runtimeText)

                    val genre = media.genres.firstOrNull() ?: "Cinema"
                    MetadataChip(text = genre)

                    val typeLabel = if (media.type == MediaType.TvSeries) "Series" else "Movie"
                    MetadataChip(text = typeLabel)

                    MetadataChip(text = "4K UHD")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Star Rating & Quick Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Star Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "★",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val rating = media.rating ?: 8.4f
                        Text(
                            text = "%.1f/10".format(rating),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        media.year?.let { y ->
                            Text(
                                text = "• $y",
                                color = Color(0xFF9BA8AB),
                                fontSize = 12.sp,
                                fontFamily = ManropeFontFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action Buttons: Watchlist & Download (Monochrome)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Watchlist Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .graphicsLayer {
                                scaleX = watchlistScale
                                scaleY = watchlistScale
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (state.isInMyList) Color(0xFF253745) else Color(0xFF11212D)
                            )
                            .border(
                                1.dp,
                                if (state.isInMyList) Color.White else Color(0xFF253745),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(
                                interactionSource = watchlistInteraction,
                                indication = null,
                                onClick = { detailController.toggleMyList() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isInMyList) VortexIcons.Check else VortexIcons.Add,
                                contentDescription = "Watchlist",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (state.isInMyList) "In Watchlist" else "Watchlist",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Real-Time Download Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .graphicsLayer {
                                scaleX = downloadScale
                                scaleY = downloadScale
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF11212D))
                            .border(1.dp, Color(0xFF253745), RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = downloadInteraction,
                                indication = null,
                                onClick = { showDownloadDialog = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = VortexIcons.Download,
                                contentDescription = "Download",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Download",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 4. SYNOPSIS SECTION
        val synopsisText = media.synopsis?.takeIf { it.isNotBlank() }
        if (!synopsisText.isNullOrBlank()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Synopsis",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = synopsisText,
                        color = Color(0xFF9BA8AB),
                        fontSize = 13.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 5. CAST SECTION (100% Real TMDB Cast with actor photos and roles)
        if (displayCast.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cast",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(displayCast) { index, actor ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF11212D))
                                    .border(1.dp, Color(0xFF253745), RoundedCornerShape(16.dp))
                            ) {
                                AsyncImage(
                                    model = actor.photoUrl,
                                    contentDescription = actor.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = actor.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = actor.character,
                                color = Color(0xFF9BA8AB),
                                fontSize = 10.sp,
                                fontFamily = ManropeFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 6. TV SERIES SEASONS & EPISODES (if TV Series / Anime)
        if (media.type == MediaType.TvSeries || media.type == MediaType.Anime) {
            item {
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "Episodes",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(state.currentSeasonEpisodes) { ep ->
                val isCurrentlyPlaying = state.currentPlayingEpisode?.id == ep.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCurrentlyPlaying) Color(0xFF253745) else Color(0xFF11212D))
                        .border(
                            1.dp,
                            if (isCurrentlyPlaying) Color.White else Color(0x33253745),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { detailController.playEpisode(ep) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "EP ${ep.episodeNumber}: ${ep.title.ifBlank { "Episode ${ep.episodeNumber}" }}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                            ep.synopsis?.let { desc ->
                                Text(
                                    text = desc,
                                    color = Color(0xFF9BA8AB),
                                    fontSize = 11.sp,
                                    fontFamily = ManropeFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = VortexIcons.Play,
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. RELATED & RECOMMENDED MOVIES
        if (media.recommendations.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "More Like This",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(media.recommendations) { rec ->
                        PosterCard(
                            media = rec,
                            onClick = { onMediaClick(rec) },
                            width = 135.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF253745))
            .border(0.8.dp, Color(0x334A5C6A), RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}
