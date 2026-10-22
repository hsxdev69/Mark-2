package com.movievortex.android.controller

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.lagradost.cloudstream3.APIHolder
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.utils.DataStoreHelper
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.ui.subtitles.SubtitlesFragment
import com.lagradost.cloudstream3.ui.subtitles.SubtitlesFragment.Companion.saveStyle
import com.movievortex.android.core.ServerManager
import com.movievortex.android.core.VortexPluginBootstrap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class DnsOption(
    val id: Int,
    val name: String,
    val provider: String,
    val ips: String,
    val description: String
)

data class SourceItem(
    val name: String,
    val language: String,
    val isOnline: Boolean,
    val supportedTypes: String
)

data class SettingsUiState(
    // Retained for DnsIntroScreen compatibility
    val selectedDnsId: Int = 2,

    // 1. PLAYBACK
    val defaultPlaybackQuality: String = "1080p FHD",
    val autoPlayNext: Boolean = true,
    val resumePlayback: Boolean = true,
    val autoFullScreen: Boolean = false,

    // 2. SUBTITLES
    val subtitleStyle: String = "Classic White",
    val subtitleSize: String = "Normal (100%)",

    // 3. DOWNLOADS
    val downloadQuality: String = "Highest (1080p)",
    val downloadWifiOnly: Boolean = true,
    val downloadLocation: String = "",

    // 4. SOURCES & SERVERS
    val defaultServer: String = ServerManager.DEFAULT_SERVER,
    val connectionStatus: String = "Checking...",
    val isTestingConnection: Boolean = false,
    val installedSources: List<SourceItem> = emptyList(),

    // 5. APPEARANCE
    val appTheme: String = "Dark (OLED Pure Black)",
    val accentColor: String = "Pure White (Cinematic)",
    val appLanguage: String = "English",

    // 6. NETWORK & DATA
    val dataSaver: Boolean = false,
    val streamingQuality: String = "Adaptive (Recommended)",
    val mobileDataAllowed: Boolean = true,

    // 7. STORAGE & CACHE
    val cacheSizeMb: String = "0.0 MB",
    val storageFreeGb: String = "0.0 GB",
    val storageTotalGb: String = "0.0 GB",
    val downloadsSizeMb: String = "0.0 MB",

    // 9. NOTIFICATIONS
    val downloadNotifications: Boolean = true,
    val playbackAlerts: Boolean = true,

    // 10. ABOUT
    val appVersion: String = "v2.0.0-PRO (Build 20261021)"
)

class SettingsController(private val context: Context) : ViewModel() {

    private val settingsManager = PreferenceManager.getDefaultSharedPreferences(context)
    private val dnsKey = context.getString(R.string.dns_key)

    // SharedPreferences Keys for Persistence
    companion object {
        private const val PREF_DEFAULT_PLAYBACK_QUALITY = "pref_default_playback_quality"
        private const val PREF_AUTO_PLAY_NEXT = "pref_auto_play_next"
        private const val PREF_RESUME_PLAYBACK = "pref_resume_playback"
        private const val PREF_AUTO_FULLSCREEN = "pref_auto_fullscreen"

        private const val PREF_PREFERRED_AUDIO_LANG = "pref_preferred_audio_lang"
        private const val PREF_SUBTITLE_STYLE = "pref_subtitle_style"
        private const val PREF_SUBTITLE_SIZE = "pref_subtitle_size"

        private const val PREF_DOWNLOAD_QUALITY = "pref_download_quality"
        private const val PREF_DOWNLOAD_WIFI_ONLY = "pref_download_wifi_only"
        private const val PREF_DOWNLOAD_LOCATION = "pref_download_location"

        private const val PREF_DEFAULT_SERVER = "pref_default_server"

        private const val PREF_APP_THEME = "pref_app_theme"
        private const val PREF_ACCENT_COLOR = "pref_accent_color"
        private const val PREF_APP_LANGUAGE = "pref_app_language"

        private const val PREF_DATA_SAVER = "pref_data_saver"
        private const val PREF_STREAMING_QUALITY = "pref_streaming_quality"
        private const val PREF_MOBILE_DATA_ALLOWED = "pref_mobile_data_allowed"

        private const val PREF_DOWNLOAD_NOTIFICATIONS = "pref_download_notifications"
        private const val PREF_PLAYBACK_ALERTS = "pref_playback_alerts"
    }

