import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:google_fonts/google_fonts.dart';
import '../models/media_item.dart';
import '../services/backend_bridge.dart';
import '../theme/monochrome_theme.dart';

class DetailScreen extends StatefulWidget {
  final MediaItem media;

  const DetailScreen({Key? key, required this.media}) : super(key: key);

  @override
  State<DetailScreen> createState() => _DetailScreenState();
}

class _DetailScreenState extends State<DetailScreen> {
  bool _isBookmarked = false;

  @override
  void initState() {
    super.initState();
    _isBookmarked = widget.media.isInMyList;
  }

  @override
  Widget build(BuildContext context) {
    final imageUrl = widget.media.backdrop ?? widget.media.poster ?? '';

    return Scaffold(
      backgroundColor: MonochromeTheme.oledBlack,
      body: CustomScrollView(
        slivers: [
          // Collapsible Backdrop App Bar
          SliverAppBar(
            expandedHeight: 360,
            pinned: true,
            backgroundColor: MonochromeTheme.oledBlack,
            leading: IconButton(
              icon: Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: Colors.black.withOpacity(0.6),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.arrow_back, color: Colors.white, size: 20),
              ),
              onPressed: () => Navigator.pop(context),
            ),
            actions: [
              IconButton(
                icon: Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: Colors.black.withOpacity(0.6),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(
                    _isBookmarked ? Icons.bookmark : Icons.bookmark_border_rounded,
                    color: Colors.white,
                    size: 20,
                  ),
                ),
                onPressed: () {
                  setState(() => _isBookmarked = !_isBookmarked);
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text(
                        _isBookmarked ? 'Saved to Watchlist' : 'Removed from Watchlist',
                        style: GoogleFonts.manrope(color: Colors.black, fontWeight: FontWeight.w700),
                      ),
                      backgroundColor: Colors.white,
                      duration: const Duration(seconds: 1),
                    ),
                  );
                },
              ),
              const SizedBox(width: 8),
            ],
            flexibleSpace: FlexibleSpaceBar(
              background: Stack(
                fit: StackFit.expand,
                children: [
                  if (imageUrl.isNotEmpty)
                    CachedNetworkImage(
                      imageUrl: imageUrl,
                      fit: BoxFit.cover,
                      errorWidget: (_, __, ___) => Container(color: MonochromeTheme.cardDark),
                    )
                  else
                    Container(color: MonochromeTheme.cardDark),
                  // Gradient fade into black
                  Container(
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                        colors: [
                          Colors.black.withOpacity(0.3),
                          Colors.transparent,
                          MonochromeTheme.oledBlack.withOpacity(0.85),
                          MonochromeTheme.oledBlack,
                        ],
                        stops: const [0.0, 0.4, 0.8, 1.0],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Content Information
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Title
                  Text(
                    widget.media.title,
                    style: GoogleFonts.manrope(
                      color: Colors.white,
                      fontSize: 28,
                      fontWeight: FontWeight.w900,
                      letterSpacing: -0.6,
                    ),
                  ),

                  const SizedBox(height: 12),

                  // Metadata Badges (Rating, Year, 4K, Provider)
                  Row(
                    children: [
                      if (widget.media.rating > 0) ...[
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.star, color: Colors.black, size: 12),
                              const SizedBox(width: 4),
                              Text(
                                widget.media.rating.toStringAsFixed(1),
                                style: GoogleFonts.manrope(
                                  color: Colors.black,
                                  fontSize: 11,
                                  fontWeight: FontWeight.w800,
                                ),
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(width: 10),
                      ],
                      if (widget.media.year != null) ...[
                        Text(
                          '${widget.media.year}',
                          style: GoogleFonts.manrope(
                            color: Colors.white,
                            fontSize: 13,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(width: 10),
                      ],
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.12),
                          borderRadius: BorderRadius.circular(4),
                          border: Border.all(color: Colors.white.withOpacity(0.2)),
                        ),
                        child: Text(
                          '4K ULTRA HD',
                          style: GoogleFonts.manrope(
                            color: Colors.white,
                            fontSize: 10,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 20),

                  // Action Buttons (Play & Download)
                  Row(
                    children: [
                      Expanded(
                        child: ElevatedButton.icon(
                          onPressed: () {
                            BackendBridge.playMedia(widget.media);
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.white,
                            foregroundColor: Colors.black,
                            padding: const EdgeInsets.symmetric(vertical: 16),
                            shape: RoundedCornerShape(26),
                          ),
                          icon: const Icon(Icons.play_arrow_rounded, size: 22),
                          label: Text(
                            'Play Now',
                            style: GoogleFonts.manrope(
                              fontSize: 15,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Container(
                        decoration: BoxDecoration(
                          color: MonochromeTheme.cardDark,
                          shape: BoxShape.circle,
                          border: Border.all(color: Colors.white.withOpacity(0.18)),
                        ),
                        child: IconButton(
                          icon: const Icon(Icons.arrow_downward_rounded, color: Colors.white),
                          onPressed: () {
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                content: Text(
                                  'Downloading ${widget.media.title} to Offline Vault...',
                                  style: GoogleFonts.manrope(color: Colors.black, fontWeight: FontWeight.w700),
                                ),
                                backgroundColor: Colors.white,
                              ),
                            );
                          },
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 24),

                  // Synopsis
                  Text(
                    'Synopsis',
                    style: GoogleFonts.manrope(
                      color: Colors.white,
                      fontSize: 18,
                      fontWeight: FontWeight.w800,
                      letterSpacing: -0.3,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    widget.media.synopsis ??
                        'A high-stakes narrative featuring compelling characters and breathtaking visual storytelling in complete ultra high definition.',
                    style: GoogleFonts.manrope(
                      color: MonochromeTheme.textSecondary,
                      fontSize: 14,
                      height: 1.5,
                      fontWeight: FontWeight.w500,
                    ),
                  ),

                  const SizedBox(height: 24),

                  // Genres Chips
                  if (widget.media.genres.isNotEmpty) ...[
                    Text(
                      'Genres',
                      style: GoogleFonts.manrope(
                        color: Colors.white,
                        fontSize: 16,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    const SizedBox(height: 10),
                    Wrap(
                      spacing: 8,
                      children: widget.media.genres.map((g) {
                        return Container(
                          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
                          decoration: BoxDecoration(
                            color: MonochromeTheme.cardDark,
                            borderRadius: BorderRadius.circular(16),
                            border: Border.all(color: Colors.white.withOpacity(0.12)),
                          ),
                          child: Text(
                            g,
                            style: GoogleFonts.manrope(
                              color: Colors.white,
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        );
                      }).toList(),
                    ),
                  ],

                  const SizedBox(height: 60),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
