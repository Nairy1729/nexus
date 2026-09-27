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
    // The one accent
    val accent: Color,
    val accentPressed: Color,
    val accentSoft: Color,     // accent at low alpha — selection fills, glows
    val accentContent: Color,  // ink that sits ON the accent (near-black in every theme)
    val accentText: Color,     // accent used as a foreground — tuned per theme for contrast
    // Semantic
    val danger: Color,
    val dangerSoft: Color,
    val scrim: Color,
    // Material bridge
    val isDark: Boolean,
)

object NexusPalette {

    private val AccentLime = Color(0xFFCBFF4D)
    private val AccentLimePressed = Color(0xFFA8DC2E)
    private val AccentInk = Color(0xFF0A0A0C)
    private val Danger = Color(0xFFFF5A4D)

    /** AMOLED-first. Pure black pixels are off. */
    val Dark = NexusColors(
        background = Color(0xFF000000),
        surface = Color(0xFF0A0A0C),
        surfaceRaised = Color(0xFF141418),
        surfacePressed = Color(0xFF1D1D22),
        border = Color(0x14FFFFFF),
        borderStrong = Color(0x2EFFFFFF),
        orbitLine = Color(0x1FFFFFFF),
        textPrimary = Color(0xFFF4F4F6),
        textSecondary = Color(0xFF9A9AA3),
        textTertiary = Color(0xFF65656E),
        accent = AccentLime,
        accentPressed = AccentLimePressed,
        accentSoft = Color(0x24CBFF4D),
        accentContent = AccentInk,
        accentText = AccentLime,
        danger = Danger,
        dangerSoft = Color(0x2EFF5A4D),
        scrim = Color(0xB3000000),
        isDark = true,
    )

    /** OBSIDIAN — the same system on softer graphite, a touch easier in low light. */
    val Obsidian = Dark.copy(
        background = Color(0xFF0C0D10),
        surface = Color(0xFF141519),
        surfaceRaised = Color(0xFF1C1D22),
        surfacePressed = Color(0xFF25262C),
        textPrimary = Color(0xFFECECF0),
        textSecondary = Color(0xFF9B9CA6),
        textTertiary = Color(0xFF6C6D77),
    )

    /** LIGHT — minimal, paper-like, same geometry and rhythm. */
    val Light = NexusColors(
        background = Color(0xFFF6F6F4),
        surface = Color(0xFFFFFFFF),
        surfaceRaised = Color(0xFFFFFFFF),
        surfacePressed = Color(0xFFE9E9E5),
        border = Color(0x14000000),
        borderStrong = Color(0x2E000000),
        orbitLine = Color(0x1F000000),
        textPrimary = Color(0xFF101013),
        textSecondary = Color(0xFF5E5F66),
        textTertiary = Color(0xFF8B8C93),
        accent = AccentLime,
        accentPressed = AccentLimePressed,
        accentSoft = Color(0x339CCC29),
        accentContent = AccentInk,
        accentText = Color(0xFF4E7A00), // same hue, darkened for AA contrast on paper
        danger = Color(0xFFD93025),
        dangerSoft = Color(0x22D93025),
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
