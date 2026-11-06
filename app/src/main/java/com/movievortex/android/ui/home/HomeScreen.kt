package com.movievortex.android.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.movievortex.android.controller.ContentRow
import com.movievortex.android.controller.HomeController
import com.movievortex.android.model.MediaType
import com.movievortex.android.model.MovieVortexMedia
import com.movievortex.android.model.StreamingPlatform
import com.movievortex.android.ui.components.HomeScreenSkeleton
import com.movievortex.android.ui.components.PosterCard
import com.movievortex.android.ui.components.VortexIcons
import com.movievortex.android.ui.theme.ManropeFontFamily
import kotlin.math.abs

@Composable
fun HomeScreen(
    homeController: HomeController,
    onMediaClick: (MovieVortexMedia) -> Unit,
    onPlayClick: (MovieVortexMedia) -> Unit,
    onPlatformClick: (StreamingPlatform) -> Unit,
    onRequireAuth: (String) -> Unit = {},
    onNavigateToLiveTv: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by homeController.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    var selectedCategory by remember { mutableStateOf("Trending") }

    val categories = listOf("Trending", "New", "Movies", "Serials", "TV shows")

    val filteredRows = remember(selectedCategory, state.rows) {
        when (selectedCategory) {
            "Trending", "New" -> state.rows
            "Movies" -> state.rows.mapNotNull { row ->
                val movies = row.items.filter { it.type == MediaType.Movie || it.title.contains("Movie", ignoreCase = true) }
                if (movies.isNotEmpty()) row.copy(items = movies) else null
            }.ifEmpty { state.rows }
            "Serials", "TV shows" -> state.rows.mapNotNull { row ->
                val series = row.items.filter { it.type == MediaType.TvSeries || it.title.contains("Season", ignoreCase = true) || it.title.contains("Series", ignoreCase = true) }
                if (series.isNotEmpty()) row.copy(items = series) else null
            }.ifEmpty { state.rows }
            else -> state.rows
        }
    }

    // Trending Hero Carousel: preserves recent items if available, or trending items
    val heroList = remember(state.continueWatching, state.heroItems, filteredRows) {
        state.continueWatching.ifEmpty {
            state.heroItems.ifEmpty {
                filteredRows.firstOrNull()?.items?.take(6) ?: emptyList()
            }
        }
    }

    if (state.isLoading) {
        HomeScreenSkeleton(modifier = modifier)
        return
    }

    LazyColumn(
        state = scrollState,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F141C)),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // 1. Top Category Tabs (Video Concept - Frames 3-5)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 10.dp, bottom = 4.dp)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Color(0xFF6B7584),
                            fontSize = if (isSelected) 22.sp else 15.sp,
                            fontFamily = ManropeFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = (-0.4).sp,
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { selectedCategory = cat }
                                )
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 2. Liquid 3D Perspective Fling Carousel (Video Concept - Frames 3-5)
        if (heroList.isNotEmpty()) {
            item {
                VideoStyle3DHeroCarousel(
                    items = heroList,
                    onTap = onMediaClick
                )
            }
        }

        // 3. "For you" Row (Video Concept - Frame 3)
        val forYouItems = filteredRows.firstOrNull()?.items ?: emptyList()
        if (forYouItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "For you",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "See all",
                        color = Color(0xFF8E95A3),
                        fontSize = 12.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(forYouItems) { item ->
                        PosterCard(
                            media = item,
                            onClick = { onMediaClick(item) },
                            width = 135.dp
                        )
                    }
                }
            }
        }

        // 4. Content Rows (Popular Movies, Series, etc.)
        val remainingRows = filteredRows.drop(1)
        items(remainingRows) { row ->
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.title,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    text = "See all",
                    color = Color(0xFF8E95A3),
                    fontSize = 12.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(row.items) { item ->
                    PosterCard(
                        media = item,
                        onClick = { onMediaClick(item) },
                        width = 135.dp
                    )
                }
            }
        }

        // 5. Explore More / Grid Loader
        item {
            LaunchedEffect(Unit) {
                homeController.loadMoreExplore()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isExploreLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoStyle3DHeroCarousel(
    items: List<MovieVortexMedia>,
    onTap: (MovieVortexMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    // Infinite Virtual Page Count (allows continuous looping in both directions)
    val virtualCount = 10000
    val initialPage = (virtualCount / (2 * items.size)) * items.size
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { virtualCount }
    )

    // 1. Automatic horizontal poster sliding every 4.5 seconds (smooth infinite carousel)
    LaunchedEffect(pagerState, items.size) {
        while (true) {
            kotlinx.coroutines.delay(4500)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val activeIndex = (pagerState.currentPage % items.size).let { if (it < 0) it + items.size else it }
    val currentItem = items.getOrNull(activeIndex) ?: items.first()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            contentAlignment = Alignment.Center
        ) {
            // Dynamic Ambient Colorful Aura Glow with smooth Crossfade
            Crossfade(
                targetState = currentItem.poster ?: currentItem.backdrop,
                animationSpec = tween(600),
                label = "hero_aura_crossfade"
            ) { auraUrl ->
                AsyncImage(
                    model = auraUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(240.dp, 290.dp)
                        .blur(50.dp)
                        .graphicsLayer {
                            alpha = 0.45f
                            scaleX = 1.15f
                            scaleY = 1.15f
                        }
                )
            }

            // 3D Overlapping Card Deck Pager
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 92.dp),
                pageSpacing = (-38).dp, // Negative spacing tucks side cards behind center card
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val realIdx = (page % items.size).let { if (it < 0) it + items.size else it }
                val item = items[realIdx]
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val absOffset = abs(pageOffset).coerceIn(0f, 2f)

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
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "hero_card_press"
                )

                Box(
                    modifier = Modifier
                        .zIndex(1f - absOffset) // Active center card is strictly on top
                        .graphicsLayer {
                            // Premium Cover Flow 3D Perspective Scaling, Tilt & Rotation
                            val cardScale = (1f - absOffset * 0.14f) * pressScale
                            scaleX = cardScale
                            scaleY = cardScale
                            rotationY = pageOffset.coerceIn(-1.5f, 1.5f) * -22f
                            cameraDistance = 18f * density

                            // Subtle downward arc translation for side cards
                            translationY = absOffset * 6f * density

                            // Side cards remain partially visible and dim slightly
                            alpha = (1f - absOffset * 0.28f).coerceIn(0.55f, 1f)
                        }
                        .fillMaxSize()
                        .shadow(
                            elevation = if (absOffset < 0.5f) 28.dp else 10.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = Color(0xCC000000),
                            ambientColor = Color(0x66000000)
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF141923))
                        .border(
                            width = 1.2.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0x66FFFFFF), // Crisp specular white edge at top
                                    Color(0x22FFFFFF), // Subtle mid edge
                                    Color(0x0AFFFFFF)  // Faint bottom edge
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onTap(item) }
                        )
                ) {
                    // Movie Poster Artwork
                    AsyncImage(
                        model = item.poster ?: item.backdrop,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Liquid Glass Top Gloss Sheen Overlay (Video Concept - Frame 4)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x38FFFFFF),
                                        Color(0x10FFFFFF),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Dimmer scrim for side cards so center card pops
                    if (absOffset > 0.05f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = (absOffset * 0.35f).coerceIn(0f, 0.45f)))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Animated Year, Title & Metadata Pills on poster change
        AnimatedContent(
            targetState = currentItem,
            transitionSpec = {
                (fadeIn(animationSpec = tween(380)) + slideInVertically(animationSpec = tween(380, easing = FastOutSlowInEasing)) { 18 })
                    .togetherWith(fadeOut(animationSpec = tween(220)) + slideOutVertically(animationSpec = tween(220)) { -18 })
            },
            label = "hero_metadata_anim",
            modifier = Modifier.fillMaxWidth()
        ) { targetMedia ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Year (e.g. "2025")
                val yearText = targetMedia.year?.toString()?.takeIf { it.isNotBlank() } ?: "2025"
                Text(
                    text = yearText,
                    color = Color(0xFF8E95A3),
                    fontSize = 13.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Title (e.g. "Minecraft Movie")
                Text(
                    text = targetMedia.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata Pills Row: Genre | Duration | Rating (Video Concept - Frame 4)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val genre = targetMedia.genres.firstOrNull() ?: "Anime"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x38354354))
                            .border(
                                0.8.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x3DFFFFFF),
                                        Color(0x14FFFFFF)
                                    )
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 11.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = genre,
                            color = Color(0xFFE5E7EB),
                            fontSize = 11.sp,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    val runtimeText = targetMedia.runtime?.let { "${it / 60}h ${it % 60}min" } ?: "9 EP"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x38354354))
                            .border(
                                0.8.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x3DFFFFFF),
                                        Color(0x14FFFFFF)
                                    )
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 11.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = runtimeText,
                            color = Color(0xFFE5E7EB),
                            fontSize = 11.sp,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    val rating = targetMedia.rating ?: 7.5f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x38354354))
                            .border(
                                0.8.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x3DFFFFFF),
                                        Color(0x14FFFFFF)
                                    )
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 11.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "★ ",
                                color = Color(0xFFFFC107),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "%.1f".format(rating),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fluid Animated Pagination Indicator Dots/Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val totalDots = items.size.coerceAtMost(8)
            for (idx in 0 until totalDots) {
                val isSelected = idx == activeIndex
                val dotWidth by animateDpAsState(
                    targetValue = if (isSelected) 16.dp else 5.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "dot_width"
                )
                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .width(dotWidth)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color(0x40FFFFFF))
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        }
        Text(
            text = "See all",
            color = Color(0xFF8E95A3),
            fontSize = 12.sp,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onSeeAllClick?.invoke() }
        )
    }
}

@Composable
fun ContentRowSection(
    row: ContentRow,
    onMediaClick: (MovieVortexMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = row.title, icon = null)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(row.items) { item ->
                PosterCard(
                    media = item,
                    onClick = { onMediaClick(item) },
                    width = 135.dp
                )
            }
        }
    }
}

@Composable
fun FeaturedHero(
    items: List<MovieVortexMedia>,
    onMediaClick: (MovieVortexMedia) -> Unit,
    onPlayClick: (MovieVortexMedia) -> Unit,
    onMyListClick: (MovieVortexMedia) -> Unit = {},
    modifier: Modifier = Modifier
) {
    VideoStyle3DHeroCarousel(
        items = items,
        onTap = onMediaClick,
        modifier = modifier
    )
}
