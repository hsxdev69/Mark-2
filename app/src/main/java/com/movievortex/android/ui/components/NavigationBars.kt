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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xF50A0E17))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Ultra subtle top hair-line divider matching CineNova reference
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.6.dp)
                .background(Color(0x18FFFFFF))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeColor = Color(0xFFE53448) // CineNova Coral Red
            val inactiveColor = Color(0xFF676E79) // CineNova Muted Slate Outline

            // 1. Home
            val isHome = currentDestination == MainDestination.Home
            CineNovaNavItem(
                icon = if (isHome) VortexIcons.Home else VortexIcons.HomeOutline,
                contentDescription = "Home",
                isSelected = isHome,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onDestinationSelected(MainDestination.Home) }
            )

            // 2. Search
            val isSearch = currentDestination == MainDestination.Search
            CineNovaNavItem(
                icon = VortexIcons.Search,
                contentDescription = "Search",
                isSelected = isSearch,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onDestinationSelected(MainDestination.Search) }
            )

            // 3. Library / Bookmarks
            val isBookmarks = currentDestination == MainDestination.Bookmarks || currentDestination == MainDestination.Downloads
            CineNovaNavItem(
                icon = if (isBookmarks) VortexIcons.Bookmark else VortexIcons.BookmarkBorder,
                contentDescription = "Library",
                isSelected = isBookmarks,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onDestinationSelected(MainDestination.Bookmarks) }
            )

            // 4. Profile / Settings
            val isProfile = currentDestination == MainDestination.Me
            CineNovaNavItem(
                icon = if (isProfile) VortexIcons.Person else VortexIcons.PersonOutline,
                contentDescription = "Profile",
                isSelected = isProfile,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onDestinationSelected(MainDestination.Me) }
            )
        }
    }
}

@Composable
private fun CineNovaNavItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_item_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "nav_icon_color"
    )

    Box(
        modifier = modifier
            .size(54.dp)
            .scale(scale)
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
            modifier = Modifier.size(23.dp)
        )
    }
}
