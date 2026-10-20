import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:google_fonts/google_fonts.dart';
import '../models/media_item.dart';
import '../theme/monochrome_theme.dart';
import 'shimmer_box.dart';

class MovieCard extends StatefulWidget {
  final MediaItem item;
  final VoidCallback onTap;
  final double width;
  final double height;

  const MovieCard({
    Key? key,
    required this.item,
    required this.onTap,
    this.width = 135,
    this.height = 200,
  }) : super(key: key);

  @override
  State<MovieCard> createState() => _MovieCardState();
}

class _MovieCardState extends State<MovieCard> with SingleTickerProviderStateMixin {
  late AnimationController _animController;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    _animController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 120),
    );
    _scaleAnimation = Tween<double>(begin: 1.0, end: 0.96).animate(
      CurvedAnimation(parent: _animController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _animController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTapDown: (_) => _animController.forward(),
      onTapUp: (_) {
        _animController.reverse();
        widget.onTap();
      },
      onTapCancel: () => _animController.reverse(),
      child: ScaleTransition(
        scale: _scaleAnimation,
        child: Container(
          width: widget.width,
          margin: const EdgeInsets.only(right: 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Poster Image Box
              Container(
                width: widget.width,
                height: widget.height,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(16),
                  color: MonochromeTheme.cardDark,
                  border: Border.all(
                    color: MonochromeTheme.pureWhite.withOpacity(0.12),
                    width: 1,
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.4),
                      blurRadius: 8,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(15),
                  child: Stack(
                    fit: StackFit.expand,
                    children: [
                      if (widget.item.poster != null && widget.item.poster!.isNotEmpty)
                        CachedNetworkImage(
                          imageUrl: widget.item.poster!,
                          fit: BoxFit.cover,
                          placeholder: (_, __) => ShimmerBox(
                            width: widget.width,
                            height: widget.height,
                            borderRadius: 15,
                          ),
                          errorWidget: (_, __, ___) => _buildPlaceholder(),
                        )
                      else
                        _buildPlaceholder(),

                      // Top Badges (4K UHD & Rating)
                      Positioned(
                        top: 8,
                        left: 8,
                        right: 8,
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                              decoration: BoxDecoration(
                                color: Colors.black.withOpacity(0.75),
                                borderRadius: BorderRadius.circular(6),
                                border: Border.all(
                                  color: Colors.white.withOpacity(0.3),
                                  width: 0.8,
                                ),
                              ),
                              child: Text(
                                '4K UHD',
                                style: GoogleFonts.manrope(
                                  color: Colors.white,
                                  fontSize: 9,
                                  fontWeight: FontWeight.w800,
                                ),
                              ),
                            ),
                            if (widget.item.rating > 0)
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                decoration: BoxDecoration(
                                  color: Colors.black.withOpacity(0.75),
                                  borderRadius: BorderRadius.circular(6),
                                  border: Border.all(
                                    color: Colors.white.withOpacity(0.3),
                                    width: 0.8,
                                  ),
                                ),
                                child: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    const Icon(Icons.star, color: Colors.white, size: 10),
                                    const SizedBox(width: 3),
                                    Text(
                                      widget.item.rating.toStringAsFixed(1),
                                      style: GoogleFonts.manrope(
                                        color: Colors.white,
                                        fontSize: 9,
                                        fontWeight: FontWeight.w700,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                          ],
                        ),
                      ),

                      // Continue Watching Progress Bar (if active)
                      if (widget.item.watchProgress > 0)
                        Positioned(
                          bottom: 0,
                          left: 0,
                          right: 0,
                          child: Container(
                            height: 3,
                            color: Colors.white.withOpacity(0.2),
                            child: FractionallySizedBox(
                              alignment: Alignment.centerLeft,
                              widthFactor: widget.item.watchProgress.clamp(0.0, 1.0),
                              child: Container(color: Colors.white),
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 8),

              // Title
              Text(
                widget.item.title,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: GoogleFonts.manrope(
                  color: MonochromeTheme.pureWhite,
                  fontSize: 13,
                  fontWeight: FontWeight.w700,
                  letterSpacing: -0.2,
                ),
              ),

              const SizedBox(height: 2),

              // Subtitle / Year / Genre
              Text(
                widget.item.year != null ? '${widget.item.year} • 4K' : 'HD Streaming',
                style: GoogleFonts.manrope(
                  color: MonochromeTheme.textMuted,
                  fontSize: 11,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPlaceholder() {
    return Container(
      color: MonochromeTheme.cardDark,
      child: Center(
        child: Icon(
          Icons.movie_outlined,
          color: Colors.white.withOpacity(0.3),
          size: 36,
        ),
      ),
    );
  }
}
