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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.movievortex.android.ui.components.VortexIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movievortex.android.controller.SearchController
import com.movievortex.android.controller.SearchStatus
import com.movievortex.android.model.MovieVortexMedia
import com.movievortex.android.ui.components.PosterCard
import com.movievortex.android.ui.components.PosterSkeleton
import com.movievortex.android.ui.theme.VortexColors

@Composable
fun SearchScreen(
    searchController: SearchController,
    onMediaClick: (MovieVortexMedia) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by searchController.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VortexColors.PureBlack)
            .padding(top = 12.dp)
    ) {
        // Cyber-Glow Search Input Bar (from ui_search.png)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = VortexIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = VortexColors.PrimaryText
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Cyber-Glow Frosted Search Field with Neon Crimson Glow Ring
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(VortexColors.GlassSurfaceDark)
                    .border(1.5.dp, VortexColors.NeonCrimsonGlow, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = VortexIcons.Search,
                        contentDescription = "Search",
                        tint = VortexColors.NeonCrimson,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (state.query.isEmpty()) {
                            Text(
                                text = "Search movies, series, anime...",
                                color = VortexColors.MutedText,
                                fontSize = 14.sp
                            )
                        }

                        BasicTextField(
                            value = state.query,
                            onValueChange = { searchController.onQueryChange(it) },
                            singleLine = true,
                            cursorBrush = SolidColor(VortexColors.NeonCrimson),
                            textStyle = TextStyle(
                                color = VortexColors.PrimaryText,
                                fontSize = 14.sp,
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
                                tint = VortexColors.SecondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Content States
        when (state.status) {
            SearchStatus.Idle -> {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // 1. Trending Searches with TrendingUp Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = VortexIcons.TrendingUp,
                            contentDescription = "Trending",
                            tint = VortexColors.PrimaryText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trending Searches",
                            color = VortexColors.PrimaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    val trendingSuggestions = listOf(
                        "Severance",
                        "Interstellar",
                        "Solo Leveling",
                        "Dune: Part Two",
                        "The Batman",
                        "Arcane",
                        "Stranger Things",
                        "Oppenheimer"
                    )

                    // First row of trending chips
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(trendingSuggestions.take(4)) { title ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VortexColors.CardSurface)
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                    .clickable {
                                        searchController.onQueryChange(title)
                                        searchController.performSearch(title)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = VortexIcons.Search,
                                        contentDescription = null,
                                        tint = VortexColors.SecondaryText,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        color = VortexColors.PrimaryText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Second row of trending chips
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(trendingSuggestions.drop(4)) { title ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VortexColors.CardSurface)
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                    .clickable {
                                        searchController.onQueryChange(title)
                                        searchController.performSearch(title)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = VortexIcons.Search,
                                        contentDescription = null,
                                        tint = VortexColors.SecondaryText,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        color = VortexColors.PrimaryText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Top Genre Chips
                    Text(
                        text = "Top Genres",
                        color = VortexColors.PrimaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val topGenres = listOf("Sci-Fi", "Cyberpunk", "Anime", "Action", "Thriller")
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(topGenres) { genre ->
                            val isFirst = genre == "Sci-Fi"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isFirst) VortexColors.PrimaryBrand else VortexColors.CardSurface)
                                    .border(
                                        1.dp,
                                        if (isFirst) Color.Transparent else Color(0x33FFFFFF),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable {
                                        searchController.onQueryChange(genre)
                                        searchController.performSearch(genre)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = genre,
                                    color = if (isFirst) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Recent Searches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Searches",
                            color = VortexColors.PrimaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Clear All",
                            color = VortexColors.SecondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { searchController.clearRecentQueries() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val recentChips = if (state.recentQueries.isNotEmpty()) {
                        state.recentQueries
                    } else {
                        listOf("Interstellar", "Severance", "Solo Leveling")
                    }

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recentChips) { query ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VortexColors.CardSurface)
                                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                                    .clickable {
                                        searchController.onQueryChange(query)
                                        searchController.performSearch(query)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = query,
                                    color = VortexColors.SecondaryText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            SearchStatus.Searching -> {
                // Branded Skeletons Grid during search
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listOf(1, 2, 3, 4)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            repeat(3) {
                                PosterSkeleton(
                                    modifier = Modifier.weight(1f),
                                    cornerRadius = 12.dp
                                )
                            }
                        }
                    }
                }
            }

            SearchStatus.Results -> {
                // Responsive Poster Grid (3 columns chunked)
                val chunked = state.results.chunked(3)
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chunked) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                }
            }

            SearchStatus.NoResults -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = VortexIcons.SearchOff,
                            contentDescription = "No Results",
                            tint = VortexColors.MutedText,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results found for \"${state.query}\"",
                            color = VortexColors.PrimaryText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try searching for a different title or keyword",
                            color = VortexColors.MutedText,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            SearchStatus.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = state.errorMessage ?: "This content is currently unavailable.",
                            color = VortexColors.PrimaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Please check your network and try again.",
                            color = VortexColors.MutedText,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { searchController.performSearch(state.query) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VortexColors.PrimaryBrand
                            )
                        ) {
                            Text(text = "Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
