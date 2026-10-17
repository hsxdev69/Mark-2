package com.movievortex.android.ui.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.movievortex.android.ui.components.VortexIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import com.movievortex.android.ui.theme.TextMuted
import com.movievortex.android.ui.theme.TextSecondary
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.movievortex.android.controller.DownloadController
import com.movievortex.android.controller.DownloadUiItem
import com.movievortex.android.model.MediaDownloadState
import com.movievortex.android.ui.theme.VortexColors
import java.util.Locale

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

@Composable
fun DownloadsScreen(
    downloadController: DownloadController,
    onBrowseClick: () -> Unit,
    onPlayOffline: (DownloadUiItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by downloadController.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        downloadController.loadDownloads()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VortexColors.BackgroundOled)
            .padding(top = 16.dp)
    ) {
        // 1. Header: Downloads & Offline Vault + Settings Capsule (ui_download.png)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Downloads\n& Offline Vault",
                color = VortexColors.PrimaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VortexColors.GlassSurfaceDark)
                    .border(1.dp, VortexColors.GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = VortexIcons.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Storage Analytics Gauge Card (ui_download.png)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(VortexColors.GlassSurfaceDark)
                .border(1.dp, VortexColors.GlassBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular Storage Gauge with Hard Drive Icon
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        progress = { 0.35f },
                        color = VortexColors.NeonCrimson,
                        trackColor = Color(0x33FFFFFF),
                        strokeWidth = 4.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                        imageVector = VortexIcons.Storage,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "MovieVortex Storage:",
                        color = VortexColors.TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "14.2 GB used",
                        color = VortexColors.NeonCrimson,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Available: 48 GB",
                        color = VortexColors.TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Segmented Dual-Tab Switcher: Downloaded vs Active Queue (ui_download.png)
        val completedCount = state.items.count { it.state == MediaDownloadState.Completed }
        val activeCount = state.items.count { it.state != MediaDownloadState.Completed }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selectedTab == 0) VortexColors.GlassSurfaceDark else Color.Transparent)
                    .border(
                        1.dp,
                        if (selectedTab == 0) VortexColors.GlassBorder else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { selectedTab = 0 }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Downloaded ($completedCount)",
                    color = if (selectedTab == 0) Color.White else VortexColors.TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selectedTab == 1) VortexColors.GlassSurfaceDark else Color.Transparent)
                    .border(
                        1.dp,
                        if (selectedTab == 1) VortexColors.GlassBorder else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { selectedTab = 1 }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Active Queue",
                        color = if (selectedTab == 1) Color.White else VortexColors.TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (activeCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(VortexColors.NeonCrimson),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$activeCount",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter items based on selected tab
        val displayItems = if (selectedTab == 0) {
            state.items.filter { it.state == MediaDownloadState.Completed }
        } else {
            state.items.filter { it.state != MediaDownloadState.Completed }
        }

        if (displayItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = VortexIcons.DownloadOff,
                        contentDescription = null,
                        tint = VortexColors.MutedText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedTab == 0) "No downloaded media" else "Download queue is empty",
                        color = VortexColors.PrimaryText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore movies and anime to download for offline vault",
                        color = VortexColors.MutedText,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBrowseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = VortexColors.NeonCrimson),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Browse Content", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayItems) { item ->
                    DownloadItemCard(
                        item = item,
                        onPause = { downloadController.pauseDownload(item.id) },
                        onResume = { downloadController.resumeDownload(item.id) },
                        onRetry = { downloadController.retryDownload(context, item) },
                        onDelete = { downloadController.deleteDownload(item.id) },
                        onPlay = { onPlayOffline(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadItemCard(
    item: DownloadUiItem,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
    onPlay: () -> Unit
) {
    val isCompleted = item.state == MediaDownloadState.Completed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(VortexColors.GlassSurfaceDark)
            .border(1.dp, VortexColors.GlassBorder, RoundedCornerShape(18.dp))
            .clickable(enabled = isCompleted) { onPlay() }
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Media Poster Thumbnail
                Box(
                    modifier = Modifier
                        .width(68.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(VortexColors.DarkGraphite)
                ) {
                    AsyncImage(
                        model = item.poster,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Media Details & Status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isCompleted) {
                        // Green Verified MP4 badge from ui_download.png
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0x3310B981), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Verified MP4 ✓",
                                    color = VortexColors.VerifiedGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${formatFileSize(item.fileSizeBytes)} • ${item.quality}",
                                color = VortexColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        // Active / Paused / Queue status
                        when (item.state) {
                            MediaDownloadState.Downloading -> {
                                Text(
                                    text = "${(item.progress * 100).toInt()}% • 4.2 MB/s",
                                    color = VortexColors.NeonCrimson,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${formatFileSize(item.downloadedBytes)} / ${formatFileSize(item.fileSizeBytes)}",
                                    color = VortexColors.TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            MediaDownloadState.Paused -> {
                                Text(
                                    text = "Paused • ${(item.progress * 100).toInt()}%",
                                    color = VortexColors.TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            MediaDownloadState.Failed -> {
                                Text(
                                    text = "Download failed",
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            else -> {
                                Text(
                                    text = "Queued",
                                    color = VortexColors.TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Delete / Cancel button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = VortexIcons.Close,
                        contentDescription = "Cancel/Delete",
                        tint = VortexColors.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bottom section: Progress bar or Play button
            if (isCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = VortexColors.NeonCrimson),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = VortexIcons.Play,
                        contentDescription = "Play Offline",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play Offline",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else if (item.state == MediaDownloadState.Downloading || item.state == MediaDownloadState.Paused) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { item.progress },
                    color = VortexColors.NeonCrimson,
                    trackColor = Color(0x33FFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (item.state == MediaDownloadState.Downloading) {
                        IconButton(onClick = onPause, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = VortexIcons.Pause,
                                contentDescription = "Pause",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        IconButton(onClick = onResume, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = VortexIcons.Play,
                                contentDescription = "Resume",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
