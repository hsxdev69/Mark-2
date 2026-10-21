package com.movievortex.android.controller

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.lagradost.cloudstream3.APIHolder
import com.lagradost.cloudstream3.AnimeLoadResponse
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.MovieLoadResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvSeriesLoadResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.ui.APIRepository
import com.lagradost.cloudstream3.ui.player.CS3IPlayer
import com.lagradost.cloudstream3.ui.player.CSPlayerEvent
import com.lagradost.cloudstream3.ui.player.CSPlayerLoading
import com.lagradost.cloudstream3.ui.player.ErrorEvent
import com.lagradost.cloudstream3.ui.player.ExtractorUri
import com.lagradost.cloudstream3.ui.player.PlayerAttachedEvent
import com.lagradost.cloudstream3.ui.player.PositionEvent
import com.lagradost.cloudstream3.ui.player.RepoLinkGenerator
import com.lagradost.cloudstream3.ui.player.StatusEvent
import com.lagradost.cloudstream3.ui.player.SubtitleData
import com.lagradost.cloudstream3.ui.result.ResultEpisode
import com.lagradost.cloudstream3.ui.result.VideoWatchState
import com.lagradost.cloudstream3.utils.DataStoreHelper
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.newExtractorLink
import com.movievortex.android.core.CoreAdapter
import com.movievortex.android.model.MediaEpisode
import com.movievortex.android.model.MovieVortexMedia
import com.movievortex.android.model.StreamSource
import com.movievortex.android.model.SubtitleTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class PlayerStatus {
    Idle,
    ResolvingStreams,
    Ready,
    Playing,
    Paused,
    Buffering,
    Error
}

data class PlayerUiState(
    val status: PlayerStatus = PlayerStatus.Idle,
    val mediaTitle: String = "",
    val mediaSubtitle: String = "",
    val streamSources: List<StreamSource> = emptyList(),
    val currentSource: StreamSource? = null,
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val currentSubtitle: SubtitleTrack? = null,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isControlsVisible: Boolean = true,
    val isOfflinePlayback: Boolean = false,
    val statusMessage: String = "Connecting to stream...",
    val errorMessage: String? = null
)

