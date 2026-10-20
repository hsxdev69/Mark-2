package com.movievortex.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lagradost.cloudstream3.R

// ─────────────────────────────────────────────────────────────────────────────
// ManropeFontFamily — Only regular & bold weights available in resources
// ─────────────────────────────────────────────────────────────────────────────
val ManropeFontFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

// ─────────────────────────────────────────────────────────────────────────────
// VortexColors — Dark Cinematic + Suspended Design System
// ─────────────────────────────────────────────────────────────────────────────
object VortexColors {

    // ── Core Surfaces ────────────────────────────────────────────────────────
    val PureBlack = Color(0xFF000000)
    val AppBackground = Color(0xFF000000)
    val SecondaryBackground = Color(0xFF0A0A0A)
    val DarkGraphite = Color(0xFF101010)
    val CardSurface = Color(0xFF141414)
    val CardSurfaceElevated = Color(0xFF1C1C1C)
    val BorderSubtle = Color(0xFF242424)
    val Divider = Color(0xFF1F1F1F)

    // ── Text Hierarchy ───────────────────────────────────────────────────────
    val PrimaryText = Color(0xFFFFFFFF)
    val SecondaryText = Color(0xFFCCCCCC)
    val MutedText = Color(0xFF888888)
    val TextPrimary = PrimaryText
    val TextSecondary = SecondaryText
    val TextMuted = MutedText

    // ── Pure Monochrome Black & White Design Tokens ──────────────────────────
    val NeonCrimson = Color(0xFFFFFFFF)
    val NeonCrimsonPressed = Color(0xFFCCCCCC)
    val NeonCrimsonGlow = Color(0x33FFFFFF)
    val BackgroundOled = Color(0xFF000000)
    val GlassSurfaceDark = Color(0xDD0C0C0C)
    val GlassSurfaceLight = Color(0x1AFFFFFF)
    val GlassBorder = Color(0x22FFFFFF)
    val AccentGold = Color(0xFFFFFFFF)
    val VerifiedGreen = Color(0xFFFFFFFF)

    // ── Brand Accent Aliases ─────────────────────────────────────────────────
    val PrimaryBrand = Color(0xFFFFFFFF)
    val PrimaryBrandGlow = Color(0x33FFFFFF)
    val GoldenAmber = Color(0xFFFFFFFF)
    val GoldenAmberDark = Color(0xFFCCCCCC)
    val GoldenAmberGlow = Color(0x33FFFFFF)
    val AmberLight = Color(0xFFFFFFFF)

    // ── Secondary Accents ────────────────────────────────────────────────────
    val CinematicRed = Color(0xFFFFFFFF)
    val CinematicRedGlow = Color(0x33FFFFFF)
    val SubtlePurple = Color(0xFFFFFFFF)
    val SubtlePurpleGlow = Color(0x33FFFFFF)
    val CoolBlue = Color(0xFFFFFFFF)
    val CoolBlueGlow = Color(0x33FFFFFF)
    val EmeraldGreen = Color(0xFFFFFFFF)
    val WarmAmber = Color(0xFFFFFFFF)

    // ── Skeleton / Shimmer ───────────────────────────────────────────────────
    val SkeletonDark = Color(0xFF141414)
    val SkeletonShimmer = Color(0xFF222222)

    // ── NEW: Cinematic Accent Colors ─────────────────────────────────────────
    /** Soft silver for secondary highlights */
    val CinematicAccent = Color(0xFFE0E0E0)

    /** 25% white for card glow shadows */
    val GlowWhite = Color(0x40FFFFFF)

    /** 80% dark for glassmorphic surfaces */
    val FrostedGlass = Color(0xCC0A0A0A)

    /** 20% white border for frosted glass panels */
    val FrostedGlassBorder = Color(0x33FFFFFF)

    /** 60% black for hero text overlay */
    val HeroOverlay = Color(0x99000000)

    /** White chip active background */
    val ChipActive = Color(0xFFFFFFFF)

    /** Dark chip inactive background */
    val ChipInactive = Color(0xFF1A1A1A)

    /** Chip border — 20% white */
    val ChipBorder = Color(0x33FFFFFF)