    val dnsOptions = listOf(
        DnsOption(2, "Cloudflare 1.1.1.1", "Cloudflare", "1.1.1.1 / 1.0.0.1", "Fastest worldwide DoH resolver with privacy protection"),
        DnsOption(1, "Google Public DNS", "Google", "8.8.8.8 / 8.8.4.4", "Reliable and high-capacity global DNS infrastructure"),
        DnsOption(4, "AdGuard DNS", "AdGuard", "94.140.14.140", "Blocks annoying trackers and malicious ads automatically"),
        DnsOption(6, "Quad9 DNS", "Quad9", "9.9.9.9", "Enhanced security with automated threat blocking"),
        DnsOption(0, "System Default", "ISP / Network", "Auto", "Uses standard system network resolver")
    )

    private val defaultDownloadDir: String
        get() = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
            ?: File(context.filesDir, "downloads").absolutePath

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            selectedDnsId = settingsManager.getInt(dnsKey, 2),

            // 1. Playback
            defaultPlaybackQuality = settingsManager.getString(PREF_DEFAULT_PLAYBACK_QUALITY, "1080p FHD") ?: "1080p FHD",
            autoPlayNext = settingsManager.getBoolean(PREF_AUTO_PLAY_NEXT, true),
            resumePlayback = settingsManager.getBoolean(PREF_RESUME_PLAYBACK, true),
            autoFullScreen = settingsManager.getBoolean(PREF_AUTO_FULLSCREEN, false),

            // 2. Subtitles
            subtitleStyle = settingsManager.getString(PREF_SUBTITLE_STYLE, "Classic White") ?: "Classic White",
            subtitleSize = settingsManager.getString(PREF_SUBTITLE_SIZE, "Normal (100%)") ?: "Normal (100%)",

            // 3. Downloads
            downloadQuality = settingsManager.getString(PREF_DOWNLOAD_QUALITY, "Highest (1080p)") ?: "Highest (1080p)",
            downloadWifiOnly = settingsManager.getBoolean(PREF_DOWNLOAD_WIFI_ONLY, true),
            downloadLocation = settingsManager.getString(PREF_DOWNLOAD_LOCATION, defaultDownloadDir) ?: defaultDownloadDir,

            // 4. Sources & Servers
            defaultServer = settingsManager.getString(PREF_DEFAULT_SERVER, ServerManager.DEFAULT_SERVER) ?: ServerManager.DEFAULT_SERVER,

            // 5. Appearance
            appTheme = settingsManager.getString(PREF_APP_THEME, "Dark (OLED Pure Black)") ?: "Dark (OLED Pure Black)",
            accentColor = settingsManager.getString(PREF_ACCENT_COLOR, "Pure White (Cinematic)") ?: "Pure White (Cinematic)",
            appLanguage = settingsManager.getString(PREF_APP_LANGUAGE, "English") ?: "English",

            // 6. Network & Data
            dataSaver = settingsManager.getBoolean(PREF_DATA_SAVER, false),
            streamingQuality = settingsManager.getString(PREF_STREAMING_QUALITY, "Adaptive (Recommended)") ?: "Adaptive (Recommended)",
            mobileDataAllowed = settingsManager.getBoolean(PREF_MOBILE_DATA_ALLOWED, true),

