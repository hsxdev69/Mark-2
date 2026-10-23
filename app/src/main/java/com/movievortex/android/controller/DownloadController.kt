package com.movievortex.android.controller

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lagradost.cloudstream3.APIHolder
import com.lagradost.cloudstream3.AnimeLoadResponse
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.MovieLoadResponse
import com.lagradost.cloudstream3.TvSeriesLoadResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.mvvm.Resource
import com.lagradost.cloudstream3.ui.APIRepository
import com.lagradost.cloudstream3.ui.player.PlayerSubtitleHelper
import com.lagradost.cloudstream3.ui.player.SubtitleData
import com.lagradost.cloudstream3.ui.result.ResultEpisode
import com.lagradost.cloudstream3.ui.result.VideoWatchState
import com.lagradost.cloudstream3.utils.DOWNLOAD_EPISODE_CACHE
import com.lagradost.cloudstream3.utils.DOWNLOAD_HEADER_CACHE
import com.lagradost.cloudstream3.utils.DataStore.getKey
import com.lagradost.cloudstream3.utils.DataStore.getKeys
import com.lagradost.cloudstream3.utils.DataStore.setKey
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.downloader.DownloadObjects
import com.lagradost.cloudstream3.utils.downloader.DownloadQueueManager
import com.lagradost.cloudstream3.utils.downloader.VideoDownloadManager
import com.movievortex.android.model.MediaDownloadState
import com.movievortex.android.model.MediaEpisode
import com.movievortex.android.model.MediaType
import com.movievortex.android.model.MovieVortexMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

fun normalizeTitle(title: String): String {
    return title.lowercase()
        .replace(Regex("\\b(1080p|720p|480p|360p|4k|uhd|hd|sd|mp4|mkv|web-dl|bluray)\\b"), "")
        .replace(Regex("[^a-z0-9]"), "")
}

fun areSameItem(a: DownloadUiItem, b: DownloadUiItem): Boolean {
    if (a.id.isNotBlank() && b.id.isNotBlank() && a.id == b.id) return true
    if (!a.localFilePath.isNullOrBlank() && !b.localFilePath.isNullOrBlank() && a.localFilePath == b.localFilePath) return true
    val normA = normalizeTitle(a.title)
    val normB = normalizeTitle(b.title)
    return normA.isNotBlank() && normA == normB
}

fun mergeItems(existing: DownloadUiItem, update: DownloadUiItem): DownloadUiItem {
    val bestPoster = update.poster?.takeIf { it.isNotBlank() }
        ?: existing.poster?.takeIf { it.isNotBlank() }
    val bestPath = update.localFilePath?.takeIf { it.isNotBlank() }
        ?: existing.localFilePath?.takeIf { it.isNotBlank() }
    val bestQuality = if (update.quality.isNotBlank() && update.quality != "1080p") update.quality else existing.quality
    val isCompleted = update.state == MediaDownloadState.Completed ||
            existing.state == MediaDownloadState.Completed ||
            update.progress >= 1.0f ||
            existing.progress >= 1.0f

    val finalState = if (isCompleted) MediaDownloadState.Completed else update.state
    val finalProgress = if (isCompleted) 1.0f else maxOf(update.progress, existing.progress)
    val finalDownloaded = maxOf(update.downloadedBytes, existing.downloadedBytes)
    val finalTotal = maxOf(update.fileSizeBytes, existing.fileSizeBytes)

    return existing.copy(
        id = if (update.id.length < existing.id.length || existing.id.contains("-")) update.id else existing.id,
        title = if (update.title.length > existing.title.length) update.title else existing.title,
        poster = bestPoster,
        quality = bestQuality,
        fileSizeBytes = finalTotal,
        downloadedBytes = if (isCompleted && finalTotal > 0L) finalTotal else finalDownloaded,
        progress = finalProgress,
        speedBytesPerSec = if (isCompleted) 0L else update.speedBytesPerSec,
        state = finalState,
        localFilePath = bestPath,
        errorMessage = if (isCompleted) null else update.errorMessage ?: existing.errorMessage
    )
}

fun deduplicateList(list: List<DownloadUiItem>): List<DownloadUiItem> {
    val result = mutableListOf<DownloadUiItem>()
    for (item in list) {
        val existingIndex = result.indexOfFirst { areSameItem(it, item) }
        if (existingIndex >= 0) {
            result[existingIndex] = mergeItems(result[existingIndex], item)
        } else {
            result.add(item)
        }
    }
    return result
}