    /** Progress bar track — 20% white */
    val ProgressTrack = Color(0x33FFFFFF)

    /** Progress bar fill — pure white */
    val ProgressFill = Color(0xFFFFFFFF)

    /** Golden star rating color — the ONLY non-white accent in the system */
    val RatingGold = Color(0xFFFFD700)

    // ── Gradients ────────────────────────────────────────────────────────────

    val CrimsonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFE0E0E0)
        )
    )

    val GoldenGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFE0E0E0)
        )
    )

    val HeroGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color(0x66000000),
            Color(0xCC000000),
            BackgroundOled
        )
    )

    val TopBarGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xEE000000),
            Color(0x99000000),
            Color.Transparent
        )
    )

    val SplashAuraGradient = Brush.radialGradient(
        colors = listOf(
            Color(0x33FFFFFF),
            Color(0x10FFFFFF),
            Color.Transparent
        )
    )

    // ── NEW: Cinematic Gradients ─────────────────────────────────────────────

    /** 5-stop smoother cinematic fade for hero sections */
    val CinematicHeroGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to Color.Transparent,
            0.40f to Color(0x66000000),
            0.70f to Color(0xCC000000),
            0.90f to Color(0xF2000000),
            1.00f to PureBlack
        )
    )

    /** Backdrop gradient for detail screens — starts fading earlier at 0.3 */
    val DetailBackdropGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to Color.Transparent,
            0.30f to Color(0x66000000),
            0.60f to Color(0xCC000000),
            0.85f to Color(0xF2000000),
            1.00f to PureBlack
        )
    )

    /** Radial glow effect for card hover / focus states */
    val CardHoverGlow = Brush.radialGradient(
        colors = listOf(
            GlowWhite,
            Color.Transparent
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// DarkColorScheme — Material3 dark palette wired to VortexColors
// ─────────────────────────────────────────────────────────────────────────────
val DarkColorScheme = darkColorScheme(
    primary = VortexColors.NeonCrimson,
    onPrimary = VortexColors.PureBlack,
    primaryContainer = VortexColors.CardSurfaceElevated,
    onPrimaryContainer = VortexColors.PrimaryText,
    secondary = VortexColors.AccentGold,
    onSecondary = VortexColors.PureBlack,
    secondaryContainer = VortexColors.CardSurface,
    onSecondaryContainer = VortexColors.PrimaryText,
    tertiary = VortexColors.VerifiedGreen,
    onTertiary = VortexColors.PureBlack,
    background = VortexColors.BackgroundOled,
    onBackground = VortexColors.PrimaryText,
    surface = VortexColors.DarkGraphite,
    onSurface = VortexColors.PrimaryText,
    surfaceVariant = VortexColors.CardSurface,
    onSurfaceVariant = VortexColors.SecondaryText,
    outline = VortexColors.GlassBorder
)

// ─────────────────────────────────────────────────────────────────────────────
// ModernTypography — Manrope type scale
// ─────────────────────────────────────────────────────────────────────────────
val ModernTypography = Typography(
    displayLarge = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

// ─────────────────────────────────────────────────────────────────────────────
// MovieVortexTheme — App-level Compose theme
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MovieVortexTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ModernTypography,
        content = content
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Top-level convenience aliases
// ─────────────────────────────────────────────────────────────────────────────
val TextPrimary = VortexColors.PrimaryText
val TextSecondary = VortexColors.SecondaryText
val TextMuted = VortexColors.MutedText
val PrimaryText = VortexColors.PrimaryText
val SecondaryText = VortexColors.SecondaryText
val TextMutedAlias = VortexColors.MutedText
val NeonCrimson = VortexColors.NeonCrimson
val BackgroundOled = VortexColors.BackgroundOled
val GlassSurfaceDark = VortexColors.GlassSurfaceDark
val GlassSurfaceLight = VortexColors.GlassSurfaceLight
val GlassBorder = VortexColors.GlassBorder
val AccentGold = VortexColors.AccentGold
val VerifiedGreen = VortexColors.VerifiedGreen
val PureBlack = VortexColors.PureBlack
