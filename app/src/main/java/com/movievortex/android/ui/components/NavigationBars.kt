package com.movievortex.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.movievortex.android.ui.theme.ManropeFontFamily
import com.movievortex.android.ui.theme.VortexColors

enum class MainDestination {
    Home,
    Search,
    Bookmarks,
    Me,
    LiveTv,
    Downloads;

    companion object {
        val MoviesTv get() = Home
        val Anime get() = Home
        val Settings get() = Me
        val Library get() = Bookmarks
        val Profile get() = Me
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
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(1.dp, Color(0x33FFFFFF), CircleShape),
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
                fontSize = 19.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.3.sp
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xBB0E0E0E))
                    .border(1.dp, Color(0x28FFFFFF), CircleShape)
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
                    modifier = Modifier.size(19.dp)
                )
            }

            // Notifications Bell Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xBB0E0E0E))
                    .border(1.dp, Color(0x28FFFFFF), CircleShape)
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
                    modifier = Modifier.size(19.dp)
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
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Glassmorphic Pill Capsule with Custom Deep Slate Palette
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color(0x80000000),
                    ambientColor = Color(0x40000000)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xF206141B))
                .border(1.dp, Color(0x334A5C6A), RoundedCornerShape(32.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                IconOnlyNavItem(
                    icon = VortexIcons.Home,
                    contentDescription = "Home",
                    isSelected = currentDestination == MainDestination.Home,
                    onClick = { onDestinationSelected(MainDestination.Home) }
                )

                // 2. Search
                IconOnlyNavItem(
                    icon = VortexIcons.Search,
                    contentDescription = "Search",
                    isSelected = currentDestination == MainDestination.Search,
                    onClick = { onDestinationSelected(MainDestination.Search) }
                )

                // 3. Library / Bookmarks
                IconOnlyNavItem(
                    icon = VortexIcons.Bookmark,
                    contentDescription = "Library",
                    isSelected = currentDestination == MainDestination.Bookmarks || currentDestination == MainDestination.Downloads,
                    onClick = { onDestinationSelected(MainDestination.Bookmarks) }
                )

                // 4. Profile / Settings
                IconOnlyNavItem(
                    icon = VortexIcons.Person,
                    contentDescription = "Profile",
                    isSelected = currentDestination == MainDestination.Me,
                    onClick = { onDestinationSelected(MainDestination.Me) }
                )
            }
        }
    }
}

@Composable
private fun IconOnlyNavItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_icon_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFCCD0CF) else Color(0xFF4A5C6A),
        animationSpec = tween(durationMillis = 200),
        label = "nav_icon_color"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) Color(0x66253745) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "nav_container_color"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0x4DCCD0CF) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "nav_border_color"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
