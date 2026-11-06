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
                    Color(0xF006141B),
                    Color(0xCC11212D),
                    Color(0x6606141B),
                    Color.Transparent
                )
            )
        )
    } else {
        Modifier.background(Color(0xFF06141B))
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
                .background(Color(0x40253745))
                .border(1.dp, Color(0x334A5C6A), CircleShape)
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
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Center: High-End Monochrome OTT Title "MovieVortex" (Pure White + Soft Muted Slate)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Movie",
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            )
            Text(
                text = "Vortex",
                color = Color(0xFF9BA8AB),
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
                .background(Color(0x40253745))
                .border(1.dp, Color(0x334A5C6A), CircleShape)
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
                tint = Color.White,
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
            .padding(horizontal = 24.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Glass Capsule (Monochrome Dark Slate #06141B, #11212D, #253745)
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .fillMaxWidth()
                .height(52.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    spotColor = Color(0xEE06141B),
                    ambientColor = Color(0x88000000)
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x99253745), // Top specular dark slate highlight
                            Color(0xD911212D), // Center frosted slate surface
                            Color(0xF206141B)  // Bottom deep translucent black
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0x594A5C6A), // Crisp subtle top edge
                            Color(0x2E253745), // Soft mid edge
                            Color(0x1406141B)  // Subtle bottom edge
                        )
                    ),
                    shape = CircleShape
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeColor = Color(0xFFFFFFFF) // Pure White
                val inactiveColor = Color(0xFF9BA8AB) // Soft Muted Gray

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

                // 2. Search
                val isSearch = currentDestination == MainDestination.Search
                VideoPillNavItem(
                    icon = VortexIcons.VideoSearch,
                    contentDescription = "Search",
                    isSelected = isSearch,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onDestinationSelected(MainDestination.Search) }
                )

                // 3. Bookmark / Library
                val isLibrary = currentDestination == MainDestination.Bookmarks
                VideoPillNavItem(
                    icon = VortexIcons.Bookmark,
                    contentDescription = "Library",
                    isSelected = isLibrary,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onDestinationSelected(MainDestination.Bookmarks) }
                )

                // 4. Profile
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
        // Active Frosted Indicator Glow Pill behind selected icon
        if (glowAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .graphicsLayer { alpha = glowAlpha }
                    .clip(CircleShape)
                    .background(Color(0x2EFFFFFF))
                    .border(0.8.dp, Color(0x38FFFFFF), CircleShape)
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
