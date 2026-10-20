import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:flutter_staggered_animations/flutter_staggered_animations.dart';
import '../models/media_item.dart';
import '../models/search_suggestion.dart';
import '../services/search_service.dart';
import '../theme/monochrome_theme.dart';
import '../widgets/movie_card.dart';
import '../widgets/shimmer_box.dart';

class SearchScreen extends StatefulWidget {
  final Function(MediaItem) onMediaClick;

  const SearchScreen({
    Key? key,
    required this.onMediaClick,
  }) : super(key: key);

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final TextEditingController _searchController = TextEditingController();
  final FocusNode _searchFocusNode = FocusNode();
  String _activeGenreFilter = 'All';

  final List<String> _quickFilters = const [
    'All',
    'Trending',
    'Sci-Fi',
    'Anime',
    'Action',
    '4K UHD',
    'Series',
  ];

  @override
  void dispose() {
    _searchController.dispose();
    _searchFocusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final searchService = Provider.of<SearchService>(context);

    return Scaffold(
      backgroundColor: MonochromeTheme.oledBlack,
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 12),

            // 1. Futuristic Monochrome Search Input Pill
            _buildSearchInput(searchService),

            const SizedBox(height: 14),

            // 2. Quick Genre / Category Filter Chips
            _buildGenreChips(searchService),

            const SizedBox(height: 10),

            // 3. Body: Trending Suggestions vs Search Results
            Expanded(
              child: searchService.isLoading
                  ? _buildLoadingGrid()
                  : searchService.query.isEmpty
                      ? _buildTrendingAndSuggestions(searchService)
                      : _buildSearchResults(searchService),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSearchInput(SearchService searchService) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      height: 54,
      decoration: BoxDecoration(
        color: MonochromeTheme.cardDark,
        borderRadius: BorderRadius.circular(27),
        border: Border.all(
          color: _searchFocusNode.hasFocus
              ? Colors.white
              : Colors.white.withOpacity(0.18),
          width: 1.2,
        ),
        boxShadow: [
          if (_searchFocusNode.hasFocus)
            BoxShadow(
              color: Colors.white.withOpacity(0.15),
              blurRadius: 12,
              spreadRadius: 1,
            ),
        ],
      ),
      child: Row(
        children: [
          const SizedBox(width: 16),
          Icon(
            Icons.search_rounded,
            color: Colors.white.withOpacity(0.7),
            size: 22,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: TextField(
              controller: _searchController,
              focusNode: _searchFocusNode,
              onChanged: (text) {
                searchService.updateQuery(text);
              },
              style: GoogleFonts.manrope(
                color: Colors.white,
                fontSize: 15,
                fontWeight: FontWeight.w600,
              ),
              cursorColor: Colors.white,
              decoration: InputDecoration(
                hintText: 'Search movies, series, anime...',
                hintStyle: GoogleFonts.manrope(
                  color: MonochromeTheme.textMuted,
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                ),
                border: InputBorder.none,
                isDense: true,
              ),
            ),
          ),
          if (_searchController.text.isNotEmpty)
            IconButton(
              icon: const Icon(Icons.clear, color: Colors.white, size: 18),
              onPressed: () {
                _searchController.clear();
                searchService.updateQuery('');
              },
            ),
          const SizedBox(width: 4),
        ],
      ),
    );
  }

