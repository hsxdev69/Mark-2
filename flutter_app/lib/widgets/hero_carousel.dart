import 'dart:async';
import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:google_fonts/google_fonts.dart';
import '../models/media_item.dart';
import '../theme/monochrome_theme.dart';
import 'shimmer_box.dart';

class HeroCarousel extends StatefulWidget {
  final List<MediaItem> items;
  final Function(MediaItem) onWatchNow;
  final Function(MediaItem) onAddToList;
  final Function(MediaItem) onTap;

  const HeroCarousel({
    Key? key,
    required this.items,
    required this.onWatchNow,
    required this.onAddToList,
    required this.onTap,
  }) : super(key: key);

  @override
  State<HeroCarousel> createState() => _HeroCarouselState();
}

class _HeroCarouselState extends State<HeroCarousel> {
  final PageController _pageController = PageController();
  int _currentPage = 0;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _startAutoScroll();
  }

  void _startAutoScroll() {
    _timer = Timer.periodic(const Duration(seconds: 6), (timer) {
      if (widget.items.isEmpty) return;
      int nextPage = _currentPage + 1;
      if (nextPage >= widget.items.length) {
        nextPage = 0;
      }
      if (_pageController.hasClients) {
        _pageController.animateToPage(
          nextPage,
          duration: const Duration(milliseconds: 700),
          curve: Curves.fastOutSlowIn,
        );
      }
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (widget.items.isEmpty) {
      return const ShimmerBox(width: double.infinity, height: 420, borderRadius: 0);
    }

    final screenHeight = MediaQuery.of(context).size.height;
    final heroHeight = (screenHeight * 0.48).clamp(380.0, 480.0);

    return SizedBox(
      height: heroHeight,
      child: Stack(
        children: [
          // PageView for Smooth Carousel Slides
          PageView.builder(
            controller: _pageController,
            itemCount: widget.items.length,
            onPageChanged: (index) {
              setState(() => _currentPage = index);
            },
            itemBuilder: (context, index) {
              final item = widget.items[index];
              final imageUrl = item.backdrop ?? item.poster ?? '';

              return GestureDetector(
                onTap: () => widget.onTap(item),
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    // Backdrop Image
                    if (imageUrl.isNotEmpty)
                      CachedNetworkImage(
                        imageUrl: imageUrl,
                        fit: BoxFit.cover,
                        placeholder: (_, __) => const ShimmerBox(
                          width: double.infinity,
                          height: double.infinity,
                          borderRadius: 0,
                        ),
                        errorWidget: (_, __, ___) => Container(color: MonochromeTheme.surfaceDark),
                      )
                    else
                      Container(color: MonochromeTheme.surfaceDark),

                    // Ultra-Smooth Monochrome Gradient Overlays
                    Container(
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: [
                            Colors.black.withOpacity(0.5),
                            Colors.transparent,
                            Colors.black.withOpacity(0.7),
                            MonochromeTheme.oledBlack,
                          ],
                          stops: const [0.0, 0.35, 0.75, 1.0],
                        ),
                      ),
                    ),

                    // Content Information Overlay
                    Positioned(
                      bottom: 24,
                      left: 20,
                      right: 20,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          // Genre Tags / Rating Row
                          Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                                decoration: BoxDecoration(
                                  color: Colors.white,
                                  borderRadius: BorderRadius.circular(6),
                                ),
                                child: Text(
                                  'PREMIER 4K',
                                  style: GoogleFonts.manrope(
                                    color: Colors.black,
                                    fontSize: 10,
                                    fontWeight: FontWeight.w900,
                                    letterSpacing: 0.5,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 8),
                              if (item.rating > 0) ...[
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                                  decoration: BoxDecoration(
                                    color: Colors.black.withOpacity(0.8),
                                    borderRadius: BorderRadius.circular(6),
                                    border: Border.all(
                                      color: Colors.white.withOpacity(0.3),
                                      width: 0.8,
                                    ),
                                  ),
                                  child: Row(
                                    children: [
                                      const Icon(Icons.star, color: Colors.white, size: 12),
                                      const SizedBox(width: 4),
                                      Text(
                                        item.rating.toStringAsFixed(1),
                                        style: GoogleFonts.manrope(
                                          color: Colors.white,
                                          fontSize: 10,
                                          fontWeight: FontWeight.w800,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                const SizedBox(width: 8),
                              ],
                              Text(
                                item.genres.take(2).join(' • '),
                                style: GoogleFonts.manrope(
                                  color: MonochromeTheme.textSecondary,
                                  fontSize: 12,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ],
                          ),

                          const SizedBox(height: 8),

                          // Title
                          Text(
                            item.title,
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                            style: GoogleFonts.manrope(
                              color: MonochromeTheme.pureWhite,
                              fontSize: 26,
                              fontWeight: FontWeight.w900,
                              letterSpacing: -0.6,
                              height: 1.15,
                            ),
                          ),

                          const SizedBox(height: 16),

                          // Action Buttons: Watch Now & + My List
                          Row(
                            children: [
                              // Watch Now (Pure White Button, Black Text)
                              Expanded(
                                flex: 3,
                                child: ElevatedButton.icon(
                                  onPressed: () => widget.onWatchNow(item),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: MonochromeTheme.pureWhite,
                                    foregroundColor: MonochromeTheme.oledBlack,
                                    elevation: 0,
                                    padding: const EdgeInsets.symmetric(vertical: 14),
                                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                                  ),
                                  icon: const Icon(Icons.play_arrow_rounded, size: 20),
                                  label: Text(
                                    'Watch Now',
                                    style: GoogleFonts.manrope(
                                      fontSize: 14,
                                      fontWeight: FontWeight.w800,
                                      letterSpacing: -0.2,
                                    ),
                                  ),
                                ),
                              ),

                              const SizedBox(width: 12),

                              // My List (Glassmorphic Button)
                              Expanded(
                                flex: 2,
                                child: OutlinedButton.icon(
                                  onPressed: () => widget.onAddToList(item),
                                  style: OutlinedButton.styleFrom(
                                    backgroundColor: Colors.white.withOpacity(0.08),
                                    foregroundColor: MonochromeTheme.pureWhite,
                                    side: BorderSide(
                                      color: Colors.white.withOpacity(0.25),
                                      width: 1,
                                    ),
                                    padding: const EdgeInsets.symmetric(vertical: 14),
                                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                                  ),
                                  icon: const Icon(Icons.add, size: 18),
                                  label: Text(
                                    'My List',
                                    style: GoogleFonts.manrope(
                                      fontSize: 13,
                                      fontWeight: FontWeight.w700,
                                    ),
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              );
            },
          ),

          // Top Header Bar Overlay
          Positioned(
            top: 48,
            left: 20,
            right: 20,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                // Minimalist Brand Logo & Name
                Row(
                  children: [
                    Container(
                      width: 32,
                      height: 32,
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Center(
                        child: Text(
                          'M',
                          style: TextStyle(
                            color: Colors.black,
                            fontWeight: FontWeight.w900,
                            fontSize: 18,
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Text(
                      'MovieVortex',
                      style: GoogleFonts.manrope(
                        color: Colors.white,
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                        letterSpacing: -0.4,
                      ),
                    ),
                  ],
                ),

                // Cast & Notification Icons
                Row(
                  children: [
                    _buildTopIcon(Icons.cast_rounded),
                    const SizedBox(width: 8),
                    _buildTopIcon(Icons.notifications_none_rounded),
                  ],
                ),
              ],
            ),
          ),

          // Animated Dots Indicator
          Positioned(
            bottom: 6,
            left: 0,
            right: 0,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(
                widget.items.length,
                (index) => AnimatedContainer(
                  duration: const Duration(milliseconds: 300),
                  margin: const EdgeInsets.symmetric(horizontal: 3),
                  width: _currentPage == index ? 22 : 6,
                  height: 4,
                  decoration: BoxDecoration(
                    color: _currentPage == index
                        ? Colors.white
                        : Colors.white.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTopIcon(IconData icon) {
    return Container(
      width: 38,
      height: 38,
      decoration: BoxDecoration(
        color: Colors.black.withOpacity(0.55),
        shape: BoxShape.circle,
        border: Border.all(
          color: Colors.white.withOpacity(0.18),
          width: 0.8,
        ),
      ),
      child: Icon(icon, color: Colors.white, size: 18),
    );
  }
}
