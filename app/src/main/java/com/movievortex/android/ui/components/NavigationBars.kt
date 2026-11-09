package com.movievortex.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    Categories,
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
        Modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color(0xF008090B),
                    Color(0xCC10121A),
                    Color(0x6608090B),
                    Color.Transparent
                )
            )
        )
    } else {
        Modifier.background(Color(0xFF08090B))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(bgModifier)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Circular Frosted Glass Button with Staggered Menu Icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0x262A3442))
                .border(1.dp, Color(0x444A5462), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Menu */ }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = VortexIcons.StaggeredMenu,
                contentDescription = "Menu",
                tint = Color(0xFFFFFFFF),
                modifier = Modifier.size(20.dp)
            )
        }

        // Center: High-End Monochrome OTT Title "MovieVortex" (Pure White + Crisp Silver)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Movie",
                color = Color(0xFFFFFFFF),
                fontSize = 20.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            )
            Text(
                text = "Vortex",
                color = Color(0xFFA8B4C8),
                fontSize = 20.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            )
        }

        // Right: Circular Frosted Glass Button with Search Icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0x262A3442))
                .border(1.dp, Color(0x444A5462), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSearchClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = VortexIcons.Search,
                contentDescription = "Search",
                tint = Color(0xFFFFFFFF),
                modifier = Modifier.size(19.dp)
            )
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Full-Line Simple Frosted Glass Dock (Matching IMG_20261109_185038.jpg)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(29.dp),
                    spotColor = Color(0xCC000000),
                    ambientColor = Color(0x66000000)
                )
                .clip(RoundedCornerShape(29.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xEE262B34), // Top subtle specular frosted surface
                            Color(0xF5181C24), // Center dark slate glass
                            Color(0xFA0E1015)  // Bottom deep obsidian
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0x55FFFFFF), // Crisp top light-catching edge
                            Color(0x22FFFFFF), // Soft translucent mid edge
                            Color(0x0A000000)  // Grounded bottom edge
                        )
                    ),
                    shape = RoundedCornerShape(29.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeColor = Color(0xFFFFFFFF) // Pure Crisp White
                val inactiveColor = Color(0xFF9EA2A5) // Muted Slate Silver

                // 1. Home
                val isHome = currentDestination == MainDestination.Home
                VideoPillNavItem(
                    icon = VortexIcons.VideoHome,
                    contentDescription = "Home",
                    isSelected = isHome,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onDestinationSelected(MainDestination.Home) }
                )

                // 2. Categories (4-Squares Grid icon from reference UI)
                val isCategories = currentDestination == MainDestination.Categories
                VideoPillNavItem(
                    icon = VortexIcons.GridView,
                    contentDescription = "Categories",
                    isSelected = isCategories,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onDestinationSelected(MainDestination.Categories) }
                )

                // 3. Search
                val isSearch = currentDestination == MainDestination.Search
                VideoPillNavItem(
                    icon = VortexIcons.VideoSearch,
                    contentDescription = "Search",
                    isSelected = isSearch,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onDestinationSelected(MainDestination.Search) }
                )

                // 4. Favorites / Watchlist (Heart icon from reference UI)
                val isFavorites = currentDestination == MainDestination.Bookmarks
                VideoPillNavItem(
                    icon = VortexIcons.Heart,
                    contentDescription = "Favorites",
                    isSelected = isFavorites,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onDestinationSelected(MainDestination.Bookmarks) }
                )

                // 5. Profile
                val isProfile = currentDestination == MainDestination.Me
                VideoPillNavItem(
                    icon = VortexIcons.Person,
                    contentDescription = "Profile",
                    isSelected = isProfile,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onDestinationSelected(MainDestination.Me) }
                )
            }
        }
    }
}

@Composable
private fun VideoPillNavItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release,
                is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_item_press"
    )

    val selectScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_item_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 220),
        label = "nav_icon_color"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "nav_glow_alpha"
    )

    Box(
        modifier = modifier
            .size(42.dp)
            .graphicsLayer {
                val totalScale = selectScale * pressScale
                scaleX = totalScale
                scaleY = totalScale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Active Frosted Indicator Glow Pill behind selected icon (Matching IMG_20261109_185038.jpg)
        if (glowAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer { alpha = glowAlpha }
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .border(0.8.dp, Color(0x55FFFFFF), CircleShape)
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
