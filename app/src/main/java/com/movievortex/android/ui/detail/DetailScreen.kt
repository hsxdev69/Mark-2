package com.movievortex.android.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.movievortex.android.controller.DetailController
import com.movievortex.android.model.MediaEpisode
import com.movievortex.android.model.MediaType
import com.movievortex.android.model.MovieVortexMedia
import com.movievortex.android.ui.components.VortexIcons
import com.movievortex.android.ui.theme.ManropeFontFamily

data class MockCast(val name: String, val photoUrl: String)

@Composable
fun DetailScreen(
    detailController: DetailController,
    onPlayMovie: (MovieVortexMedia) -> Unit,
    onPlayEpisode: (MovieVortexMedia, MediaEpisode) -> Unit,
    onDownloadClick: (MovieVortexMedia, MediaEpisode?) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by detailController.uiState.collectAsState()
    val media = state.media

    if (state.isLoading && media == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0B0E14)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
        return
    }

    if (media == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0B0E14)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.errorMessage ?: "Details unavailable",
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = ManropeFontFamily
            )
        }
        return
    }

    // Cast mock fallback if API doesn't provide photos
    val displayCast = remember(media) {
        listOf(
            MockCast("Jason Momoa", "https://image.tmdb.org/t/p/w200/6dEFBpZH8i8xy5nmQIHp9AQbIRw.jpg"),
            MockCast("Jack Black", "https://image.tmdb.org/t/p/w200/rtCxbtY9PocSTNmqq9EngVGZ9dn.jpg"),
            MockCast("Sebastian Eugene", "https://image.tmdb.org/t/p/w200/7WsyChvg4rGkWwh6c2E1lIeStY8.jpg"),
            MockCast("Emma Myers", "https://image.tmdb.org/t/p/w200/peL687mJ0uD6JzS4v0b3W0l8Z9n.jpg"),
            MockCast("Danielle Brooks", "https://image.tmdb.org/t/p/w200/7aQjR9J7uD6JzS4v0b3W0l8Z9n.jpg")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14)),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Top Cinematic Backdrop Banner with Floating Play Button (Exact Video Match - Frame 8)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Curved Backdrop Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                        .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                        .background(Color(0xFF141923))
                ) {
                    AsyncImage(
                        model = media.backdrop ?: media.poster,
                        contentDescription = media.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Subtle Dark Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x99000000),
                                        Color.Transparent,
                                        Color(0x990B0E14)
                                    )
                                )
                            )
                    )

                    // Top Actions: Back Button and More Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Frosted Glass Squircle Back Button (Video Concept - Frame 8)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = Color(0x66000000))
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x4D1A222B))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color(0x4DFFFFFF),
                                            Color(0x18FFFFFF)
                                        )
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable(onClick = onBackClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = VortexIcons.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Frosted Glass Squircle More Options Button (Video Concept - Frame 8)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = Color(0x66000000))
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x4D1A222B))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color(0x4DFFFFFF),
                                            Color(0x18FFFFFF)
                                        )
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { /* More */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = VortexIcons.MoreVert,
                                contentDescription = "More",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Floating Circular Frosted Glass Play Button matching video (video_play_btn)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 0.dp)
                        .size(58.dp)
                        .shadow(18.dp, CircleShape, spotColor = Color(0x99000000))
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xB3434E5A),
                                    Color(0xCC2B343E),
                                    Color(0xE61F262F)
                                )
                            )
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0x66FFFFFF),
                                    Color(0x22FFFFFF)
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable {
                            if (media.type == MediaType.TvSeries || media.type == MediaType.Anime) {
                                state.currentSeasonEpisodes.firstOrNull()?.let { ep ->
                                    onPlayEpisode(media, ep)
                                } ?: onPlayMovie(media)
                            } else {
                                onPlayMovie(media)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = VortexIcons.Play,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .offset(x = 2.dp)
                    )
                }
            }
        }

        // 2. Movie Info: Date, Title, Metadata Chips, Rating & Likes (Frame 8)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Release Date (e.g. "April 4, 2025")
                val releaseDateText = media.year?.let { "April 4, $it" } ?: "2025"
                Text(
                    text = releaseDateText,
                    color = Color(0xFF8E95A3),
                    fontSize = 13.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Title (e.g. "Minecraft Movie")
                Text(
                    text = media.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Metadata Pill Chips: Duration | Genre | Type | Age Rating
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val runtimeText = media.runtime?.let { "${it / 60}h ${it % 60}min" } ?: "1h 41min"
                    MetadataChip(text = runtimeText)

                    val genre = media.genres.firstOrNull() ?: "Fantasy"
                    MetadataChip(text = genre)

                    val typeLabel = if (media.type == MediaType.TvSeries) "Series" else "Movie"
                    MetadataChip(text = typeLabel)

                    MetadataChip(text = "6+")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rating & Stats Row: ★ 6.2/10 60K votes | 👍 15 950 | ❤️ 156
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Star Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "★",
                            color = Color(0xFFFFC107),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val rating = media.rating ?: 6.2f
                        Text(
                            text = "%.1f/10".format(rating),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "60K votes",
                            color = Color(0xFF8E95A3),
                            fontSize = 12.sp,
                            fontFamily = ManropeFontFamily
                        )
                    }

                    // Like & Bookmark Counts
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Likes
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = VortexIcons.ThumbUp,
                                contentDescription = "Likes",
                                tint = Color(0xFF8E95A3),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "15 950",
                                color = Color(0xFF8E95A3),
                                fontSize = 12.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Bookmark / Heart (Watchlist toggle)
                        Row(
                            modifier = Modifier.clickable { detailController.toggleMyList() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isInMyList) VortexIcons.Heart else VortexIcons.HeartBorder,
                                contentDescription = "Favorite",
                                tint = if (state.isInMyList) Color(0xFFE53448) else Color(0xFF8E95A3),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (state.isInMyList) "Saved" else "156",
                                color = if (state.isInMyList) Color(0xFFE53448) else Color(0xFF8E95A3),
                                fontSize = 12.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 3. Cast Section (Horizontal rounded portraits - Frame 8)
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
                    text = "Cast",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayCast) { actor ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(85.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(85.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF161B26))
                                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
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
                            color = Color(0xFFCCD0CF),
                            fontSize = 11.sp,
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. Synopsis Section (Frame 8)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Synopsis",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                val synopsisText = media.synopsis?.takeIf { it.isNotBlank() }
                    ?: "Four misfits are suddenly pulled through a mysterious portal into a bizarre cubic wonderland that thrives on imagination."
                Text(
                    text = synopsisText,
                    color = Color(0xFF9BA8AB),
                    fontSize = 13.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp
                )
            }
        }

        // 5. TV Series Seasons & Episodes (if series/anime)
        if (media.type == MediaType.TvSeries || media.type == MediaType.Anime) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Episodes",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(state.currentSeasonEpisodes) { ep ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141923))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
                        .clickable { onPlayEpisode(media, ep) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "EP ${ep.episodeNumber}: ${ep.title.ifBlank { "Episode ${ep.episodeNumber}" }}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                            ep.synopsis?.let { desc ->
                                Text(
                                    text = desc,
                                    color = Color(0xFF8E95A3),
                                    fontSize = 11.sp,
                                    fontFamily = ManropeFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = VortexIcons.Play,
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x38354354))
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x3DFFFFFF),
                        Color(0x14FFFFFF)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFE5E7EB),
            fontSize = 12.sp,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}
