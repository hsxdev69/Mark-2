package com.movievortex.android.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movievortex.android.controller.SettingsController
import com.movievortex.android.ui.components.VortexIcons
import com.movievortex.android.ui.theme.VortexColors

@Composable
fun SettingsScreen(
    settingsController: SettingsController,
    onNavigateToDownloads: () -> Unit = {},
    onOpenDnsSetup: () -> Unit = {},
    onOpenWatchlist: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onRequireAuth: (String) -> Unit = {},
    isLoggedIn: Boolean = false,
    userName: String = "Alex Vortex",
    modifier: Modifier = Modifier
) {
    val state by settingsController.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Dialog state holders
    var singleSelectDialogTitle by remember { mutableStateOf<String?>(null) }
    var singleSelectOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var singleSelectCurrent by remember { mutableStateOf("") }
    var onSingleSelectSubmit by remember { mutableStateOf<(String) -> Unit>({}) }

    var confirmDialogTitle by remember { mutableStateOf<String?>(null) }
    var confirmDialogMessage by remember { mutableStateOf("") }
    var confirmDialogAction by remember { mutableStateOf<() -> Unit>({}) }

    var showSourcesDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showDownloadLocationDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        settingsController.calculateStorageAndCache()
        settingsController.loadInstalledSources()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VortexColors.PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Preferences & System Configuration",
                    color = Color(0xFF888888),
                    fontSize = 13.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF161616))
                    .border(1.dp, Color(0xFF262626), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = VortexIcons.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // 1. PLAYBACK
        // ─────────────────────────────────────────────────────────────────────
        SettingsSectionHeader(icon = VortexIcons.Play, title = "1. PLAYBACK")
        SettingsCard {
            SettingsClickableRow(
                title = "Default Playback Quality",
                subtitle = state.defaultPlaybackQuality,
                onClick = {
                    singleSelectDialogTitle = "Default Playback Quality"
                    singleSelectOptions = listOf("Auto (Best Available)", "1080p FHD", "720p HD", "480p SD", "4K UHD")
                    singleSelectCurrent = state.defaultPlaybackQuality
                    onSingleSelectSubmit = { settingsController.setDefaultPlaybackQuality(it) }
                }
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = "Auto Play Next",
                subtitle = "Automatically start the next episode when current finishes",
                checked = state.autoPlayNext,
                onCheckedChange = { settingsController.setAutoPlayNext(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = "Resume Playback",
                subtitle = "Continue from exact timestamp where you left off",
                checked = state.resumePlayback,
                onCheckedChange = { settingsController.setResumePlayback(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = "Auto Full Screen",
                subtitle = "Switch directly to landscape full-screen on start",
                checked = state.autoFullScreen,
                onCheckedChange = { settingsController.setAutoFullScreen(it) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────────────
        // 2. AUDIO & SUBTITLES
        // ─────────────────────────────────────────────────────────────────────
        SettingsSectionHeader(icon = VortexIcons.Subtitles, title = "2. AUDIO & SUBTITLES")
        SettingsCard {
            SettingsClickableRow(
                title = "Preferred Audio Language",
                subtitle = state.preferredAudioLanguage,
                onClick = {
                    singleSelectDialogTitle = "Preferred Audio Language"
                    singleSelectOptions = listOf("English", "Hindi", "Tamil", "Telugu", "Japanese", "Korean", "Spanish", "Original Audio")
                    singleSelectCurrent = state.preferredAudioLanguage
                    onSingleSelectSubmit = { settingsController.setPreferredAudioLanguage(it) }
                }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Subtitle Style",
                subtitle = state.subtitleStyle,
                onClick = {
                    singleSelectDialogTitle = "Subtitle Style"
                    singleSelectOptions = listOf("Classic White", "Yellow on Black", "Translucent Background", "High Contrast Black Box")
                    singleSelectCurrent = state.subtitleStyle
                    onSingleSelectSubmit = { settingsController.setSubtitleStyle(it) }
                }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Subtitle Size",
                subtitle = state.subtitleSize,
                onClick = {
                    singleSelectDialogTitle = "Subtitle Size"
                    singleSelectOptions = listOf("Small (75%)", "Normal (100%)", "Large (125%)", "Extra Large (150%)")
                    singleSelectCurrent = state.subtitleSize
                    onSingleSelectSubmit = { settingsController.setSubtitleSize(it) }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────────────
        // 3. DOWNLOADS
        // ─────────────────────────────────────────────────────────────────────
        SettingsSectionHeader(icon = VortexIcons.Download, title = "3. DOWNLOADS")
        SettingsCard {
            SettingsClickableRow(
                title = "Download Quality",
                subtitle = state.downloadQuality,
                onClick = {
                    singleSelectDialogTitle = "Download Quality"
                    singleSelectOptions = listOf("Highest (1080p)", "Balanced (720p)", "Data Saver (480p)", "Always Ask")
                    singleSelectCurrent = state.downloadQuality
                    onSingleSelectSubmit = { settingsController.setDownloadQuality(it) }
                }
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = "Wi-Fi Only",
                subtitle = "Prevent downloading over cellular data",
                checked = state.downloadWifiOnly,
                onCheckedChange = { settingsController.setDownloadWifiOnly(it) }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Download Location",
                subtitle = state.downloadLocation.ifBlank { "Default App Storage" },
                onClick = { showDownloadLocationDialog = true }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Manage Downloads",
                subtitle = "View and remove offline movies and episodes",
                onClick = onNavigateToDownloads,
                badge = state.downloadsSizeMb
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────────────
        // 4. SOURCES & SERVERS
        // ─────────────────────────────────────────────────────────────────────
        SettingsSectionHeader(icon = VortexIcons.Dns, title = "4. SOURCES & SERVERS")
        SettingsCard {
            SettingsClickableRow(
                title = "Manage Sources",
                subtitle = "${state.installedSources.size} Active Provider Plugins",
                onClick = { showSourcesDialog = true },
                badge = "Installed"
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Default Server",
                subtitle = state.defaultServer,
                onClick = {
                    singleSelectDialogTitle = "Default Server Priority"
                    singleSelectOptions = listOf("Auto Select Best", "Anivortex Turbo", "StreamFlare Core", "Rive Server")
                    singleSelectCurrent = state.defaultServer
                    onSingleSelectSubmit = { settingsController.setDefaultServer(it) }
                }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Connection Status",
                subtitle = state.connectionStatus,
                onClick = { settingsController.testConnection() },
                badge = if (state.isTestingConnection) "Testing..." else "Ping DoH"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────────────
        // 5. APPEARANCE
        // ─────────────────────────────────────────────────────────────────────
        SettingsSectionHeader(icon = VortexIcons.Movie, title = "5. APPEARANCE")
        SettingsCard {
            SettingsClickableRow(
                title = "Theme",
                subtitle = state.appTheme,
                onClick = {
                    singleSelectDialogTitle = "Theme"
                    singleSelectOptions = listOf("Dark (OLED Pure Black)", "Light", "System Default")
                    singleSelectCurrent = state.appTheme
                    onSingleSelectSubmit = { settingsController.setAppTheme(it) }
                }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Accent Color",
                subtitle = state.accentColor,
                onClick = {
                    singleSelectDialogTitle = "Accent Color"
                    singleSelectOptions = listOf("Pure White (Cinematic)", "Crimson Red", "Electric Blue", "Emerald Green", "Amber Gold")
                    singleSelectCurrent = state.accentColor
                    onSingleSelectSubmit = { settingsController.setAccentColor(it) }
                }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "App Language",
                subtitle = state.appLanguage,
                onClick = {
                    singleSelectDialogTitle = "App Language"
                    singleSelectOptions = listOf("English", "Hindi", "Spanish", "French", "Japanese")
                    singleSelectCurrent = state.appLanguage
                    onSingleSelectSubmit = { settingsController.setAppLanguage(it) }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────────────
        // 6. NETWORK & DATA
        // ─────────────────────────────────────────────────────────────────────
        SettingsSectionHeader(icon = VortexIcons.Wifi, title = "6. NETWORK & DATA")
        SettingsCard {
            SettingsSwitchRow(
                title = "Data Saver",
                subtitle = "Limits video resolution and reduces pre-buffering",
                checked = state.dataSaver,
                onCheckedChange = { settingsController.setDataSaver(it) }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Streaming Quality",
                subtitle = state.streamingQuality,
                onClick = {
                    singleSelectDialogTitle = "Streaming Quality Mode"
                    singleSelectOptions = listOf("High Quality (Unrestricted)", "Adaptive (Recommended)", "Data Saver (Low Bandwidth)")
                    singleSelectCurrent = state.streamingQuality
                    onSingleSelectSubmit = { settingsController.setStreamingQuality(it) }
                }
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = "Mobile Data Controls",
                subtitle = "Allow streaming playback over mobile cellular data",
                checked = state.mobileDataAllowed,
                onCheckedChange = { settingsController.setMobileDataAllowed(it) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────────────
        // 7. STORAGE & CACHE
        // ─────────────────────────────────────────────────────────────────────
        SettingsSectionHeader(icon = VortexIcons.Storage, title = "7. STORAGE & CACHE")
        SettingsCard {
            SettingsClickableRow(
                title = "Cache Size",
                subtitle = "Temporary streaming buffers & image cache",
                badge = state.cacheSizeMb,
                onClick = { settingsController.calculateStorageAndCache() }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Clear Cache",
                subtitle = "Free temporary buffer space without deleting downloads",
                onClick = {
                    confirmDialogTitle = "Clear Cache?"
                    confirmDialogMessage = "This will delete ${state.cacheSizeMb} of temporary image and streaming cache. Your downloaded movies and watchlist will remain safe."
                    confirmDialogAction = {
                        val ok = settingsController.clearCache()
                        Toast.makeText(context, if (ok) "Cache cleared successfully" else "Failed to clear cache", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Storage Usage",
                subtitle = "Device memory breakdown and free space",
                badge = state.storageFreeGb,
                onClick = { showStorageDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────────────
        // 8. PRIVACY
        // ─────────────────────────────────────────────────────────────────────
        SettingsSectionHeader(icon = VortexIcons.Security, title = "8. PRIVACY")
        SettingsCard {
            SettingsClickableRow(
                title = "Clear Search History",
                subtitle = "Remove saved queries and search suggestions",
                onClick = {
                    confirmDialogTitle = "Clear Search History?"
                    confirmDialogMessage = "All past search entries will be permanently removed from this device."
                    confirmDialogAction = {
                        val ok = settingsController.clearSearchHistory()
                        Toast.makeText(context, if (ok) "Search history cleared" else "Failed to clear search history", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Clear Watch History",
                subtitle = "Reset playback progress and Continue Watching list",
                onClick = {
                    confirmDialogTitle = "Clear Watch History?"
                    confirmDialogMessage = "This will reset all playback progress for movies and episodes. Downloaded media will NOT be deleted."
                    confirmDialogAction = {
                        val ok = settingsController.clearWatchHistory()
                        Toast.makeText(context, if (ok) "Watch history cleared" else "Failed to clear watch history", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────────────
        // 9. NOTIFICATIONS
        // ─────────────────────────────────────────────────────────────────────
        SettingsSectionHeader(icon = VortexIcons.Notifications, title = "9. NOTIFICATIONS")
        SettingsCard {
            SettingsSwitchRow(
                title = "Download Notifications",
                subtitle = "Alert when media downloads complete or encounter issues",
                checked = state.downloadNotifications,
                onCheckedChange = { settingsController.setDownloadNotifications(it) }
            )
            SettingsDivider()
            SettingsSwitchRow(
                title = "Playback Alerts",
                subtitle = "Notifications for bookmarked anime and series releases",
                checked = state.playbackAlerts,
                onCheckedChange = { settingsController.setPlaybackAlerts(it) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─────────────────────────────────────────────────────────────────────
        // 10. ABOUT
        // ─────────────────────────────────────────────────────────────────────
        SettingsSectionHeader(icon = VortexIcons.Settings, title = "10. ABOUT")
        SettingsCard {
            SettingsClickableRow(
                title = "App Version",
                subtitle = state.appVersion,
                badge = "Check Updates",
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hsxdev69/Mark-2/releases/tag/v2.0-latest"))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Running latest version: ${state.appVersion}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Privacy Policy",
                subtitle = "Zero-log & local-first data policy",
                onClick = { showPrivacyPolicyDialog = true }
            )
            SettingsDivider()
            SettingsClickableRow(
                title = "Open-Source Licenses",
                subtitle = "ExoPlayer, Compose, CloudStream Core, OkHttp",
                onClick = { showLicensesDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Dialog Implementations
    // ─────────────────────────────────────────────────────────────────────────

    // Single Select Dialog
    if (singleSelectDialogTitle != null) {
        AlertDialog(
            onDismissRequest = { singleSelectDialogTitle = null },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = singleSelectDialogTitle ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    singleSelectOptions.forEach { option ->
                        val isSelected = option == singleSelectCurrent
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    singleSelectCurrent = option
                                    onSingleSelectSubmit(option)
                                    singleSelectDialogTitle = null
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    singleSelectCurrent = option
                                    onSingleSelectSubmit(option)
                                    singleSelectDialogTitle = null
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color(0xFF666666)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option,
                                color = if (isSelected) Color.White else Color(0xFFCCCCCC),
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { singleSelectDialogTitle = null }) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Confirmation Action Dialog
    if (confirmDialogTitle != null) {
        AlertDialog(
            onDismissRequest = { confirmDialogTitle = null },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = confirmDialogTitle ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = confirmDialogMessage,
                    color = Color(0xFFCCCCCC),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDialogAction()
                        confirmDialogTitle = null
                    }
                ) {
                    Text("Clear", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDialogTitle = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Manage Sources Dialog
    if (showSourcesDialog) {
        AlertDialog(
            onDismissRequest = { showSourcesDialog = false },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active Providers", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF222222))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("${state.installedSources.size} Active", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (state.installedSources.isEmpty()) {
                        Text("No plugins installed. Tap Sync below to bootstrap core plugins.", color = Color(0xFF888888), fontSize = 13.sp)
                    } else {
                        state.installedSources.forEach { source ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(source.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text(source.supportedTypes, color = Color(0xFF888888), fontSize = 12.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E3A20))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(source.language, color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(color = Color(0xFF222222), thickness = 0.5.dp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        activity?.let { settingsController.refreshSources(it) }
                        Toast.makeText(context, "Syncing core repositories & plugins...", Toast.LENGTH_SHORT).show()
                        showSourcesDialog = false
                    }
                ) {
                    Text("Re-sync Core Plugins", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSourcesDialog = false }) {
                    Text("Close", color = Color(0xFF888888))
                }
            }
        )
    }

    // Storage Breakdown Dialog
    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Storage Breakdown", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StorageInfoCard(title = "Free System Memory", value = state.storageFreeGb, color = Color(0xFF4CAF50))
                    StorageInfoCard(title = "Total Device Storage", value = state.storageTotalGb, color = Color.White)
                    StorageInfoCard(title = "Offline Movies & Series", value = state.downloadsSizeMb, color = Color(0xFF2196F3))
                    StorageInfoCard(title = "App Buffer Cache", value = state.cacheSizeMb, color = Color(0xFFFF9800))
                }
            },
            confirmButton = {
                TextButton(onClick = { showStorageDialog = false }) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Download Location Dialog
    if (showDownloadLocationDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadLocationDialog = false },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Download Storage Path", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text(
                        "Downloaded movies are saved to dedicated isolated external app storage:",
                        color = Color(0xFF888888),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0A0A0A))
                            .border(1.dp, Color(0xFF262626), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.downloadLocation,
                            color = Color(0xFFEEEEEE),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsController.resetDownloadLocation()
                        Toast.makeText(context, "Location reset to default", Toast.LENGTH_SHORT).show()
                        showDownloadLocationDialog = false
                    }
                ) {
                    Text("Reset Default", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadLocationDialog = false }) {
                    Text("Close", color = Color(0xFF888888))
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("MovieVortex Privacy Declaration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "1. Zero-Log Architecture: MovieVortex does not collect, sell, or log your IP address, queries, or watch history.\n\n" +
                               "2. Encrypted DNS: Built-in DNS over HTTPS (DoH) routes resolution queries via Cloudflare (1.1.1.1) preventing ISP interception and throttling.\n\n" +
                               "3. Client-Side Only: All bookmarking, continue watching timestamps, and cache records remain strictly stored on your local device.",
                        color = Color(0xFFCCCCCC),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Got It", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Open-Source Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            containerColor = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Open-Source Libraries", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LicenseItem(name = "AndroidX Media3 (ExoPlayer)", license = "Apache License 2.0")
                    LicenseItem(name = "Jetpack Compose", license = "Apache License 2.0")
                    LicenseItem(name = "CloudStream Core Platform", license = "GPL v3.0")
                    LicenseItem(name = "Square OkHttp & Okio", license = "Apache License 2.0")
                    LicenseItem(name = "Coil Image Loader", license = "Apache License 2.0")
                    LicenseItem(name = "Kotlinx Coroutines", license = "Apache License 2.0")
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Subcomponents for Clean Aesthetic
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFAAAAAA),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color(0xFFAAAAAA),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F0F0F))
            .border(1.dp, Color(0xFF1F1F1F), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = Color(0xFF191919),
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingsClickableRow(
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color(0xFF888888),
                fontSize = 13.sp
            )
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Icon(
                imageVector = VortexIcons.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF666666),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color(0xFF888888),
                fontSize = 13.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF333333),
                checkedBorderColor = Color.White,
                uncheckedThumbColor = Color(0xFF666666),
                uncheckedTrackColor = Color(0xFF141414),
                uncheckedBorderColor = Color(0xFF262626)
            )
        )
    }
}

@Composable
private fun StorageInfoCard(title: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0C0C0C))
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color(0xFFCCCCCC), fontSize = 13.sp)
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LicenseItem(name: String, license: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(license, color = Color(0xFF888888), fontSize = 11.sp)
    }
}
