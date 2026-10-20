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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.movievortex.android.ui.components.PosterCard
import com.movievortex.android.ui.components.PosterSkeleton
import com.movievortex.android.ui.components.VortexIcons
import com.movievortex.android.ui.theme.VortexColors
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    homeController: HomeController,
    onMediaClick: (MovieVortexMedia) -> Unit,
    onPlayClick: (MovieVortexMedia) -> Unit,
    onPlatformClick: (StreamingPlatform) -> Unit,
    onRequireAuth: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by homeController.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Movies", "Series", "Anime", "4K UHD", "Documentaries")

    val filteredRows = remember(selectedCategory, state.rows) {
        when (selectedCategory) {
            "All" -> state.rows
            "Movies" -> state.rows.mapNotNull { row ->
                val movies = row.items.filter { it.type == MediaType.Movie || it.title.contains("Movie", ignoreCase = true) }
                if (movies.isNotEmpty()) row.copy(items = movies) else null
            }.ifEmpty { state.rows }
            "Series" -> state.rows.mapNotNull { row ->
                val series = row.items.filter { it.type == MediaType.TvSeries || it.title.contains("Season", ignoreCase = true) || it.title.contains("Series", ignoreCase = true) }
                if (series.isNotEmpty()) row.copy(items = series) else null
            }.ifEmpty { state.rows }
            "Anime" -> state.rows.mapNotNull { row ->
                val anime = row.items.filter { it.type == MediaType.Anime || it.title.contains("Anime", ignoreCase = true) }
                if (anime.isNotEmpty()) row.copy(items = anime) else null
            }.ifEmpty { state.rows }
            else -> state.rows
        }
    }

    val heroList = state.heroItems.ifEmpty {
        filteredRows.firstOrNull()?.items?.take(5) ?: emptyList()
    }

    if (state.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(VortexColors.PureBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading MovieVortex...",
                    color = VortexColors.MutedText,
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    LazyColumn(
        state = scrollState,
        modifier = modifier
            .fillMaxSize()
            .background(VortexColors.PureBlack),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Sleek Full-Bleed Hero Carousel (Matching Flutter hero_carousel.dart)
        if (heroList.isNotEmpty()) {
            item {
                FlutterStyleHeroCarousel(
                    items = heroList,
                    onTap = onMediaClick,
                    onWatchNow = onPlayClick,
                    onAddToList = { item ->
                        onRequireAuth("Sign in to save '${item.title}' to your personal Watchlist.")
                    }
                )
            }
        }

        // 2. Monochrome Category Tabs (Matching Flutter _buildCategoryTabs)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color.White else VortexColors.CardSurface)
                            .border(
                                1.dp,
                                if (isSelected) Color.White else Color(0x22FFFFFF),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Continue Watching Section (if any)
        if (state.continueWatching.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "Continue Watching", icon = VortexIcons.Play)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.continueWatching) { item ->
                        PosterCard(
                            media = item,
                            onClick = { onPlayClick(item) },
                            width = 140.dp
                        )
                    }
                }
            }
        }

        // 4. Trending Now Horizontal Section (Matching Flutter HomeScreen)
        val trendingItems = filteredRows.firstOrNull()?.items ?: emptyList()
        if (trendingItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "Trending Now", icon = VortexIcons.TrendingUp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingItems) { item ->
                        PosterCard(
                            media = item,
                            onClick = { onMediaClick(item) },
                            width = 135.dp
                        )
                    }
                }
            }
        }

        // 5. Blockbusters & Originals (Matching Flutter HomeScreen)
        val blockbusterItems = filteredRows.getOrNull(1)?.items ?: heroList
        if (blockbusterItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "Blockbusters & Originals", icon = VortexIcons.Movie)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(blockbusterItems) { item ->
                        PosterCard(
                            media = item,
                            onClick = { onMediaClick(item) },
                            width = 135.dp
                        )
                    }
                }
            }
        }

        // 6. Remaining CloudStream Provider Rows
        val remainingRows = filteredRows.drop(2)
        items(remainingRows) { row ->
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = row.title, icon = null)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

        // 7. Explore More Grid (3 columns chunked)
        if (state.exploreGrid.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Explore More",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Infinite catalog curated from global streaming networks",
                        color = VortexColors.MutedText,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            val chunked = state.exploreGrid.chunked(3)
            items(chunked) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { media ->
                        PosterCard(
                            media = media,
                            onClick = { onMediaClick(media) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

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
}

@Composable
fun FlutterStyleHeroCarousel(
    items: List<MovieVortexMedia>,
    onTap: (MovieVortexMedia) -> Unit,
    onWatchNow: (MovieVortexMedia) -> Unit,
    onAddToList: (MovieVortexMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(items) {
        while (true) {
            delay(6000)
            if (items.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % items.size
            }
        }
    }

    val activeItem = items.getOrNull(currentIndex) ?: return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
            .clickable { onTap(activeItem) }
    ) {
        // Full-bleed Backdrop Image
        Crossfade(targetState = activeItem, label = "hero_slide") { item ->
            AsyncImage(
                model = item.backdrop ?: item.poster,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Ultra-Smooth Monochrome Gradient Overlays (Matching Flutter HeroCarousel)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x80000000),
                            Color.Transparent,
                            Color(0xB3000000),
                            VortexColors.PureBlack
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Hero Content Overlay at Bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Genre Tags / Rating Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Premier 4K Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PREMIER 4K",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                // Star Rating
                val rating = activeItem.rating ?: 0f
                if (rating > 0f) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC000000))
                            .border(0.8.dp, Color(0x4DFFFFFF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "★ %.1f".format(rating),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Subtitle
                val genresText = activeItem.genres.take(2).joinToString(" • ")
                if (genresText.isNotBlank()) {
                    Text(
                        text = genresText,
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title (26sp, ExtraBold Manrope, letterSpacing -0.6)
            Text(
                text = activeItem.title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.6).sp,
                lineHeight = 30.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Watch Now & + My List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Watch Now Button (Pure White Pill, Black Play Icon + Text)
                Button(
                    onClick = { onWatchNow(activeItem) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = VortexIcons.Play,
                        contentDescription = "Watch Now",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Watch Now",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // + My List Button (Frosted Glass Pill)
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                        .clickable { onAddToList(activeItem) }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = VortexIcons.Add,
                            contentDescription = "My List",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "My List",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Capsule Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.take(6).forEachIndexed { idx, _ ->
                    val isSelected = idx == currentIndex
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (isSelected) 22.dp else 6.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) Color.White else Color(0x40FFFFFF))
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp
            )
        }
        Text(
            text = "See All",
            color = VortexColors.MutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
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
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    FlutterStyleHeroCarousel(
        items = items,
        onTap = onMediaClick,
        onWatchNow = onPlayClick,
        onAddToList = onMyListClick,
        modifier = modifier
    )
}