data class DownloadUiItem(
    val id: String,
    val title: String,
    val mediaType: MediaType = MediaType.Movie,
    val poster: String? = null,
    val quality: String = "1080p",
    val fileSizeBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val progress: Float = 0f, // 0.0 to 1.0 strictly calculated from actual transferred bytes
    val speedBytesPerSec: Long = 0L,
    val state: MediaDownloadState = MediaDownloadState.Queued,
    val localFilePath: String? = null,
    val errorMessage: String? = null,
    val originalMedia: MovieVortexMedia? = null,
    val originalEpisode: MediaEpisode? = null
)

data class DownloadsUiState(
    val items: List<DownloadUiItem> = emptyList(),
    val activeDownloadsCount: Int = 0,
    val totalDownloadedBytes: Long = 0L,
    val deviceAvailableBytes: Long = 0L,
    val deviceTotalBytes: Long = 0L
)

class DownloadController : ViewModel() {

    companion object {
        private const val TAG = "DownloadController"
        const val DOWNLOAD_DIR_NAME = "MovieVortex"
        const val DOWNLOAD_POSTER_CACHE = "vortex_download_posters"
        private var instance: DownloadController? = null

        fun registerExternalDownload(item: DownloadUiItem) {
            val controller = instance ?: return
            val norm = normalizeTitle(item.title)

            // Cache poster permanently across restarts
            val ctx = CloudStreamApp.context
            if (ctx != null && !item.poster.isNullOrBlank()) {
                try {
                    ctx.setKey(DOWNLOAD_POSTER_CACHE, item.id, item.poster)
                    ctx.setKey(DOWNLOAD_POSTER_CACHE, norm, item.poster)
                    ctx.setKey(DOWNLOAD_POSTER_CACHE, item.title.replace(Regex("[^a-zA-Z0-9.-]"), "_"), item.poster)
                } catch (_: Exception) {}
            }

            val current = controller._uiState.value
            val existingIndex = current.items.indexOfFirst { areSameItem(it, item) }
            val newItems = if (existingIndex >= 0) {
                current.items.mapIndexed { idx, existing ->
                    if (idx == existingIndex) mergeItems(existing, item) else existing
                }
            } else {
                listOf(item) + current.items
            }

            val deduped = deduplicateList(newItems)
            controller._uiState.value = current.copy(
                items = deduped,
                activeDownloadsCount = deduped.count { it.state == MediaDownloadState.Downloading }
            )
        }
    }

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    private val speedTracker = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, Long>>()
    private val speedMap = java.util.concurrent.ConcurrentHashMap<String, Long>()

    private val downloadProgressListener: (Triple<Int, Long, Long>) -> Unit = { (id, downloaded, total) ->
        updateProgressForQueueItem(id.toString(), downloaded, total)
    }

    private val downloadStatusListener: (Pair<Int, VideoDownloadManager.DownloadType>) -> Unit = { (id, status) ->
        handleQueueStatusChanged(id.toString(), status)
    }

    private val downloadDeleteListener: (Int) -> Unit = { id ->
        handleQueueItemDeleted(id.toString())
    }

    init {
        instance = this
        // Attach listeners to MovieVortex's internal downloader events
        VideoDownloadManager.downloadProgressEvent += downloadProgressListener
        VideoDownloadManager.downloadStatusEvent += downloadStatusListener
        VideoDownloadManager.downloadDeleteEvent += downloadDeleteListener
        loadDownloads()
    }

    override fun onCleared() {
        VideoDownloadManager.downloadProgressEvent -= downloadProgressListener
        VideoDownloadManager.downloadStatusEvent -= downloadStatusListener
        VideoDownloadManager.downloadDeleteEvent -= downloadDeleteListener
        super.onCleared()
    }

