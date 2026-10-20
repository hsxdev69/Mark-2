import 'package:flutter/material.dart';
import 'package:shimmer/shimmer.dart';
import '../theme/monochrome_theme.dart';

class ShimmerBox extends StatelessWidget {
  final double width;
  final double height;
  final double borderRadius;

  const ShimmerBox({
    Key? key,
    required this.width,
    required this.height,
    this.borderRadius = 12.0,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Shimmer.fromColors(
      baseColor: const Color(0xFF141414),
      highlightColor: const Color(0xFF262626),
      period: const Duration(milliseconds: 1400),
      child: Container(
        width: width,
        height: height,
        decoration: BoxDecoration(
          color: MonochromeTheme.cardDark,
          borderRadius: BorderRadius.circular(borderRadius),
        ),
      ),
    );
  }
}
