package com.movievortex.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.movievortex.android.ui.theme.VortexColors

enum class MainDestination {
    Home,
    Search,
    Downloads,
    Me;

    companion object {
        val MoviesTv get() = Home
        val Anime get() = Home
        val Settings get() = Me
    }
}

@Composable
fun MovieVortexTopBar(
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isTransparent: Boolean = true
) {
    val bgModifier = if (isTransparent) {
        Modifier.background(VortexColors.TopBarGradient)
    } else {
        Modifier.background(VortexColors.BackgroundOled)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(bgModifier)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Official MovieVortex 'M' Brand Logo with Neon Crimson Glow & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(1.dp, VortexColors.NeonCrimsonGlow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = com.lagradost.cloudstream3.R.drawable.vortex_brand_logo,
                    contentDescription = "MovieVortex",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = "MovieVortex",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // Right: Frosted Glass Circular Buttons for Cast and Notifications
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chromecast Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(VortexColors.GlassSurfaceDark)
                    .border(1.dp, VortexColors.GlassBorder, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Cast */ }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = VortexIcons.Cast,
                    contentDescription = "Cast",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Notifications Bell Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(VortexColors.GlassSurfaceDark)
                    .border(1.dp, VortexColors.GlassBorder, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Notifications */ }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = VortexIcons.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MovieVortexBottomBar(
    currentDestination: MainDestination,
    onDestinationSelected: (MainDestination) -> Unit,
    modifier: Modifier = Modifier,
    activeDownloadCount: Int = 0
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Glass Pill Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(36.dp))
                .background(VortexColors.GlassSurfaceDark)
                .border(1.dp, VortexColors.GlassBorder, RoundedCornerShape(36.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavIslandItem(
                    title = "Home",
                    icon = VortexIcons.Movie,
                    isSelected = currentDestination == MainDestination.Home,
                    onClick = { onDestinationSelected(MainDestination.Home) }
                )

                BottomNavIslandItem(
                    title = "Search",
                    icon = VortexIcons.Search,
                    isSelected = currentDestination == MainDestination.Search,
                    onClick = { onDestinationSelected(MainDestination.Search) }
                )

                BottomNavIslandItem(
                    title = "Downloads",
                    icon = VortexIcons.Download,
                    isSelected = currentDestination == MainDestination.Downloads,
                    badgeCount = activeDownloadCount,
                    onClick = { onDestinationSelected(MainDestination.Downloads) }
                )

                BottomNavIslandItem(
                    title = "Me",
                    icon = VortexIcons.Person,
                    isSelected = currentDestination == MainDestination.Me,
                    onClick = { onDestinationSelected(MainDestination.Me) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavIslandItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    val interactionSource = remember { MutableInteractionSource() }

    if (isSelected) {
        // Active Pill in MovieVortex 2.0: Glowing Neon Crimson Capsule
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(VortexColors.CrimsonGradient)
                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        // Inactive: Minimal frosted icon with notification badge
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = VortexColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(VortexColors.NeonCrimson),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