    /**
     * Verifies that a file is not an HTML error response or placeholder.
     */
    fun isHtmlOrInvalidMediaFile(file: File): Boolean {
        return try {
            if (!file.exists() || file.length() < 1024L) return true
            val buffer = ByteArray(512)
            val bytesRead = file.inputStream().use { it.read(buffer) }
            if (bytesRead <= 0) return true
            val headerStr = String(buffer, 0, bytesRead, Charsets.UTF_8).lowercase()
            headerStr.contains("<!doctype html") ||
                    headerStr.contains("<html") ||
                    headerStr.contains("<head") ||
                    headerStr.contains("<body") ||
                    headerStr.contains("{\"error\"") ||
                    headerStr.contains("404 not found") ||
                    headerStr.contains("403 forbidden")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Refreshes downloads list by scanning local files, reading internal download queue state,
     * and merging metadata with strict deduplication and persistent poster preservation.
     */
    fun loadDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            val ctx = CloudStreamApp.context
            val downloadItems = mutableListOf<DownloadUiItem>()
            val posterLookup = mutableMapOf<String, String>()

            // 0. Load all persisted posters from DataStore
            if (ctx != null) {
                try {
                    ctx.getKeys(DOWNLOAD_POSTER_CACHE)?.forEach { key ->
                        val poster = ctx.getKey<String>(key)
                        if (!poster.isNullOrBlank()) {
                            val subKey = key.substringAfterLast("/")
                            posterLookup[subKey] = poster
                            posterLookup[normalizeTitle(subKey)] = poster
                        }
                    }
                } catch (_: Exception) {}
            }

            // 1. Scan internal Download cache FIRST (so we get full title, poster, id)
            if (ctx != null) {
                try {
                    ctx.getKeys(DOWNLOAD_EPISODE_CACHE)?.forEach { key ->
                        val cachedEp = ctx.getKey<DownloadObjects.DownloadEpisodeCached>(key)
                        if (cachedEp != null) {
                            val info = VideoDownloadManager.getDownloadFileInfo(ctx, cachedEp.id)
                            val title = cachedEp.name ?: "Episode ${cachedEp.episode}"
                            val header = ctx.getKey<DownloadObjects.DownloadHeaderCached>(
                                DOWNLOAD_HEADER_CACHE,
                                cachedEp.parentId.toString()
                            )
                            val fullTitle = if (header != null && !title.startsWith(header.name)) "${header.name} - $title" else title
                            val norm = normalizeTitle(fullTitle)

                            val poster = cachedEp.poster
                                ?: header?.poster
                                ?: posterLookup[cachedEp.id.toString()]
                                ?: posterLookup[norm]

                            if (!poster.isNullOrBlank()) {
                                posterLookup[cachedEp.id.toString()] = poster
                                posterLookup[norm] = poster
                            }

                            val fileLength = info?.fileLength ?: 0L
                            val totalBytes = info?.totalBytes ?: 0L
                            val isDone = totalBytes > 0 && fileLength >= totalBytes
                            val prog = if (totalBytes > 0) (fileLength.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else (if (fileLength > 1024L) 1.0f else 0.05f)

                            val appDir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: ctx.filesDir
                            val cleanName = fullTitle.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                            val expectedFile = File(appDir, "$cleanName.mp4")
                            val resolvedPath = if (expectedFile.exists()) expectedFile.absolutePath else info?.path?.toString()

                            downloadItems.add(
                                DownloadUiItem(
                                    id = cachedEp.id.toString(),
                                    title = fullTitle,
                                    poster = poster,
                                    quality = "1080p",
                                    fileSizeBytes = if (totalBytes > 0) totalBytes else fileLength,
                                    downloadedBytes = fileLength,
                                    progress = if (isDone) 1.0f else prog,
                                    state = if (isDone) MediaDownloadState.Completed else MediaDownloadState.Downloading,
                                    localFilePath = resolvedPath
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading download cache", e)
                }
            }

            // 2. Scan app-specific private downloads directory for verified files
            try {
                val appDirs = listOfNotNull(
                    ctx?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    ctx?.filesDir
                )
                for (appDir in appDirs) {
                    if (appDir.exists() && appDir.isDirectory) {
                        appDir.walkTopDown().filter { file ->
                            file.isFile && (file.extension.equals("mp4", true) ||
                                    file.extension.equals("mkv", true) ||
                                    file.extension.equals("webm", true) ||
                                    file.extension.equals("avi", true))
                        }.forEach { file ->
                            if (!isHtmlOrInvalidMediaFile(file)) {
                                val displayTitle = file.nameWithoutExtension.replace("_", " ")
                                val norm = normalizeTitle(displayTitle)
                                val resolvedPoster = posterLookup[norm] ?: posterLookup[file.nameWithoutExtension]

                                downloadItems.add(
                                    DownloadUiItem(
                                        id = file.absolutePath.hashCode().toString(),
                                        title = displayTitle,
                                        poster = resolvedPoster,
                                        quality = "1080p",
                                        fileSizeBytes = file.length(),
                                        downloadedBytes = file.length(),
                                        progress = 1.0f,
                                        state = MediaDownloadState.Completed,
                                        localFilePath = file.absolutePath
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning app private downloads", e)
            }

            // Also check external storage MovieVortex downloads directory for backwards compatibility
            try {
                val dir = File("/storage/emulated/0/Download/$DOWNLOAD_DIR_NAME")
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.filter { file ->
                        file.isFile && (file.extension.equals("mp4", true) ||
                                file.extension.equals("mkv", true) ||
                                file.extension.equals("webm", true) ||
                                file.extension.equals("avi", true))
                    }?.forEach { file ->
                        if (!isHtmlOrInvalidMediaFile(file)) {
                            val displayTitle = file.nameWithoutExtension.replace("_", " ")
                            val norm = normalizeTitle(displayTitle)
                            val resolvedPoster = posterLookup[norm] ?: posterLookup[file.nameWithoutExtension]

                            downloadItems.add(
                                DownloadUiItem(
                                    id = file.absolutePath.hashCode().toString(),
                                    title = displayTitle,
                                    poster = resolvedPoster,
                                    quality = "1080p",
                                    fileSizeBytes = file.length(),
                                    downloadedBytes = file.length(),
                                    progress = 1.0f,
                                    state = MediaDownloadState.Completed,
                                    localFilePath = file.absolutePath
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning local downloads", e)
            }

            // 3. Active downloads from persistent queue & current active downloads
            try {
                val queue = DownloadQueueManager.queue.value
                queue.forEach { queueWrapper ->
                    val item = queueWrapper.downloadItem
                    val resumeEp = queueWrapper.resumePackage?.item?.ep
                    val id = queueWrapper.id
                    val title = item?.resultName ?: resumeEp?.mainName ?: resumeEp?.name ?: "Media Download"
                    val norm = normalizeTitle(title)
                    val poster = item?.resultPoster ?: resumeEp?.poster ?: posterLookup[id.toString()] ?: posterLookup[norm]

                    downloadItems.add(
                        DownloadUiItem(
                            id = id.toString(),
                            title = title,
                            poster = poster,
                            quality = "1080p",
                            fileSizeBytes = 0L,
                            downloadedBytes = 0L,
                            progress = 0f,
                            state = MediaDownloadState.Downloading
                        )
                    )
                }

                if (ctx != null) {
                    VideoDownloadManager.currentDownloads.value.forEach { activeId ->
                        val resume = ctx.getKey<DownloadObjects.DownloadResumePackage>(VideoDownloadManager.KEY_RESUME_PACKAGES, activeId.toString())
                        if (resume != null) {
                            val ep = resume.item.ep
                            val title = ep.mainName ?: ep.name ?: "Downloading Stream"
                            val norm = normalizeTitle(title)
                            val poster = ep.poster ?: posterLookup[activeId.toString()] ?: posterLookup[norm]

                            downloadItems.add(
                                DownloadUiItem(
                                    id = activeId.toString(),
                                    title = title,
                                    poster = poster,
                                    quality = "1080p",
                                    fileSizeBytes = 0L,
                                    downloadedBytes = 0L,
                                    progress = 0.05f,
                                    state = MediaDownloadState.Downloading
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading internal queue", e)
            }

            // 4. Merge existing in-memory items (preserving active download states & posters)
            val inMemoryItems = _uiState.value.items
            for (memItem in inMemoryItems) {
                downloadItems.add(memItem)
            }

            // 5. Strict deduplication: merges items with matching id, path, or normalized title!
            val deduplicated = deduplicateList(downloadItems)

            val (dlBytes, availBytes, totBytes) = calculateStorageStats(deduplicated)
            _uiState.value = DownloadsUiState(
                items = deduplicated,
                activeDownloadsCount = deduplicated.count { it.state == MediaDownloadState.Downloading },
                totalDownloadedBytes = dlBytes,
                deviceAvailableBytes = availBytes,
                deviceTotalBytes = totBytes
            )
        }
    }

    private fun calculateStorageStats(items: List<DownloadUiItem>): Triple<Long, Long, Long> {
        var downloadedBytes = 0L

        // 1. Scan external storage MovieVortex downloads directory
        try {
            val dir = File("/storage/emulated/0/Download/$DOWNLOAD_DIR_NAME")
            if (dir.exists() && dir.isDirectory) {
                dir.walkTopDown().filter { it.isFile }.forEach { file ->
                    downloadedBytes += file.length()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating download folder size", e)
        }

        // 2. Scan internal app-specific download directory
        try {
            val appDownloads = CloudStreamApp.context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (appDownloads != null && appDownloads.exists() && appDownloads.isDirectory) {
                appDownloads.walkTopDown().filter { it.isFile }.forEach { file ->
                    downloadedBytes += file.length()
                }
            }
        } catch (_: Exception) {}

        // 3. Compare with sum of downloadedBytes from UI items
        val itemsSum = items.sumOf { it.downloadedBytes.coerceAtLeast(0L) }
        val finalDownloadedBytes = maxOf(downloadedBytes, itemsSum)

        // 4. Device storage via StatFs
        var availableBytes = 0L
        var totalBytes = 0L
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            totalBytes = stat.blockCountLong * stat.blockSizeLong
        } catch (_: Exception) {
            try {
                val stat = StatFs(Environment.getExternalStorageDirectory().path)
                availableBytes = stat.availableBlocksLong * stat.blockSizeLong
                totalBytes = stat.blockCountLong * stat.blockSizeLong
            } catch (_: Exception) {}
        }

        return Triple(finalDownloadedBytes, availableBytes, totalBytes)
    }

    private fun updateProgressForQueueItem(id: String, downloaded: Long, total: Long) {
        val prog = if (total > 0L) (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
        val isCompleted = prog >= 1.0f || (total > 0L && downloaded >= total)

        val now = System.currentTimeMillis()
        val last = speedTracker[id]
        if (last != null) {
            val dt = (now - last.first) / 1000f
            if (dt >= 0.5f) {
                val dBytes = downloaded - last.second
                if (dBytes > 0) {
                    val spd = (dBytes / dt).toLong()
                    speedMap[id] = spd
                }
                speedTracker[id] = Pair(now, downloaded)
            }
        } else {
            speedTracker[id] = Pair(now, downloaded)
        }
        val currentSpeed = if (isCompleted) 0L else (speedMap[id] ?: 0L)

        val currentItems = _uiState.value.items
        val existingIndex = currentItems.indexOfFirst {
            it.id == id || it.id == (it.title.hashCode()).toString() || normalizeTitle(it.title) == normalizeTitle(id)
        }

        val updatedList = if (existingIndex >= 0) {
            currentItems.mapIndexed { idx, item ->
                if (idx == existingIndex) {
                    val newState = if (isCompleted) MediaDownloadState.Completed else MediaDownloadState.Downloading
                    item.copy(
                        downloadedBytes = if (isCompleted && total > 0L) total else downloaded,
                        fileSizeBytes = if (total > 0L) total else item.fileSizeBytes,
                        progress = if (isCompleted) 1.0f else prog,
                        speedBytesPerSec = currentSpeed,
                        state = newState
                    )
                } else item
            }
        } else {
            val ctx = CloudStreamApp.context
            val epCached = ctx?.getKey<DownloadObjects.DownloadEpisodeCached>(DOWNLOAD_EPISODE_CACHE, id)
                ?: ctx?.getKeys(DOWNLOAD_EPISODE_CACHE)?.firstNotNullOfOrNull { key ->
                    if (key.endsWith("/$id") || key == id) ctx.getKey<DownloadObjects.DownloadEpisodeCached>(key) else null
                }
            val headerName = epCached?.parentId?.let {
                ctx?.getKey<DownloadObjects.DownloadHeaderCached>(DOWNLOAD_HEADER_CACHE, it.toString())?.name
            }
            val title = epCached?.name ?: headerName ?: "Downloading Video"
            val poster = epCached?.poster
                ?: ctx?.getKey<String>(DOWNLOAD_POSTER_CACHE, id)
                ?: ctx?.getKey<String>(DOWNLOAD_POSTER_CACHE, normalizeTitle(title))

            val newItem = DownloadUiItem(
                id = id,
                title = title,
                poster = poster,
                quality = "1080p",
                downloadedBytes = if (isCompleted && total > 0L) total else downloaded,
                fileSizeBytes = total,
                progress = if (isCompleted) 1.0f else prog,
                speedBytesPerSec = currentSpeed,
                state = if (isCompleted) MediaDownloadState.Completed else MediaDownloadState.Downloading
            )
            listOf(newItem) + currentItems
        }

        val finalItems = deduplicateList(updatedList)
        _uiState.value = _uiState.value.copy(
            items = finalItems,
            activeDownloadsCount = finalItems.count { it.state == MediaDownloadState.Downloading }
        )
    }

    private fun handleQueueStatusChanged(id: String, status: VideoDownloadManager.DownloadType) {
        val updated = _uiState.value.items.map { item ->
            if (item.id == id || item.title.hashCode().toString() == id || normalizeTitle(item.title) == normalizeTitle(id)) {
                when (status) {
                    VideoDownloadManager.DownloadType.IsDone -> {
                        item.copy(
                            state = MediaDownloadState.Completed,
                            progress = 1.0f,
                            downloadedBytes = if (item.fileSizeBytes > 0) item.fileSizeBytes else item.downloadedBytes,
                            speedBytesPerSec = 0L,
                            errorMessage = null
                        )
                    }
                    VideoDownloadManager.DownloadType.IsPaused -> {
                        item.copy(state = MediaDownloadState.Paused, speedBytesPerSec = 0L)
                    }
                    VideoDownloadManager.DownloadType.IsFailed -> {
                        item.copy(
                            state = MediaDownloadState.Failed,
                            speedBytesPerSec = 0L,
                            errorMessage = "Stream download failed from provider."
                        )
                    }
                    VideoDownloadManager.DownloadType.IsStopped -> {
                        item.copy(state = MediaDownloadState.Cancelled, speedBytesPerSec = 0L)
                    }
                    VideoDownloadManager.DownloadType.IsDownloading -> {
                        item.copy(state = MediaDownloadState.Downloading)
                    }
                    else -> item
                }
            } else item
        }
        val finalItems = deduplicateList(updated)
        _uiState.value = _uiState.value.copy(
            items = finalItems,
            activeDownloadsCount = finalItems.count { it.state == MediaDownloadState.Downloading }
        )
    }

    private fun handleQueueItemDeleted(id: String) {
        speedTracker.remove(id)
        speedMap.remove(id)
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.filter { it.id != id },
            activeDownloadsCount = _uiState.value.items.count { it.state == MediaDownloadState.Downloading }
        )
        loadDownloads()
    }

    fun pauseDownload(id: String) {
        val intId = id.toIntOrNull()
        if (intId != null) {
            VideoDownloadManager.downloadEvent.invoke(intId to VideoDownloadManager.DownloadActionType.Pause)
        }
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map {
                if (it.id == id) it.copy(state = MediaDownloadState.Paused) else it
            },
            activeDownloadsCount = _uiState.value.items.count { it.state == MediaDownloadState.Downloading }
        )
    }

    fun resumeDownload(id: String) {
        val intId = id.toIntOrNull()
        if (intId != null) {
            VideoDownloadManager.downloadEvent.invoke(intId to VideoDownloadManager.DownloadActionType.Resume)
        }
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map {
                if (it.id == id) it.copy(state = MediaDownloadState.Downloading) else it
            },
            activeDownloadsCount = _uiState.value.items.count { it.state == MediaDownloadState.Downloading }
        )
    }

    fun retryDownload(context: Context, item: DownloadUiItem) {
        val media = item.originalMedia
        if (media != null) {
            _uiState.value = _uiState.value.copy(
                items = _uiState.value.items.filter { it.id != item.id }
            )
            downloadMedia(context, media, item.originalEpisode)
        } else {
            Toast.makeText(context, "Cannot retry: original source details missing.", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteDownload(id: String) {
        val itemToDelete = _uiState.value.items.find { it.id == id }

        val intId = id.toIntOrNull()
        if (intId != null) {
            try {
                DownloadQueueManager.cancelDownload(intId)
            } catch (e: Exception) {
                Log.w(TAG, "Error cancelling download for id $id: ${e.message}")
            }
        }

        // Delete physical file if present
        if (itemToDelete?.localFilePath != null) {
            try {
                if (itemToDelete.localFilePath.startsWith("content://")) {
                    CloudStreamApp.context?.contentResolver?.delete(Uri.parse(itemToDelete.localFilePath), null, null)
                } else {
                    val f = File(itemToDelete.localFilePath)
                    if (f.exists()) f.delete()
                }
            } catch (_: Exception) {}
        }

        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.filter { it.id != id },
            activeDownloadsCount = _uiState.value.items.count { it.state == MediaDownloadState.Downloading }
        )
        loadDownloads()
    }

    /**
     * Executes real in-app download pipeline:
     * Media Selection -> Provider Resolution -> Stream Validation -> In-App Queue -> Verification.
     * Keeps all downloads and notifications strictly inside MovieVortex without invoking external apps.
     */
    fun downloadMedia(
        context: Context,
        media: MovieVortexMedia,
        episode: MediaEpisode? = null,
        streamUrl: String? = null
    ) {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)

        // 1. Enforce Wi-Fi Only setting
        val wifiOnly = prefs.getBoolean("pref_download_wifi_only", true)
        if (wifiOnly) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val isWifi = cm?.getNetworkCapabilities(cm.activeNetwork)?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true
            if (!isWifi) {
                Toast.makeText(context, "Wi-Fi Only is enabled in Settings. Connect to Wi-Fi to download.", Toast.LENGTH_LONG).show()
                return
            }
        }

        val title = if (episode != null) {
            "${media.title} S${episode.seasonNumber}E${episode.episodeNumber}"
        } else {
            media.title
        }
        val cleanName = title.replace(Regex("[^a-zA-Z0-9.-]"), "_")

        // 2. Use configured download location (Default to app-private internal storage, no external folders)
        val customLoc = prefs.getString("pref_download_location", null)
        val appDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        val baseDir = if (!customLoc.isNullOrBlank()) File(customLoc) else appDir
        if (!baseDir.exists()) baseDir.mkdirs()
        val localPath = File(baseDir, "$cleanName.mp4").absolutePath

        // 1. Duplicate check: Already downloaded and verified
        val targetFile = File(localPath)
        if (targetFile.exists() && !isHtmlOrInvalidMediaFile(targetFile)) {
            Toast.makeText(context, "Already downloaded and available offline!", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Duplicate check: Already actively downloading
        if (_uiState.value.items.any { (it.title == title || it.localFilePath == localPath) && it.state == MediaDownloadState.Downloading }) {
            Toast.makeText(context, "Download already in progress for: $title", Toast.LENGTH_SHORT).show()
            return
        }

        val newId = System.currentTimeMillis().toString()
        val newItem = DownloadUiItem(
            id = newId,
            title = title,
            mediaType = media.type,
            poster = episode?.poster ?: media.poster,
            quality = "1080p",
            fileSizeBytes = 0L,
            downloadedBytes = 0L,
            progress = 0f,
            state = MediaDownloadState.Queued,
            localFilePath = localPath,
            originalMedia = media,
            originalEpisode = episode
        )

        _uiState.value = _uiState.value.copy(
            items = listOf(newItem) + _uiState.value.items.filter { it.title != title },
            activeDownloadsCount = _uiState.value.activeDownloadsCount + 1
        )

        Toast.makeText(context, "Resolving video links for: $title...", Toast.LENGTH_SHORT).show()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = APIHolder.getApiFromNameNull(media.providerApiName)
                    ?: APIHolder.apis.firstOrNull { media.rawUrl.startsWith(it.mainUrl) }
                    ?: APIHolder.apis.firstOrNull()

                if (api == null) {
                    val errMsg = "Provider '${media.providerApiName}' is not loaded or active."
                    Log.e(TAG, errMsg)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                        updateItemState(newId, MediaDownloadState.Failed, errMsg)
                    }
                    return@launch
                }

                val repo = APIRepository(api)

                // Resolve real media dataUrl
                var targetDataUrl = episode?.dataUrl
                if (targetDataUrl.isNullOrBlank() && media.rawUrl.isNotBlank()) {
                    val loadRes = repo.load(media.rawUrl)
                    if (loadRes is Resource.Success) {
                        targetDataUrl = when (val resVal = loadRes.value) {
                            is MovieLoadResponse -> resVal.dataUrl.ifBlank { media.rawUrl }
                            is TvSeriesLoadResponse -> resVal.episodes.firstOrNull()?.data ?: media.rawUrl
                            is AnimeLoadResponse -> resVal.episodes.values.flatten().firstOrNull()?.data ?: media.rawUrl
                            else -> media.rawUrl
                        }
                    }
                }
                if (targetDataUrl.isNullOrBlank()) {
                    targetDataUrl = media.rawUrl
                }

                val extractedLinks = mutableListOf<ExtractorLink>()
                val extractedSubs = mutableListOf<SubtitleData>()

                if (targetDataUrl.isNotBlank()) {
                    repo.loadLinks(
                        data = targetDataUrl,
                        isCasting = false,
                        subtitleCallback = { sub ->
                            extractedSubs.add(PlayerSubtitleHelper.getSubtitleData(sub))
                        },
                        callback = { link ->
                            val urlLower = link.url.lowercase()
                            if (link.url.isNotBlank() &&
                                (urlLower.startsWith("http://") || urlLower.startsWith("https://")) &&
                                !urlLower.contains(".html") && !urlLower.contains("/embed/")
                            ) {
                                extractedLinks.add(link)
                            }
                        }
                    )
                }

                if (extractedLinks.isEmpty() && streamUrl.isNullOrBlank()) {
                    val errMsg = "No downloadable media stream found for $title."
                    Log.w(TAG, errMsg)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                        updateItemState(newId, MediaDownloadState.Failed, errMsg)
                    }
                    return@launch
                }

                val epNumber = episode?.episodeNumber ?: 1
                val seasonNumber = episode?.seasonNumber ?: 1
                val tvType = if (media.type == MediaType.Movie) TvType.Movie else TvType.TvSeries

                val resultEp = ResultEpisode(
                    headerName = media.title,
                    name = episode?.title ?: media.title,
                    poster = episode?.poster ?: media.poster,
                    episode = epNumber,
                    seasonIndex = seasonNumber,
                    season = seasonNumber,
                    data = targetDataUrl,
                    apiName = media.providerApiName,
                    id = (media.rawUrl + targetDataUrl).hashCode(),
                    index = epNumber - 1,
                    position = 0L,
                    duration = 0L,
                    score = null,
                    description = media.synopsis,
                    isFiller = false,
                    tvType = tvType,
                    parentId = media.rawUrl.hashCode(),
                    videoWatchState = VideoWatchState.None
                )

                // Sort extracted links based on configured download quality preference
                val qualityPref = prefs.getString("pref_download_quality", "Highest (1080p)") ?: "Highest (1080p)"
                val sortedLinks = when {
                    qualityPref.contains("Highest", true) -> extractedLinks.sortedByDescending { it.quality }
                    qualityPref.contains("720", true) -> extractedLinks.sortedBy { kotlin.math.abs(it.quality - 720) }
                    qualityPref.contains("480", true) -> extractedLinks.sortedBy { it.quality }
                    else -> extractedLinks
                }

                // Queue into MovieVortex's internal in-app downloader
                val queueItem = DownloadObjects.DownloadQueueItem(
                    episode = resultEp,
                    isMovie = media.type == MediaType.Movie,
                    resultName = media.title,
                    resultType = tvType,
                    resultPoster = media.poster,
                    apiName = media.providerApiName,
                    resultId = media.rawUrl.hashCode(),
                    resultUrl = media.rawUrl,
                    links = if (sortedLinks.isNotEmpty()) sortedLinks else null,
                    subs = if (extractedSubs.isNotEmpty()) extractedSubs else null
                )

                DownloadQueueManager.addToQueue(queueItem.toWrapper())
                updateItemState(newId, MediaDownloadState.Downloading, null)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "MovieVortex download started: $title", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val errMsg = "Download failed: ${e.message ?: "Unknown error"}"
                Log.e(TAG, "Download resolution error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                    updateItemState(newId, MediaDownloadState.Failed, errMsg)
                }
            }
        }
    }

    private fun updateItemState(id: String, state: MediaDownloadState, error: String?) {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map {
                if (it.id == id) it.copy(state = state, errorMessage = error) else it
            },
            activeDownloadsCount = _uiState.value.items.count { it.state == MediaDownloadState.Downloading }
        )
    }
}