class PlayerController : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // Original CloudStream Native Player Engine
    val cs3Player = CS3IPlayer()

    private val _attachedPlayer = MutableStateFlow<ExoPlayer?>(null)
    val attachedPlayer: StateFlow<ExoPlayer?> = _attachedPlayer.asStateFlow()

    private val rawExtractorLinks = mutableMapOf<String, ExtractorLink>()
    private val rawSubtitleData = mutableMapOf<String, SubtitleData>()
    private var currentContext: Context? = null
    private var streamLoadingJob: Job? = null

    private var activeDataUrl: String = ""
    private var activeApiName: String = ""

    init {
        cs3Player.initCallbacks(
            eventHandler = { event ->
                when (event) {
                    is PlayerAttachedEvent -> {
                        _attachedPlayer.value = event.player as? ExoPlayer
                    }
                    is PositionEvent -> {
                        _uiState.value = _uiState.value.copy(
                            currentPositionMs = event.toMs,
                            durationMs = event.durationMs
                        )
                        if (activeDataUrl.isNotBlank() && event.durationMs > 0) {
                            DataStoreHelper.setViewPos(activeDataUrl.hashCode(), event.toMs, event.durationMs)
                        }
                    }
                    is StatusEvent -> {
                        val isPlaying = event.isPlaying == CSPlayerLoading.IsPlaying
                        val isBuffering = event.isPlaying == CSPlayerLoading.IsBuffering
                        _uiState.value = _uiState.value.copy(
                            status = if (isPlaying) PlayerStatus.Playing
                            else if (isBuffering) PlayerStatus.Buffering
                            else PlayerStatus.Paused
                        )
                    }
                    is ErrorEvent -> {
                        Log.e("PlayerController", "CS3IPlayer Playback error", event.error)
                    }
                    else -> {}
                }
            }
        )
    }

    fun initializePlayback(
        context: Context,
        media: MovieVortexMedia,
        episode: MediaEpisode? = null,
        offlineFilePath: String? = null
    ) {
        currentContext = context.applicationContext
        streamLoadingJob?.cancel()
        rawExtractorLinks.clear()
        rawSubtitleData.clear()

        val title = media.title
        val subTitle = if (episode != null) {
            "Season ${episode.seasonNumber} • Episode ${episode.episodeNumber}: ${episode.title}"
        } else {
            media.year?.let { "$it" } ?: ""
        }

        activeDataUrl = episode?.dataUrl ?: media.rawUrl
        activeApiName = media.providerApiName

        // 1. Offline playback
        if (offlineFilePath != null) {
            val uri = if (offlineFilePath.startsWith("content://")) {
                Uri.parse(offlineFilePath)
            } else {
                Uri.fromFile(File(offlineFilePath))
            }
            val extractorUri = ExtractorUri(
                uri = uri,
                name = title,
                id = offlineFilePath.hashCode()
            )
            val offlineSource = StreamSource(
                url = offlineFilePath,
                quality = "Offline (Original)",
                isTorrent = false
            )
            _uiState.value = PlayerUiState(
                status = PlayerStatus.Ready,
                mediaTitle = title,
                mediaSubtitle = subTitle,
                streamSources = listOf(offlineSource),
                currentSource = offlineSource,
                isOfflinePlayback = true
            )
            viewModelScope.launch(Dispatchers.Main) {
                cs3Player.loadPlayer(
                    context = context,
                    sameEpisode = false,
                    link = null,
                    data = extractorUri,
                    startPosition = 0L,
                    subtitles = emptySet(),
                    subtitle = null,
                    autoPlay = true,
                    preview = false
                )
            }
            return
        }

        // 2. Direct Stream or Live TV playback
        if (media.type == com.movievortex.android.model.MediaType.LiveStream ||
            activeApiName == "LiveTV" ||
            activeDataUrl.contains(".m3u8") ||
            activeDataUrl.endsWith(".mp4")) {
            viewModelScope.launch(Dispatchers.Main) {
                val liveLink = newExtractorLink(
                    source = activeApiName.ifBlank { "LiveTV" },
                    name = title,
                    url = activeDataUrl,
                    type = if (activeDataUrl.contains(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                )
                val directSource = CoreAdapter.fromExtractorLink(liveLink)
                rawExtractorLinks[directSource.url] = liveLink
                _uiState.value = PlayerUiState(
                    status = PlayerStatus.Ready,
                    mediaTitle = title,
                    mediaSubtitle = subTitle,
                    streamSources = listOf(directSource),
                    currentSource = directSource,
                    statusMessage = ""
                )
                cs3Player.loadPlayer(
                    context = context,
                    sameEpisode = false,
                    link = liveLink,
                    data = null,
                    startPosition = null,
                    subtitles = emptySet(),
                    subtitle = null,
                    autoPlay = true,
                    preview = false
                )
            }
            return
        }

        // 3. CloudStream Provider Stream Playback
        val lastPos = DataStoreHelper.getViewPos(activeDataUrl.hashCode())
        val resumeMs = lastPos?.position ?: 0L

        _uiState.value = PlayerUiState(
            status = PlayerStatus.ResolvingStreams,
            mediaTitle = title,
            mediaSubtitle = subTitle,
            currentPositionMs = resumeMs,
            statusMessage = "Resolving streams from provider..."
        )

        resolveStreamLinks(context, media, episode, resumeMs)
    }

    fun initializePlayback(
        media: MovieVortexMedia,
        episode: MediaEpisode? = null,
        offlineFilePath: String? = null
    ) {
        val ctx = currentContext ?: CloudStreamApp.context ?: return
        initializePlayback(ctx, media, episode, offlineFilePath)
    }

    private fun resolveStreamLinks(
        context: Context,
        media: MovieVortexMedia,
        targetEpisode: MediaEpisode?,
        resumeMs: Long
    ) {
        streamLoadingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = APIHolder.getApiFromNameNull(activeApiName)
                    ?: APIHolder.apis.firstOrNull { activeDataUrl.startsWith(it.mainUrl) }
                    ?: APIHolder.apis.firstOrNull()

                if (api == null) {
                    _uiState.value = _uiState.value.copy(
                        status = PlayerStatus.Error,
                        errorMessage = "This content is currently unavailable. Please try again."
                    )
                    return@launch
                }

                val repo = APIRepository(api)
                val res = repo.load(activeDataUrl)
                val loadResponse = (res as? Resource.Success)?.value

                val episodes: List<ResultEpisode> = when (loadResponse) {
                    is MovieLoadResponse -> listOf(
                        ResultEpisode(
                            headerName = loadResponse.name,
                            name = loadResponse.name,
                            poster = loadResponse.posterUrl,
                            episode = 1,
                            seasonIndex = null,
                            season = null,
                            data = loadResponse.dataUrl.ifBlank { activeDataUrl },
                            apiName = api.name,
                            id = (api.name + activeDataUrl).hashCode(),
                            index = 0,
                            position = 0L,
                            duration = 0L,
                            score = loadResponse.score,
                            description = loadResponse.plot,
                            isFiller = false,
                            tvType = TvType.Movie,
                            parentId = activeDataUrl.hashCode(),
                            videoWatchState = VideoWatchState.None
                        )
                    )
                    is TvSeriesLoadResponse -> loadResponse.episodes.mapIndexed { idx, ep ->
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
                            parentId = activeDataUrl.hashCode(),
                            videoWatchState = VideoWatchState.None
                        )
                    }
                    is AnimeLoadResponse -> loadResponse.episodes.values.flatten().mapIndexed { idx, ep ->
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
                            parentId = activeDataUrl.hashCode(),
                            videoWatchState = VideoWatchState.None
                        )
                    }
                    else -> emptyList()
                }

                if (episodes.isEmpty()) {
                    // Fallback direct loadLinks if load(url) didn't return episodes
                    repo.loadLinks(
                        data = activeDataUrl,
                        isCasting = false,
                        subtitleCallback = { subFile: SubtitleFile ->
                            val subTrack = CoreAdapter.fromSubtitleFile(subFile)
                            val currentList = _uiState.value.subtitleTracks.toMutableList()
                            if (!currentList.any { it.url == subTrack.url }) {
                                currentList.add(subTrack)
                                _uiState.value = _uiState.value.copy(subtitleTracks = currentList)
                            }
                        },
                        callback = { link: ExtractorLink ->
                            val streamSource = CoreAdapter.fromExtractorLink(link)
                            synchronized(rawExtractorLinks) {
                                rawExtractorLinks[streamSource.url] = link
                            }
                            if (_uiState.value.currentSource == null) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    _uiState.value = _uiState.value.copy(
                                        status = PlayerStatus.Ready,
                                        streamSources = listOf(streamSource),
                                        currentSource = streamSource,
                                        statusMessage = ""
                                    )
                                    cs3Player.loadPlayer(
                                        context = context,
                                        sameEpisode = false,
                                        link = link,
                                        data = null,
                                        startPosition = resumeMs,
                                        subtitles = emptySet(),
                                        subtitle = null,
                                        autoPlay = true,
                                        preview = true
                                    )
                                }
                            }
                        }
                    )
                    return@launch
                }

                val startIndex = when {
                    targetEpisode != null -> episodes.indexOfFirst { it.episode == targetEpisode.episodeNumber }.coerceAtLeast(0)
                    else -> 0
                }

                val generator = RepoLinkGenerator(episodes, page = loadResponse)
                val collectedSources = mutableListOf<StreamSource>()
                val collectedSubs = mutableSetOf<SubtitleData>()
                var hasStartedPlayback = false

                generator.generateLinks(
                    clearCache = false,
                    sourceTypes = setOf(ExtractorLinkType.M3U8, ExtractorLinkType.DASH, ExtractorLinkType.VIDEO, ExtractorLinkType.TORRENT),
                    callback = { (extractorLink, extractorUri) ->
                        if (extractorLink != null) {
                            val streamSource = CoreAdapter.fromExtractorLink(extractorLink)
                            synchronized(rawExtractorLinks) {
                                rawExtractorLinks[streamSource.url] = extractorLink
                            }
                            synchronized(collectedSources) {
                                if (!collectedSources.any { it.url == streamSource.url }) {
                                    collectedSources.add(streamSource)
                                }
                            }

                            if (!hasStartedPlayback) {
                                hasStartedPlayback = true
                                viewModelScope.launch(Dispatchers.Main) {
                                    _uiState.value = _uiState.value.copy(
                                        status = PlayerStatus.Ready,
                                        streamSources = collectedSources.toList(),
                                        currentSource = streamSource,
                                        statusMessage = ""
                                    )
                                    cs3Player.loadPlayer(
                                        context = context,
                                        sameEpisode = false,
                                        link = extractorLink,
                                        data = extractorUri,
                                        startPosition = resumeMs,
                                        subtitles = collectedSubs,
                                        subtitle = collectedSubs.firstOrNull(),
                                        autoPlay = true,
                                        preview = true
                                    )
                                }
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    streamSources = collectedSources.toList()
                                )
                            }
                        }
                    },
                    subtitleCallback = { subData ->
                        synchronized(collectedSubs) {
                            collectedSubs.add(subData)
                            rawSubtitleData[subData.url] = subData
                        }
                        val track = SubtitleTrack(language = subData.name, url = subData.url, format = "srt")
                        val currentList = _uiState.value.subtitleTracks.toMutableList()
                        if (!currentList.any { it.url == track.url }) {
                            currentList.add(track)
                            _uiState.value = _uiState.value.copy(subtitleTracks = currentList)
                        }
                    },
                    offset = startIndex,
                    isCasting = false
                )

                if (!hasStartedPlayback && collectedSources.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        status = PlayerStatus.Error,
                        errorMessage = "This content is currently unavailable. Please try again."
                    )
                }
            } catch (t: Throwable) {
                Log.e("PlayerController", "resolveStreamLinks error", t)
                _uiState.value = _uiState.value.copy(
                    status = PlayerStatus.Error,
                    errorMessage = "This content is currently unavailable. Please try again."
                )
            }
        }
    }

    fun selectSource(context: Context, source: StreamSource) {
        _uiState.value = _uiState.value.copy(currentSource = source)
        val link = rawExtractorLinks[source.url]
        if (link != null) {
            viewModelScope.launch(Dispatchers.Main) {
                cs3Player.loadPlayer(
                    context = context,
                    sameEpisode = true,
                    link = link,
                    data = null,
                    startPosition = cs3Player.getPosition() ?: _uiState.value.currentPositionMs,
                    subtitles = emptySet(),
                    subtitle = null,
                    autoPlay = true,
                    preview = true
                )
            }
        }
    }

    fun play() {
        cs3Player.handleEvent(CSPlayerEvent.Play)
    }

    fun pause() {
        cs3Player.handleEvent(CSPlayerEvent.Pause)
    }

    fun togglePlayPause() {
        cs3Player.handleEvent(CSPlayerEvent.PlayPauseToggle)
    }

    fun seekForward() {
        cs3Player.handleEvent(CSPlayerEvent.SeekForward)
    }

    fun seekBack() {
        cs3Player.handleEvent(CSPlayerEvent.SeekBack)
    }

    fun seekTo(positionMs: Long) {
        cs3Player.seekTo(positionMs)
    }

    fun selectSubtitle(subtitle: SubtitleTrack?) {
        val sub = subtitle?.let { rawSubtitleData[it.url] }
        cs3Player.setPreferredSubtitles(sub)
        _uiState.value = _uiState.value.copy(currentSubtitle = subtitle)
    }

    fun setPlaybackSpeed(speed: Float) {
        cs3Player.setPlaybackSpeed(speed)
    }

    fun retryPlayback() {
        val ctx = currentContext ?: CloudStreamApp.context ?: return
        if (activeDataUrl.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                status = PlayerStatus.ResolvingStreams,
                statusMessage = "Re-connecting to media stream...",
                errorMessage = null
            )
            resolveStreamLinks(ctx, MovieVortexMedia(id = activeDataUrl, title = _uiState.value.mediaTitle, poster = "", rawUrl = activeDataUrl, providerApiName = activeApiName), null, _uiState.value.currentPositionMs)
        }
    }

    fun updateProgress(posMs: Long, durMs: Long) {
        _uiState.value = _uiState.value.copy(
            currentPositionMs = posMs,
            durationMs = durMs
        )
        if (activeDataUrl.isNotBlank() && durMs > 0) {
            DataStoreHelper.setViewPos(activeDataUrl.hashCode(), posMs, durMs)
        }
    }

    fun toggleControlsVisibility() {
        _uiState.value = _uiState.value.copy(
            isControlsVisible = !_uiState.value.isControlsVisible
        )
    }

    fun releasePlayer() {
        streamLoadingJob?.cancel()
        try {
            cs3Player.release()
        } catch (_: Exception) {}
        _attachedPlayer.value = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}
