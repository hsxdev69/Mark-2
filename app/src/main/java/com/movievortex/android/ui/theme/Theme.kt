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

val ManropeFontFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

object VortexColors {
    val PureBlack = Color(0xFF000000)
    val AppBackground = Color(0xFF000000)
    val SecondaryBackground = Color(0xFF0A0A0A)
    val DarkGraphite = Color(0xFF101010)
    val CardSurface = Color(0xFF141414)
    val CardSurfaceElevated = Color(0xFF1C1C1C)
    val BorderSubtle = Color(0xFF242424)
    val Divider = Color(0xFF1F1F1F)

    val PrimaryText = Color(0xFFFFFFFF)
    val SecondaryText = Color(0xFFCCCCCC)
    val MutedText = Color(0xFF888888)
    val TextPrimary = PrimaryText
    val TextSecondary = SecondaryText
    val TextMuted = MutedText

    // Pure Monochrome Black & White Design Tokens
    val NeonCrimson = Color(0xFFFFFFFF)
    val NeonCrimsonPressed = Color(0xFFCCCCCC)
    val NeonCrimsonGlow = Color(0x33FFFFFF)
    val BackgroundOled = Color(0xFF000000)
    val GlassSurfaceDark = Color(0xDD0C0C0C)
    val GlassSurfaceLight = Color(0x1AFFFFFF)
    val GlassBorder = Color(0x22FFFFFF)
    val AccentGold = Color(0xFFFFFFFF)
    val VerifiedGreen = Color(0xFFFFFFFF)

    // Brand Accent Aliases
    val PrimaryBrand = Color(0xFFFFFFFF)
    val PrimaryBrandGlow = Color(0x33FFFFFF)
    val GoldenAmber = Color(0xFFFFFFFF)
    val GoldenAmberDark = Color(0xFFCCCCCC)
    val GoldenAmberGlow = Color(0x33FFFFFF)
    val AmberLight = Color(0xFFFFFFFF)

    // Secondary Accents
    val CinematicRed = Color(0xFFFFFFFF)
    val CinematicRedGlow = Color(0x33FFFFFF)
    val SubtlePurple = Color(0xFFFFFFFF)
    val SubtlePurpleGlow = Color(0x33FFFFFF)
    val CoolBlue = Color(0xFFFFFFFF)
    val CoolBlueGlow = Color(0x33FFFFFF)
    val EmeraldGreen = Color(0xFFFFFFFF)
    val WarmAmber = Color(0xFFFFFFFF)

    // Skeleton
    val SkeletonDark = Color(0xFF141414)
    val SkeletonShimmer = Color(0xFF222222)

    // Gradients
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
}

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