  Widget _buildGenreChips(SearchService searchService) {
    return SizedBox(
      height: 36,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: _quickFilters.length,
        itemBuilder: (context, index) {
          final filter = _quickFilters[index];
          final isSelected = _activeGenreFilter == filter;

          return GestureDetector(
            onTap: () {
              setState(() => _activeGenreFilter = filter);
              if (filter != 'All') {
                _searchController.text = filter;
                searchService.updateQuery(filter);
              } else {
                _searchController.clear();
                searchService.updateQuery('');
              }
            },
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              margin: const EdgeInsets.only(right: 8),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: isSelected ? Colors.white : MonochromeTheme.cardDark,
                borderRadius: BorderRadius.circular(18),
                border: Border.all(
                  color: isSelected ? Colors.white : Colors.white.withOpacity(0.14),
                  width: 1,
                ),
              ),
              child: Center(
                child: Text(
                  filter,
                  style: GoogleFonts.manrope(
                    color: isSelected ? Colors.black : Colors.white,
                    fontSize: 12,
                    fontWeight: isSelected ? FontWeight.w800 : FontWeight.w600,
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  // Primary Trending Movies & Smart Suggestions
  Widget _buildTrendingAndSuggestions(SearchService searchService) {
    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      children: [
        // 1. Recent Searches Row
        if (searchService.recentSearches.isNotEmpty) ...[
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Recent Searches',
                style: GoogleFonts.manrope(
                  color: Colors.white,
                  fontSize: 15,
                  fontWeight: FontWeight.w800,
                  letterSpacing: -0.2,
                ),
              ),
              GestureDetector(
                onTap: () => searchService.clearRecentSearches(),
                child: Text(
                  'Clear All',
                  style: GoogleFonts.manrope(
                    color: MonochromeTheme.textMuted,
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: searchService.recentSearches.map((keyword) {
              return GestureDetector(
                onTap: () {
                  _searchController.text = keyword;
                  searchService.updateQuery(keyword);
                },
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                  decoration: BoxDecoration(
                    color: MonochromeTheme.cardDark,
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(
                      color: Colors.white.withOpacity(0.15),
                      width: 1,
                    ),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.history, color: Colors.white54, size: 14),
                      const SizedBox(width: 6),
                      Text(
                        keyword,
                        style: GoogleFonts.manrope(
                          color: Colors.white,
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
              );
            }).toList(),
          ),
          const SizedBox(height: 24),
        ],

        // 2. Trending Movie Suggestions (Title + Subtitle + Poster)
        Row(
          children: [
            Container(
              padding: const EdgeInsets.all(4),
              decoration: const BoxDecoration(
                color: Colors.white,
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.trending_up, color: Colors.black, size: 14),
            ),
            const SizedBox(width: 8),
            Text(
              'Trending Searches & Suggestions',
              style: GoogleFonts.manrope(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.w800,
                letterSpacing: -0.3,
              ),
            ),
          ],
        ),

        const SizedBox(height: 12),

        // List of Trending Suggestions
        ...searchService.trendingSuggestions.map((suggestion) {
          return _buildSuggestionItem(suggestion, searchService);
        }).toList(),
      ],
    );
  }

  Widget _buildSuggestionItem(SearchSuggestion suggestion, SearchService searchService) {
    return GestureDetector(
      onTap: () {
        _searchController.text = suggestion.title;
        searchService.updateQuery(suggestion.title);
      },
      child: Container(
        margin: const EdgeInsets.only(bottom: 10),
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          color: MonochromeTheme.cardDark,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: Colors.white.withOpacity(0.08),
            width: 1,
          ),
        ),
        child: Row(
          children: [
            // Thumbnail Poster
            ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: Container(
                width: 48,
                height: 64,
                color: const Color(0xFF1E1E1E),
                child: suggestion.poster != null
                    ? CachedNetworkImage(
                        imageUrl: suggestion.poster!,
                        fit: BoxFit.cover,
                        errorWidget: (_, __, ___) => const Icon(Icons.movie, color: Colors.white24),
                      )
                    : const Icon(Icons.movie, color: Colors.white24),
              ),
            ),

            const SizedBox(width: 14),

            // Information
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        suggestion.title,
                        style: GoogleFonts.manrope(
                          color: Colors.white,
                          fontSize: 14,
                          fontWeight: FontWeight.w700,
                          letterSpacing: -0.2,
                        ),
                      ),
                      const SizedBox(width: 6),
                      if (suggestion.isTrending)
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1.5),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: Text(
                            'HOT',
                            style: GoogleFonts.manrope(
                              color: Colors.black,
                              fontSize: 8,
                              fontWeight: FontWeight.w900,
                            ),
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    suggestion.subtitle ?? '4K Ultra HD Streaming',
                    style: GoogleFonts.manrope(
                      color: MonochromeTheme.textMuted,
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
            ),

            // Rating / Arrow
            if (suggestion.rating != null) ...[
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 3),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.star, color: Colors.white, size: 11),
                    const SizedBox(width: 3),
                    Text(
                      suggestion.rating!.toStringAsFixed(1),
                      style: GoogleFonts.manrope(
                        color: Colors.white,
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
            ],

            Icon(
              Icons.north_west_rounded,
              color: Colors.white.withOpacity(0.4),
              size: 16,
            ),
          ],
        ),
      ),
    );
  }

  // Search Results Grid with Staggered Animation
  Widget _buildSearchResults(SearchService searchService) {
    if (searchService.results.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.search_off_rounded,
              color: Colors.white.withOpacity(0.3),
              size: 54,
            ),
            const SizedBox(height: 12),
            Text(
              'No results found for "${searchService.query}"',
              style: GoogleFonts.manrope(
                color: Colors.white,
                fontSize: 15,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              'Try searching with a different title or keyword',
              style: GoogleFonts.manrope(
                color: MonochromeTheme.textMuted,
                fontSize: 13,
              ),
            ),
          ],
        ),
      );
    }

    return AnimationLimiter(
      child: GridView.builder(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 3,
          childAspectRatio: 0.58,
          crossAxisSpacing: 12,
          mainAxisSpacing: 16,
        ),
        itemCount: searchService.results.length,
        itemBuilder: (context, index) {
          final item = searchService.results[index];
          return AnimationConfiguration.staggeredGrid(
            position: index,
            duration: const Duration(milliseconds: 350),
            columnCount: 3,
            child: ScaleAnimation(
              child: FadeInAnimation(
                child: MovieCard(
                  item: item,
                  onTap: () => widget.onMediaClick(item),
                  width: double.infinity,
                  height: 155,
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildLoadingGrid() {
    return GridView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        childAspectRatio: 0.58,
        crossAxisSpacing: 12,
        mainAxisSpacing: 16,
      ),
      itemCount: 9,
      itemBuilder: (_, __) {
        return const ShimmerBox(width: double.infinity, height: 180, borderRadius: 14);
      },
    );
  }
}
