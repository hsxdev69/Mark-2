package com.movievortex.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object VortexColors {
    val PureBlack = Color(0xFF000000)
    val AppBackground = Color(0xFF0A0A0E)
    val SecondaryBackground = Color(0xFF0D0D11)
    val DarkGraphite = Color(0xFF14141A)
    val CardSurface = Color(0xFF181820)
    val CardSurfaceElevated = Color(0xFF22222A)
    val BorderSubtle = Color(0xFF282834)
    val Divider = Color(0xFF242430)

    val PrimaryText = Color(0xFFFFFFFF)
    val SecondaryText = Color(0xFFB5B5C3)
    val MutedText = Color(0xFF8E8E9A)
    val TextPrimary = PrimaryText
    val TextSecondary = SecondaryText
    val TextMuted = MutedText

    // MovieVortex 2.0 Cyber-Spatial Design Tokens
    val NeonCrimson = Color(0xFFEF2F62)
    val NeonCrimsonPressed = Color(0xFFD4224F)
    val NeonCrimsonGlow = Color(0x59EF2F62)
    val BackgroundOled = Color(0xFF000000)
    val GlassSurfaceDark = Color(0xBF0D0D12)
    val GlassSurfaceLight = Color(0x0FFFFFFF)
    val GlassBorder = Color(0x24FFFFFF)
    val AccentGold = Color(0xFFFFB800)
    val VerifiedGreen = Color(0xFF10B981)

    // Legacy and Brand Accent Aliases
    val PrimaryBrand = NeonCrimson
    val PrimaryBrandGlow = NeonCrimsonGlow
    val GoldenAmber = Color(0xFFF5A524)
    val GoldenAmberDark = Color(0xFFE59819)
    val GoldenAmberGlow = Color(0x66F5A524)
    val AmberLight = Color(0xFFFFB800)

    // Secondary Accents
    val CinematicRed = NeonCrimson
    val CinematicRedGlow = NeonCrimsonGlow
    val SubtlePurple = Color(0xFF8B5CF6)
    val SubtlePurpleGlow = Color(0x4D8B5CF6)
    val CoolBlue = Color(0xFF3B82F6)
    val CoolBlueGlow = Color(0x4D3B82F6)
    val EmeraldGreen = VerifiedGreen
    val WarmAmber = Color(0xFFF5A524)

    // Skeleton
    val SkeletonDark = Color(0xFF181822)
    val SkeletonShimmer = Color(0xFF282836)

    // Gradients
    val CrimsonGradient = Brush.horizontalGradient(
        colors = listOf(
            NeonCrimson,
            NeonCrimsonPressed
        )
    )

    val GoldenGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFB800),
            Color(0xFFF5A524)
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
            NeonCrimsonGlow,
            Color(0x20EF2F62),
            Color.Transparent
        )
    )

    val AccentSelectionGradient = Brush.horizontalGradient(
        colors = listOf(
            NeonCrimson,
            NeonCrimsonPressed
        )
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = VortexColors.NeonCrimson,
    onPrimary = Color.White,
    secondary = VortexColors.NeonCrimsonPressed,
    onSecondary = VortexColors.PrimaryText,
    tertiary = VortexColors.AccentGold,
    background = VortexColors.BackgroundOled,
    onBackground = VortexColors.PrimaryText,
    surface = VortexColors.DarkGraphite,
    onSurface = VortexColors.PrimaryText,
    surfaceVariant = VortexColors.CardSurface,
    onSurfaceVariant = VortexColors.SecondaryText,
    outline = VortexColors.GlassBorder
)

@Composable
fun MovieVortexTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

val TextPrimary = VortexColors.PrimaryText
val TextSecondary = VortexColors.SecondaryText
val TextMuted = VortexColors.MutedText
val PrimaryText = VortexColors.PrimaryText
val SecondaryText = VortexColors.SecondaryText
val MutedText = VortexColors.MutedText
val NeonCrimson = VortexColors.NeonCrimson
val BackgroundOled = VortexColors.BackgroundOled
val GlassSurfaceDark = VortexColors.GlassSurfaceDark
val GlassSurfaceLight = VortexColors.GlassSurfaceLight
val GlassBorder = VortexColors.GlassBorder
val AccentGold = VortexColors.AccentGold
val VerifiedGreen = VortexColors.VerifiedGreen
val PureBlack = VortexColors.PureBlack
