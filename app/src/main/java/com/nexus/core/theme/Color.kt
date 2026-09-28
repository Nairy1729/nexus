package com.nexus.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * NEXUS color system.
 *
 * One accent. Everything else is a carefully tiered neutral. Semantic red exists only for
 * missed calls and decline — it communicates state, it is not decoration.
 *
 * All three themes share this exact token set; only the values differ, so NEXUS DARK,
 * NEXUS OBSIDIAN and NEXUS LIGHT are skins of one design system, never three designs.
 */
@Immutable
data class NexusColors(
    // Surfaces
    val background: Color,
    val backgroundGradientStart: Color = background,
    val backgroundGradientEnd: Color = background,
    val surface: Color,
    val surfaceRaised: Color,
    val surfacePressed: Color,
    // Lines
    val border: Color,
    val borderStrong: Color,
    val orbitLine: Color,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    // The restrained accent: cool electric blue / icy violet
    val accent: Color,
    val accentPressed: Color,
    val accentSoft: Color,     // accent at low alpha — selection fills, ambient glows
    val accentContent: Color,  // ink that sits ON the accent
    val accentText: Color,     // accent used as foreground text
    // Semantic
    val danger: Color,
    val dangerSoft: Color,
    val scrim: Color,
    // Contextual light accents (passing through glass)
    val contextViolet: Color = Color(0xFFC0A6FF),
    val contextBlue: Color = Color(0xFF78B7FF),
    val contextAmber: Color = Color(0xFFFFB668),
    val contextGreen: Color = Color(0xFF72D2B0),
    // Material bridge
    val isDark: Boolean,
)

object NexusPalette {

    // Restrained cool electric blue / icy violet accent
    private val AccentIcyBlue = Color(0xFF829FFF)
    private val AccentIcyBluePressed = Color(0xFF6B8BE8)
    private val AccentInkDark = Color(0xFF08090E)
    private val Danger = Color(0xFFE25850)

    /**
     * SPATIAL GLASS OS — Dark (Near-black / graphite with subtle tonal depth).
     * Replaces harsh pitch-black with deep atmospheric graphite.
     */
    val Dark = NexusColors(
        background = Color(0xFF08090D),
        backgroundGradientStart = Color(0xFF0D0F16),
        backgroundGradientEnd = Color(0xFF050608),
        surface = Color(0xFF101218),
        surfaceRaised = Color(0xFF161822),
        surfacePressed = Color(0xFF1F222E),
        border = Color(0x1AFFFFFF),
        borderStrong = Color(0x33FFFFFF),
        orbitLine = Color(0x1FFFFFFF),
        textPrimary = Color(0xFFF0F1F6),   // Soft white
        textSecondary = Color(0xFF8E91A0), // Muted cool gray
        textTertiary = Color(0xFF565967),  // Low-contrast gray
        accent = AccentIcyBlue,
        accentPressed = AccentIcyBluePressed,
        accentSoft = Color(0x24829FFF),
        accentContent = AccentInkDark,
        accentText = Color(0xFFA2B7FF),
        danger = Danger,
        dangerSoft = Color(0x28E25850),
        scrim = Color(0xB8050608),
        isDark = true,
    )

    /** OBSIDIAN — refined graphite with softer atmospheric contrast. */
    val Obsidian = Dark.copy(
        background = Color(0xFF0C0E14),
        backgroundGradientStart = Color(0xFF11141D),
        backgroundGradientEnd = Color(0xFF07080C),
        surface = Color(0xFF151822),
        surfaceRaised = Color(0xFF1C202B),
        surfacePressed = Color(0xFF262A38),
        textPrimary = Color(0xFFECECF0),
        textSecondary = Color(0xFF8C8F9E),
        textTertiary = Color(0xFF5A5C69),
        accent = Color(0xFF8BA5FF),
    )

    /** LIGHT — paper & frosted translucent glass. */
    val Light = NexusColors(
        background = Color(0xFFF4F5F8),
        backgroundGradientStart = Color(0xFFFAFAFC),
        backgroundGradientEnd = Color(0xFFECEEF2),
        surface = Color(0xFFFFFFFF),
        surfaceRaised = Color(0xFFF7F8FA),
        surfacePressed = Color(0xFFE6E8EE),
        border = Color(0x18000000),
        borderStrong = Color(0x2D000000),
        orbitLine = Color(0x1C000000),
        textPrimary = Color(0xFF12141A),
        textSecondary = Color(0xFF585A68),
        textTertiary = Color(0xFF888B98),
        accent = Color(0xFF476FF5),
        accentPressed = Color(0xFF3458D4),
        accentSoft = Color(0x26476FF5),
        accentContent = Color.White,
        accentText = Color(0xFF385EDB),
        danger = Color(0xFFD43830),
        dangerSoft = Color(0x22D43830),
        scrim = Color(0x99000000),
        isDark = false,
    )
}

enum class NexusThemeMode { Dark, Obsidian, Light }

/**
 * Bridge to Material 3 so stock primitives (text fields, ripples) inherit the system
 * instead of fighting it. Feature UI is built from NEXUS components, not Material widgets.
 */
fun NexusColors.toColorScheme(): ColorScheme = if (isDark) {
    darkColorScheme(
        primary = accent,
        onPrimary = accentContent,
        primaryContainer = accentSoft,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceRaised,
        onSurfaceVariant = textSecondary,
        outline = borderStrong,
        error = danger,
        onError = Color.Black,
        scrim = scrim,
    )
} else {
    lightColorScheme(
        primary = accentText,
        onPrimary = Color.White,
        primaryContainer = accentSoft,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceRaised,
        onSurfaceVariant = textSecondary,
        outline = borderStrong,
        error = danger,
        onError = Color.White,
        scrim = scrim,
    )
}
