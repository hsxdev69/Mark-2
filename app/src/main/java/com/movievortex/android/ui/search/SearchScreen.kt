package com.movievortex.android.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.movievortex.android.controller.SearchController
import com.movievortex.android.controller.SearchStatus
import com.movievortex.android.model.MovieVortexMedia
import com.movievortex.android.ui.components.PosterCard
import com.movievortex.android.ui.components.PosterSkeleton
import com.movievortex.android.ui.components.VortexIcons
import com.movievortex.android.ui.theme.ManropeFontFamily

data class GenreChipItem(val name: String, val count: Int)
data class ActorItem(val name: String, val photoUrl: String)

@Composable
fun SearchScreen(
    searchController: SearchController,
    onMediaClick: (MovieVortexMedia) -> Unit,
    recommendations: List<MovieVortexMedia> = emptyList(),
    onOpenCategory: (String) -> Unit = {},
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by searchController.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val categoryChips15 = remember {
        listOf(
            GenreChipItem("Biographical", 932),
            GenreChipItem("Western", 673),
            GenreChipItem("Documentary", 126),
            GenreChipItem("Action", 1096),
            GenreChipItem("Fantasy", 475),
            GenreChipItem("Musical", 398),
            GenreChipItem("War", 56),
            GenreChipItem("Independent", 49),
            GenreChipItem("Crime", 399),
            GenreChipItem("Sci-fi", 821),
            GenreChipItem("Comedy", 736),
            GenreChipItem("Animation", 512),
            GenreChipItem("Adventure", 840),
            GenreChipItem("Horror", 620),
            GenreChipItem("Thriller", 940)
        )
    }

    val actorsList = remember {
        listOf(
            ActorItem("Leonardo DiCaprio", "https://image.tmdb.org/t/p/w185/wo2pa3nrvuaAcHgusvt8Ah894l.jpg"),
            ActorItem("Scarlett Johansson", "https://image.tmdb.org/t/p/w185/6NsMbJXRlDZuDzatQTlgAmGu39e.jpg"),
            ActorItem("George Clooney", "https://image.tmdb.org/t/p/w185/kFQPghqT7bY1fFw1uB4o1A1lVb8.jpg"),
            ActorItem("Johnny Depp", "https://image.tmdb.org/t/p/w185/ilPB0q0wI5J525v0Y8yFv458a2E.jpg"),
            ActorItem("Cillian Murphy", "https://image.tmdb.org/t/p/w185/3On2GL446E8jOC932t009dC9W1Y.jpg")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF08090B))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // 1. Search Bar with Filter Sliders Icon (from Reference UI)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Input Pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color(0xFF10121A))
                    .border(1.dp, Color(0x33959CE3), RoundedCornerShape(25.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = VortexIcons.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF959CE3),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (state.query.isEmpty()) {
                            Text(
                                text = "Search",
                                color = Color(0xFF959CE3),
                                fontSize = 15.sp,
                                fontFamily = ManropeFontFamily
                            )
                        }

                        BasicTextField(
                            value = state.query,
                            onValueChange = { searchController.onQueryChange(it) },
                            singleLine = true,
                            cursorBrush = SolidColor(Color(0xFFF8F8F8)),
                            textStyle = TextStyle(
                                color = Color(0xFFF8F8F8),
                                fontSize = 15.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Normal
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                    searchController.performSearch(state.query)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (state.query.isNotEmpty()) {
                        IconButton(
                            onClick = { searchController.clearQuery() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = VortexIcons.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF959CE3),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Filter Sliders Icon Button (Matching Screenshot)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10121A))
                    .border(1.dp, Color(0x33959CE3), CircleShape)
                    .clickable { onOpenCategory("All Categories") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = VortexIcons.Tune,
                    contentDescription = "Filter",
                    tint = Color(0xFFF8F8F8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2. Search Body
        when (state.status) {
            SearchStatus.Idle -> {
                val recentItems = recommendations.take(6).ifEmpty { state.results.take(6) }
                val popularItems = recommendations.drop(6).take(6).ifEmpty { state.results.take(6) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    // SECTION A: "Recent Search" with "See all >"
                    if (recentItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Search",
                                    color = Color(0xFFF8F8F8),
                                    fontSize = 18.sp,
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onOpenCategory("Recent Search") }
                                ) {
                                    Text(
                                        text = "See all",
                                        color = Color(0xFF959CE3),
                                        fontSize = 12.sp,
                                        fontFamily = ManropeFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = VortexIcons.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF959CE3),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(recentItems) { media ->
                                    PosterCard(
                                        media = media,
                                        onClick = { onMediaClick(media) },
                                        width = 135.dp
                                    )
                                }
                            }
                        }
                    }

                    // SECTION B: "Categories" (15 chips as user requested)
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categories",
                                color = Color(0xFFF8F8F8),
                                fontSize = 18.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onOpenCategory("All Categories") }
                            ) {
                                Text(
                                    text = "See all",
                                    color = Color(0xFF959CE3),
                                    fontSize = 12.sp,
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = VortexIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF959CE3),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Row 1 of chips
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categoryChips15.take(4)) { chip ->
                                CategoryChipPill(chip = chip, onClick = { onOpenCategory(chip.name) })
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 2 of chips
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categoryChips15.subList(4, 8)) { chip ->
                                CategoryChipPill(chip = chip, onClick = { onOpenCategory(chip.name) })
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 3 of chips
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categoryChips15.subList(8, 12)) { chip ->
                                CategoryChipPill(chip = chip, onClick = { onOpenCategory(chip.name) })
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 4 of chips
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categoryChips15.drop(12)) { chip ->
                                CategoryChipPill(chip = chip, onClick = { onOpenCategory(chip.name) })
                            }
                        }
                    }

                    // SECTION C: "Based on actor" with "See all >"
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Based on actor",
                                color = Color(0xFFF8F8F8),
                                fontSize = 18.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onOpenCategory("Popular Actors") }
                            ) {
                                Text(
                                    text = "See all",
                                    color = Color(0xFF959CE3),
                                    fontSize = 12.sp,
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = VortexIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF959CE3),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(actorsList) { actor ->
                                Column(
                                    modifier = Modifier
                                        .width(96.dp)
                                        .clickable {
                                            searchController.onQueryChange(actor.name)
                                            searchController.performSearch(actor.name)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp, 120.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFF10121A))
                                            .border(1.dp, Color(0x33959CE3), RoundedCornerShape(16.dp))
                                    ) {
                                        AsyncImage(
                                            model = actor.photoUrl,
                                            contentDescription = actor.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = actor.name,
                                        color = Color(0xFFF8F8F8),
                                        fontSize = 12.sp,
                                        fontFamily = ManropeFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // SECTION D: "Popular search"
                    if (popularItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Popular search",
                                    color = Color(0xFFF8F8F8),
                                    fontSize = 18.sp,
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onOpenCategory("Popular search") }
                                ) {
                                    Text(
                                        text = "See all",
                                        color = Color(0xFF959CE3),
                                        fontSize = 12.sp,
                                        fontFamily = ManropeFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = VortexIcons.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF959CE3),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(popularItems) { media ->
                                    PosterCard(
                                        media = media,
                                        onClick = { onMediaClick(media) },
                                        width = 135.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SearchStatus.Searching -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listOf(1, 2, 3, 4)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(3) {
                                PosterSkeleton(
                                    modifier = Modifier.weight(1f),
                                    cornerRadius = 16.dp
                                )
                            }
                        }
                    }
                }
            }

            SearchStatus.Results -> {
                if (state.results.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found for \"${state.query}\"",
                            color = Color(0xFF959CE3),
                            fontSize = 15.sp,
                            fontFamily = ManropeFontFamily
                        )
                    }
                } else {
                    val chunked = state.results.chunked(3)
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chunked) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                    }
                }
            }

            SearchStatus.NoResults -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found for \"${state.query}\"",
                        color = Color(0xFF959CE3),
                        fontSize = 15.sp,
                        fontFamily = ManropeFontFamily
                    )
                }
            }

            SearchStatus.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.errorMessage ?: "Search failed",
                            color = Color(0xFFF8F8F8),
                            fontSize = 15.sp,
                            fontFamily = ManropeFontFamily
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { searchController.performSearch(state.query) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF959CE3))
                        ) {
                            Text("Retry", color = Color(0xFF08090B), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipPill(
    chip: GenreChipItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF10121A))
            .border(1.dp, Color(0x33959CE3), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = chip.name,
                color = Color(0xFFF8F8F8),
                fontSize = 12.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${chip.count}",
                color = Color(0xFF959CE3),
                fontSize = 11.sp,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
