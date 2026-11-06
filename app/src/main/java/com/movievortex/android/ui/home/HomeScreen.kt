package com.movievortex.android.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
            .background(Color(0xFF0B0E14)),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Top Category Tabs (Video Concept - Frames 3-5)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else Color(0xFF8E95A3),
                        fontSize = if (isSelected) 18.sp else 15.sp,
                        fontFamily = ManropeFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = (-0.3).sp,
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

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { items.size }
    )

    val currentItem = items.getOrNull(pagerState.currentPage) ?: items.first()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 80.dp),
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
        ) { page ->
            val item = items[page]
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        val absOffset = abs(pageOffset).coerceIn(0f, 2f)
                        scaleX = 1f - absOffset * 0.14f
                        scaleY = 1f - absOffset * 0.14f
                        rotationY = pageOffset * -16f
                        cameraDistance = 14f * density
                        alpha = (1f - absOffset * 0.35f).coerceIn(0.4f, 1f)
                    }
                    .fillMaxSize()
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x99000000))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF141923))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(24.dp))
                    .clickable { onTap(item) }
            ) {
                AsyncImage(
                    model = item.poster ?: item.backdrop,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Year (e.g. "2025")
        val yearText = currentItem.year?.toString()?.takeIf { it.isNotBlank() } ?: "2025"
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
            text = currentItem.title,
            color = Color.White,
            fontSize = 22.sp,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Metadata Pills Row: Genre | Duration | Rating
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val genre = currentItem.genres.firstOrNull() ?: "Fantasy"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF181E29))
                    .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = genre,
                    color = Color(0xFFCCD0CF),
                    fontSize = 11.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF181E29))
                    .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = currentItem.duration?.takeIf { it.isNotBlank() } ?: "1h 45min",
                    color = Color(0xFFCCD0CF),
                    fontSize = 11.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }

            val rating = currentItem.rating ?: 7.5f
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF181E29))
                    .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Fluid Animated Pagination Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.take(8).forEachIndexed { idx, _ ->
                val isSelected = idx == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 6.dp else 4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color(0x40FFFFFF))
                )
            }
        }
    }
}