            // 9. Notifications
            downloadNotifications = settingsManager.getBoolean(PREF_DOWNLOAD_NOTIFICATIONS, true),
            playbackAlerts = settingsManager.getBoolean(PREF_PLAYBACK_ALERTS, true)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        calculateStorageAndCache()
        loadInstalledSources()
        testConnection()
    }

    // Retained for DnsIntroScreen
    fun setDns(dnsId: Int) {
        settingsManager.edit().putInt(dnsKey, dnsId).apply()
        _uiState.value = _uiState.value.copy(selectedDnsId = dnsId)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. PLAYBACK
    // ─────────────────────────────────────────────────────────────────────────
    fun setDefaultPlaybackQuality(quality: String) {
        settingsManager.edit().putString(PREF_DEFAULT_PLAYBACK_QUALITY, quality).apply()
        val qualityInt = when {
            quality.contains("4K", true) || quality.contains("2160", true) -> Qualities.P2160.value
            quality.contains("1080", true) -> Qualities.P1080.value
            quality.contains("720", true) -> Qualities.P720.value
            quality.contains("480", true) -> Qualities.P480.value
            else -> Qualities.P1080.value
        }
        try {
            settingsManager.edit().putInt(context.getString(R.string.quality_pref_key), qualityInt).apply()
        } catch (e: Exception) {
            Log.e("SettingsController", "Failed to write quality_pref_key", e)
        }
        _uiState.value = _uiState.value.copy(defaultPlaybackQuality = quality)
    }

    fun setAutoPlayNext(enabled: Boolean) {
        settingsManager.edit().putBoolean(PREF_AUTO_PLAY_NEXT, enabled).apply()
        try {
            settingsManager.edit().putBoolean(context.getString(R.string.autoplay_next_key), enabled).apply()
        } catch (e: Exception) {
            Log.e("SettingsController", "Failed to write autoplay_next_key", e)
        }
        _uiState.value = _uiState.value.copy(autoPlayNext = enabled)
    }

    fun setResumePlayback(enabled: Boolean) {
        settingsManager.edit().putBoolean(PREF_RESUME_PLAYBACK, enabled).apply()
        _uiState.value = _uiState.value.copy(resumePlayback = enabled)
    }

    fun setAutoFullScreen(enabled: Boolean) {
        settingsManager.edit().putBoolean(PREF_AUTO_FULLSCREEN, enabled).apply()
        _uiState.value = _uiState.value.copy(autoFullScreen = enabled)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. SUBTITLES
    // ─────────────────────────────────────────────────────────────────────────
    fun setSubtitleStyle(style: String) {
        settingsManager.edit().putString(PREF_SUBTITLE_STYLE, style).apply()
        _uiState.value = _uiState.value.copy(subtitleStyle = style)
        try {
            val (fgColor, bgColor) = when (style) {
                "Yellow on Black" -> Pair(android.graphics.Color.YELLOW, android.graphics.Color.BLACK)
                "Translucent Background" -> Pair(android.graphics.Color.WHITE, android.graphics.Color.argb(128, 0, 0, 0))
                "High Contrast Black Box" -> Pair(android.graphics.Color.WHITE, android.graphics.Color.BLACK)
                else -> Pair(android.graphics.Color.WHITE, android.graphics.Color.TRANSPARENT)
            }
            val current = SubtitlesFragment.getCurrentSavedStyle()
            val newStyle = current.copy(
                foregroundColor = fgColor,
                backgroundColor = bgColor
            )
            with(SubtitlesFragment) {
                context.saveStyle(newStyle)
            }
        } catch (e: Exception) {
            Log.e("SettingsController", "Error applying subtitle style", e)
        }
    }

    fun setSubtitleSize(size: String) {
        settingsManager.edit().putString(PREF_SUBTITLE_SIZE, size).apply()
        _uiState.value = _uiState.value.copy(subtitleSize = size)
        try {
            val scale = when {
                size.contains("75") -> 0.75f
                size.contains("125") -> 1.25f
                size.contains("150") -> 1.50f
                else -> 1.0f
            }
            val current = SubtitlesFragment.getCurrentSavedStyle()
            val newStyle = current.copy(
                elevation = (scale * 20).toInt()
            )
            with(SubtitlesFragment) {
                context.saveStyle(newStyle)
            }
        } catch (e: Exception) {
            Log.e("SettingsController", "Error applying subtitle size", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. DOWNLOADS
    // ─────────────────────────────────────────────────────────────────────────
    fun setDownloadQuality(quality: String) {
        settingsManager.edit().putString(PREF_DOWNLOAD_QUALITY, quality).apply()
        _uiState.value = _uiState.value.copy(downloadQuality = quality)
    }

    fun setDownloadWifiOnly(wifiOnly: Boolean) {
        settingsManager.edit().putBoolean(PREF_DOWNLOAD_WIFI_ONLY, wifiOnly).apply()
        _uiState.value = _uiState.value.copy(downloadWifiOnly = wifiOnly)
    }

    fun setDownloadLocation(path: String) {
        settingsManager.edit().putString(PREF_DOWNLOAD_LOCATION, path).apply()
        _uiState.value = _uiState.value.copy(downloadLocation = path)
    }

    fun resetDownloadLocation() {
        setDownloadLocation(defaultDownloadDir)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. SOURCES & SERVERS
    // ─────────────────────────────────────────────────────────────────────────
    fun setDefaultServer(server: String) {
        settingsManager.edit().putString(PREF_DEFAULT_SERVER, server).apply()
        _uiState.value = _uiState.value.copy(defaultServer = server)
    }

    fun loadInstalledSources() {
        viewModelScope.launch(Dispatchers.IO) {
            val apis = APIHolder.apis
            val apiSources = apis.map { api ->
                val markName = ServerManager.toDisplayName(api.name)
                SourceItem(
                    name = markName,
                    language = api.lang.uppercase(Locale.US),
                    isOnline = true,
                    supportedTypes = ServerManager.toDescription(markName)
                )
            }

            // Ensure all 4 Mark servers are represented
            val finalSources = mutableListOf<SourceItem>()
            ServerManager.ALL_SERVERS.forEach { serverName ->
                val existing = apiSources.firstOrNull { it.name == serverName }
                if (existing != null) {
                    finalSources.add(existing)
                } else {
                    finalSources.add(
                        SourceItem(
                            name = serverName,
                            language = "EN",
                            isOnline = true,
                            supportedTypes = ServerManager.toDescription(serverName)
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(installedSources = finalSources)
            }
        }
    }

    fun refreshSources(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                VortexPluginBootstrap.bootstrap(activity)
                loadInstalledSources()
            } catch (e: Exception) {
                Log.e("SettingsController", "Failed to refresh plugins", e)
            }
        }
    }

    fun testConnection() {
        _uiState.value = _uiState.value.copy(isTestingConnection = true, connectionStatus = "Testing DoH Ping...")
        viewModelScope.launch(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            var status = "Offline • No Internet"
            try {
                val url = URL("https://1.1.1.1")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.requestMethod = "HEAD"
                val code = conn.responseCode
                val latency = System.currentTimeMillis() - start
                if (code in 200..399) {
                    status = "Connected (1.1.1.1 DoH) • ${latency}ms"
                }
                conn.disconnect()
            } catch (_: Exception) {
                try {
                    val url2 = URL("https://www.google.com/generate_204")
                    val conn2 = url2.openConnection() as HttpURLConnection
                    conn2.connectTimeout = 3000
                    conn2.readTimeout = 3000
                    val latency = System.currentTimeMillis() - start
                    if (conn2.responseCode == 204) {
                        status = "Connected • ${latency}ms"
                    }
                    conn2.disconnect()
                } catch (_: Exception) {
                    status = "Offline • Connection Failed"
                }
            }
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionStatus = status
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. APPEARANCE
    // ─────────────────────────────────────────────────────────────────────────
    fun setAppTheme(theme: String) {
        settingsManager.edit().putString(PREF_APP_THEME, theme).apply()
        _uiState.value = _uiState.value.copy(appTheme = theme)
    }

    fun setAccentColor(color: String) {
        settingsManager.edit().putString(PREF_ACCENT_COLOR, color).apply()
        _uiState.value = _uiState.value.copy(accentColor = color)
    }

    fun setAppLanguage(language: String) {
        settingsManager.edit().putString(PREF_APP_LANGUAGE, language).apply()
        _uiState.value = _uiState.value.copy(appLanguage = language)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. NETWORK & DATA
    // ─────────────────────────────────────────────────────────────────────────
    fun setDataSaver(enabled: Boolean) {
        settingsManager.edit().putBoolean(PREF_DATA_SAVER, enabled).apply()
        _uiState.value = _uiState.value.copy(dataSaver = enabled)
    }

    fun setStreamingQuality(quality: String) {
        settingsManager.edit().putString(PREF_STREAMING_QUALITY, quality).apply()
        _uiState.value = _uiState.value.copy(streamingQuality = quality)
    }

    fun setMobileDataAllowed(allowed: Boolean) {
        settingsManager.edit().putBoolean(PREF_MOBILE_DATA_ALLOWED, allowed).apply()
        _uiState.value = _uiState.value.copy(mobileDataAllowed = allowed)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. STORAGE & CACHE
    // ─────────────────────────────────────────────────────────────────────────
    fun calculateStorageAndCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val cacheBytes = getFolderSize(context.cacheDir) + (context.externalCacheDir?.let { getFolderSize(it) } ?: 0L)
            val cacheStr = formatBytes(cacheBytes)

            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.filesDir, "downloads")
            var downloadBytes = getFolderSize(downloadDir)
            val publicDir = File("/storage/emulated/0/Download/MovieVortex")
            if (publicDir.exists()) {
                downloadBytes += getFolderSize(publicDir)
            }
            val downloadStr = formatBytes(downloadBytes)

            var freeStr = "Unknown"
            var totalStr = "Unknown"
            try {
                val stat = StatFs(Environment.getDataDirectory().path)
                val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
                val totalBytes = stat.blockCountLong * stat.blockSizeLong
                freeStr = String.format(Locale.US, "%.1f GB Free", freeBytes.toDouble() / (1024 * 1024 * 1024))
                totalStr = String.format(Locale.US, "%.1f GB Total", totalBytes.toDouble() / (1024 * 1024 * 1024))
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    cacheSizeMb = cacheStr,
                    downloadsSizeMb = downloadStr,
                    storageFreeGb = freeStr,
                    storageTotalGb = totalStr
                )
            }
        }
    }

    fun clearCache(): Boolean {
        return try {
            context.cacheDir.deleteRecursively()
            context.externalCacheDir?.deleteRecursively()
            _uiState.value = _uiState.value.copy(cacheSizeMb = "0.0 MB")
            calculateStorageAndCache()
            true
        } catch (e: Exception) {
            Log.e("SettingsController", "Failed to clear cache", e)
            false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. PRIVACY
    // ─────────────────────────────────────────────────────────────────────────
    fun clearSearchHistory(): Boolean {
        return try {
            CloudStreamApp.removeKeys("search_history")
            true
        } catch (e: Exception) {
            Log.e("SettingsController", "Failed to clear search history", e)
            false
        }
    }

    fun clearWatchHistory(): Boolean {
        return try {
            DataStoreHelper.deleteAllResumeStateIds()
            true
        } catch (e: Exception) {
            Log.e("SettingsController", "Failed to clear watch history", e)
            false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. NOTIFICATIONS
    // ─────────────────────────────────────────────────────────────────────────
    fun setDownloadNotifications(enabled: Boolean) {
        settingsManager.edit().putBoolean(PREF_DOWNLOAD_NOTIFICATIONS, enabled).apply()
        _uiState.value = _uiState.value.copy(downloadNotifications = enabled)
    }

    fun setPlaybackAlerts(enabled: Boolean) {
        settingsManager.edit().putBoolean(PREF_PLAYBACK_ALERTS, enabled).apply()
        _uiState.value = _uiState.value.copy(playbackAlerts = enabled)
    }

    // Helpers
    private fun getFolderSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            total += if (f.isDirectory) getFolderSize(f) else f.length()
        }
        return total
    }

    private fun formatBytes(bytes: Long): String {
        val mb = bytes.toDouble() / (1024 * 1024)
        return if (mb >= 1024) {
            String.format(Locale.US, "%.2f GB", mb / 1024)
        } else {
            String.format(Locale.US, "%.1f MB", mb)
        }
    }
}
