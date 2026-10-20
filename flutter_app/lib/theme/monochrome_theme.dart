import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class MonochromeTheme {
  // Pure Black & White Color Palette
  static const Color oledBlack = Color(0xFF000000);
  static const Color pureWhite = Color(0xFFFFFFFF);
  static const Color surfaceDark = Color(0xFF0C0C0C);
  static const Color cardDark = Color(0xFF141414);
  static const Color cardElevated = Color(0xFF1C1C1C);
  static const Color borderSubtle = Color(0xFF262626);
  static const Color borderBright = Color(0xFF404040);
  static const Color textMuted = Color(0xFF888888);
  static const Color textSecondary = Color(0xFFB3B3B3);

  // Smooth Glassmorphic Borders & Highlights
  static final Border glassBorder = Border.all(
    color: pureWhite.withOpacity(0.14),
    width: 1.0,
  );

  static final Border glowBorder = Border.all(
    color: pureWhite.withOpacity(0.40),
    width: 1.2,
  );

  static ThemeData get themeData {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: oledBlack,
      primaryColor: pureWhite,
      colorScheme: const ColorScheme.dark(
        primary: pureWhite,
        onPrimary: oledBlack,
        secondary: pureWhite,
        onSecondary: oledBlack,
        surface: surfaceDark,
        onSurface: pureWhite,
        background: oledBlack,
        onBackground: pureWhite,
      ),
      textTheme: GoogleFonts.manropeTextTheme(
        ThemeData.dark().textTheme,
      ).apply(
        bodyColor: pureWhite,
        displayColor: pureWhite,
      ),
      appBarTheme: AppBarTheme(
        backgroundColor: oledBlack,
        elevation: 0,
        centerTitle: false,
        titleTextStyle: GoogleFonts.manrope(
          color: pureWhite,
          fontSize: 20,
          fontWeight: FontWeight.w800,
          letterSpacing: -0.4,
        ),
      ),
      splashColor: pureWhite.withOpacity(0.08),
      highlightColor: pureWhite.withOpacity(0.04),
    );
  }
}
