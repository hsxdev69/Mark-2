import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../models/media_item.dart';
import '../services/backend_bridge.dart';
import '../theme/monochrome_theme.dart';
import '../widgets/hero_carousel.dart';
import '../widgets/movie_card.dart';
import '../widgets/shimmer_box.dart';

class HomeScreen extends StatefulWidget {
  final Function(MediaItem) onMediaClick;
  final Function(MediaItem) onPlayClick;

  const HomeScreen({
    Key? key,
    required this.onMediaClick,
    required this.onPlayClick,
  }) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  bool _isLoading = true;
  List<MediaItem> _heroItems = [];
  List<MediaItem> _continueWatching = [];
  List<MediaItem> _trending = [];
  String _selectedCategory = 'All';

  final List<String> _categories = const [
    'All',
    'Movies',
    'Series',
    'Anime',
    '4K UHD',
    'Documentaries',
  ];

  @override
  void initState() {
    super.initState();
    _loadContent();
  }

  Future<void> _loadContent() async {
    setState(() => _isLoading = true);
    final data = await BackendBridge.getHomeContent();

    final heroList = (data['hero'] as List<dynamic>?)
            ?.map((e) => MediaItem.fromJson(e))
            .toList() ??
        [];
    final continueList = (data['continueWatching'] as List<dynamic>?)
            ?.map((e) => MediaItem.fromJson(e))
            .toList() ??
        [];
    final trendingList = (data['trending'] as List<dynamic>?)
            ?.map((e) => MediaItem.fromJson(e))
            .toList() ??
        [];

    if (mounted) {
      setState(() {
        _heroItems = heroList;
        _continueWatching = continueList;
        _trending = trendingList;
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return Scaffold(
        backgroundColor: MonochromeTheme.oledBlack,
        body: ListView(
          padding: EdgeInsets.zero,
          children: [
            const ShimmerBox(width: double.infinity, height: 420, borderRadius: 0),
            const SizedBox(height: 24),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Row(
                children: const [
                  ShimmerBox(width: 80, height: 32, borderRadius: 16),
                  SizedBox(width: 8),
                  ShimmerBox(width: 80, height: 32, borderRadius: 16),
                  SizedBox(width: 8),
                  ShimmerBox(width: 80, height: 32, borderRadius: 16),
                ],
              ),
            ),
          ],
        ),
      );
    }

    return Scaffold(
      backgroundColor: MonochromeTheme.oledBlack,
      body: RefreshIndicator(
        onRefresh: _loadContent,
        color: Colors.white,
        backgroundColor: MonochromeTheme.cardDark,
        child: ListView(
          padding: const EdgeInsets.only(bottom: 96),
          children: [
            // 1. Sleek Hero Carousel
            HeroCarousel(
              items: _heroItems,
              onTap: widget.onMediaClick,
              onWatchNow: widget.onPlayClick,
              onAddToList: (item) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text(
                      'Added "${item.title}" to My List',
                      style: GoogleFonts.manrope(color: Colors.black, fontWeight: FontWeight.w700),
                    ),
                    backgroundColor: Colors.white,
                    behavior: SnackBarBehavior.floating,
                    shape: RoundedCornerShape(12),
                  ),
                );
              },
            ),

            const SizedBox(height: 16),

            // 2. Monochrome Category Filter Tabs
            _buildCategoryTabs(),

            const SizedBox(height: 20),

            // 3. Continue Watching Section (if any)
            if (_continueWatching.isNotEmpty) ...[
              _buildSectionHeader('Continue Watching', icon: Icons.play_circle_outline_rounded),
              const SizedBox(height: 12),
              _buildContinueWatchingRow(),
              const SizedBox(height: 24),
            ],

            // 4. Trending Now Horizontal Section
            _buildSectionHeader('Trending Now', icon: Icons.local_fire_department_rounded),
            const SizedBox(height: 12),
            _buildMediaHorizontalRow(_trending),

            const SizedBox(height: 24),

            // 5. Popular Blockbusters
            _buildSectionHeader('Blockbusters & Originals', icon: Icons.star_border_rounded),
            const SizedBox(height: 12),
            _buildMediaHorizontalRow(_heroItems),
          ],
        ),
      ),
    );
  }

  Widget _buildCategoryTabs() {
    return SizedBox(
      height: 38,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: _categories.length,
        itemBuilder: (context, index) {
          final cat = _categories[index];
          final isSelected = _selectedCategory == cat;

          return GestureDetector(
            onTap: () => setState(() => _selectedCategory = cat),
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              margin: const EdgeInsets.only(right: 8),
              padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 8),
              decoration: BoxDecoration(
                color: isSelected ? Colors.white : MonochromeTheme.cardDark,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: isSelected ? Colors.white : Colors.white.withOpacity(0.12),
                  width: 1,
                ),
              ),
              child: Center(
                child: Text(
                  cat,
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

  Widget _buildSectionHeader(String title, {IconData? icon}) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              if (icon != null) ...[
                Icon(icon, color: Colors.white, size: 18),
                const SizedBox(width: 8),
              ],
              Text(
                title,
                style: GoogleFonts.manrope(
                  color: Colors.white,
                  fontSize: 17,
                  fontWeight: FontWeight.w800,
                  letterSpacing: -0.3,
                ),
              ),
            ],
          ),
          Text(
            'See All',
            style: GoogleFonts.manrope(
              color: MonochromeTheme.textMuted,
              fontSize: 12,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildContinueWatchingRow() {
    return SizedBox(
      height: 120,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: _continueWatching.length,
        itemBuilder: (context, index) {
          final item = _continueWatching[index];
          return GestureDetector(
            onTap: () => widget.onPlayClick(item),
            child: Container(
              width: 220,
              margin: const EdgeInsets.only(right: 14),
              decoration: BoxDecoration(
                color: MonochromeTheme.cardDark,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: Colors.white.withOpacity(0.12),
                  width: 1,
                ),
              ),
              child: Row(
                children: [
                  // Poster Thumbnail
                  ClipRRect(
                    borderRadius: const BorderRadius.only(
                      topLeft: Radius.circular(15),
                      bottomLeft: Radius.circular(15),
                    ),
                    child: SizedBox(
                      width: 85,
                      height: 120,
                      child: item.poster != null
                          ? CachedNetworkImage(
                              imageUrl: item.poster!,
                              fit: BoxFit.cover,
                            )
                          : Container(color: Colors.white12),
                    ),
                  ),

                  // Info & Progress
                  Expanded(
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(
                            item.title,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: GoogleFonts.manrope(
                              color: Colors.white,
                              fontSize: 13,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'Resume playback',
                            style: GoogleFonts.manrope(
                              color: MonochromeTheme.textMuted,
                              fontSize: 11,
                            ),
                          ),
                          const SizedBox(height: 10),
                          // Linear Progress Bar
                          ClipRRect(
                            borderRadius: BorderRadius.circular(3),
                            child: LinearProgressIndicator(
                              value: item.watchProgress.clamp(0.0, 1.0),
                              backgroundColor: Colors.white.withOpacity(0.15),
                              valueColor: const AlwaysStoppedAnimation<Color>(Colors.white),
                              minHeight: 4,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildMediaHorizontalRow(List<MediaItem> list) {
    return SizedBox(
      height: 250,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: list.length,
        itemBuilder: (context, index) {
          final item = list[index];
          return MovieCard(
            item: item,
            onTap: () => widget.onMediaClick(item),
          );
        },
      ),
    );
  }
}
