package com.nexus.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * NEXUS SPATIAL GLASS OS Color System.
 *
 * Implements three distinct visual identities:
 * 1. OBSIDIAN AURORA (Default) — Luxury, Spatial, Calm, Aurora Violet & Subtle Champagne
 * 2. GRAPHITE LIME — Technical, Precision, Kinetic, Restrained Electric Lime & Cold Slate
 * 3. MIDNIGHT BURGUNDY — Intimate, Royal, Cinematic, Translucent Wine Glass & Warm Champagne
 *
 * All three themes share this exact token set; zero hardcoded colors exist in UI components.
 */
@Immutable
data class NexusColors(
    // Surface Foundations
    val background: Color,
    val backgroundGradientStart: Color = background,
    val backgroundGradientEnd: Color = background,
    val surface: Color,
    val surfaceRaised: Color,
    val surfacePressed: Color,

    // Liquid Glass Material Tokens
    val glassFillTop: Color,
    val glassFillMid: Color,
    val glassFillBottom: Color,
    val glassBorderTop: Color,
    val glassBorderBottom: Color,
    val glassSpecularStart: Color,
    val glassSpecularCenter: Color,
    val glassSpecularEnd: Color,
    val glassSpecularSecondary: Color,
    val glassRefractionTint: Color,

    // Lines & Separators
    val border: Color,
    val borderStrong: Color,
    val orbitLine: Color,

    // Text Hierarchy
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,

    // Primary Identity Accent
    val accent: Color,
    val accentPressed: Color,
    val accentSoft: Color,     // accent at low alpha — selection fills, ambient glows
    val accentContent: Color,  // ink that sits ON the accent
    val accentText: Color,     // accent used as foreground text

    // Secondary / Ambient Accent (Champagne / Rose / Slate highlights)
    val accentSecondary: Color,

    // Semantics (State-driven only, never decoration)
    val danger: Color,
    val dangerSoft: Color,
    val scrim: Color,

    // Contextual Light Accents (Passing through translucent glass)
    val contextViolet: Color = Color(0xFFC0A6FF),
    val contextBlue: Color = Color(0xFF78B7FF),
    val contextAmber: Color = Color(0xFFFFB668),
    val contextGreen: Color = Color(0xFF72D2B0),

    // Thinking Orb Specific Tokens
    val orbCore: Color,
    val orbAccent: Color,
    val orbParticleHighlight: Color,
    val orbGlow: Color,

    // 3D Spatial Environment Tokens (OpenGL ES Universe)
    val glClearColor: Color,
    val glMoteColor: Color,
    val glCoreColor: Color,
    val glOrbitRingColor: Color,

    // Material Bridge
    val isDark: Boolean = true,
)

object NexusPalette {

    // Common semantic alert red
    private val Danger = Color(0xFFE25850)
    private val DangerSoft = Color(0x28E25850)

    /**
     * THEME 01 — OBSIDIAN AURORA (Default)
     * Personality: Luxury, Futuristic, Calm, Premium, Spatial.
     * Deep obsidian, pearl glass, silver, aurora violet accent light, subtle lavender & champagne.
     */
    val ObsidianAurora = NexusColors(
        background = Color(0xFF07080C),
        backgroundGradientStart = Color(0xFF0C0E15),
        backgroundGradientEnd = Color(0xFF040508),
        surface = Color(0xFF0F121A),
        surfaceRaised = Color(0xFF161B26),
        surfacePressed = Color(0xFF1F2534),

        glassFillTop = Color(0x18FFFFFF),
        glassFillMid = Color(0x0CFFFFFF),
        glassFillBottom = Color(0x05FFFFFF),
        glassBorderTop = Color(0x44C8CCD8),      // Pearl silver edge
        glassBorderBottom = Color(0x16A78BFA),   // Subtle aurora violet falloff
        glassSpecularStart = Color(0x00C8CCD8),
        glassSpecularCenter = Color(0x60E8EDF8), // Pure pearl specular gleam
        glassSpecularEnd = Color(0x00F5E6C8),
        glassSpecularSecondary = Color(0x40C4B5FD), // Subtle lavender sheen
        glassRefractionTint = Color(0x1AA78BFA),    // Aurora violet ambient refraction

        border = Color(0x1FC8CCD8),
        borderStrong = Color(0x38C8CCD8),
        orbitLine = Color(0x24A78BFA),

        textPrimary = Color(0xFFF0F3F9),   // Pearl white
        textSecondary = Color(0xFF9096A8), // Muted cool silver
        textTertiary = Color(0xFF5A6072),  // Low-contrast slate

        accent = Color(0xFFA78BFA),        // Aurora violet accent light
        accentPressed = Color(0xFF8B6BF5),
        accentSoft = Color(0x28A78BFA),
        accentContent = Color(0xFF07080C),
        accentText = Color(0xFFBCA7FF),
        accentSecondary = Color(0xFFF5E6C8), // Restrained champagne

        danger = Danger,
        dangerSoft = DangerSoft,
        scrim = Color(0xBF07080C),

        orbCore = Color(0xFF10131E),
        orbAccent = Color(0xFFA78BFA),
        orbParticleHighlight = Color(0xFFF6F3FF),
        orbGlow = Color(0x3AA78BFA),

        glClearColor = Color(0xFF07080C),
        glMoteColor = Color(0xFFD4D8F0),
        glCoreColor = Color(0xFFECEFF8),
        glOrbitRingColor = Color(0x3AA78BFA),

        isDark = true,
    )

    /**
     * THEME 02 — GRAPHITE LIME
     * Personality: Precision, Technical, Kinetic, High Contrast, Modernist.
     * Deep graphite, smoked glass, pearl white, electric lime accent (restrained, non-gaming), muted silver.
     */
    val GraphiteLime = NexusColors(
        background = Color(0xFF090B0E),
        backgroundGradientStart = Color(0xFF0F1218),
        backgroundGradientEnd = Color(0xFF050709),
        surface = Color(0xFF13161F),
        surfaceRaised = Color(0xFF1A1F2B),
        surfacePressed = Color(0xFF242A3B),

        glassFillTop = Color(0x16FFFFFF),
        glassFillMid = Color(0x0AFFFFFF),
        glassFillBottom = Color(0x04FFFFFF),
        glassBorderTop = Color(0x40A2ABB8),      // Technical cold slate silver
        glassBorderBottom = Color(0x18B8FF34),   // Subtle electric lime rim bleed
        glassSpecularStart = Color(0x00FFFFFF),
        glassSpecularCenter = Color(0x65FFFFFF), // Ultra-crisp specular line
        glassSpecularEnd = Color(0x00B8FF34),
        glassSpecularSecondary = Color(0x38B8FF34), // Fine electric lime gleam
        glassRefractionTint = Color(0x16B8FF34),

        border = Color(0x1D8A95A8),
        borderStrong = Color(0x389CA9BE),
        orbitLine = Color(0x28B8FF34),

        textPrimary = Color(0xFFF4F6FA),   // Pearl white
        textSecondary = Color(0xFF8690A2), // Technical slate
        textTertiary = Color(0xFF545C6C),  // Dark slate

        accent = Color(0xFFB8FF34),        // Electric lime (restrained, precision, non-gamer)
        accentPressed = Color(0xFF9FE522),
        accentSoft = Color(0x24B8FF34),
        accentContent = Color(0xFF090B0E),
        accentText = Color(0xFFC4FF52),
        accentSecondary = Color(0xFFE2E8F0), // Technical silver

        danger = Color(0xFFE5534B),
        dangerSoft = Color(0x26E5534B),
        scrim = Color(0xC0090B0E),

        orbCore = Color(0xFF12151D),
        orbAccent = Color(0xFFB8FF34),
        orbParticleHighlight = Color(0xFFFFFFFF),
        orbGlow = Color(0x38B8FF34),

        glClearColor = Color(0xFF090B0E),
        glMoteColor = Color(0xFFE2E8F0),
        glCoreColor = Color(0xFFF4F8F0),
        glOrbitRingColor = Color(0x32B8FF34),

        isDark = true,
    )

    /**
     * THEME 03 — MIDNIGHT BURGUNDY
     * Personality: Dramatic, Intimate, Nocturnal, Royal, Cinematic.
     * Midnight noir, deep wine/burgundy translucent glass, pearl, champagne, muted silver.
     */
    val MidnightBurgundy = NexusColors(
        background = Color(0xFF070508),
        backgroundGradientStart = Color(0xFF0E0710),
        backgroundGradientEnd = Color(0xFF040305),
        surface = Color(0xFF150A13),
        surfaceRaised = Color(0xFF1E0E1B),
        surfacePressed = Color(0xFF2C1427),

        glassFillTop = Color(0x1AFFFFFF),
        glassFillMid = Color(0x122D1322),        // Deep wine translucent warmth
        glassFillBottom = Color(0x08180913),
        glassBorderTop = Color(0x42E6CCA0),      // Warm champagne gold highlight
        glassBorderBottom = Color(0x208C1D40),   // Deep crimson/burgundy glow
        glassSpecularStart = Color(0x00E6CCA0),
        glassSpecularCenter = Color(0x5AFFF1DC), // Champagne pearl gleam
        glassSpecularEnd = Color(0x008C1D40),
        glassSpecularSecondary = Color(0x3CE6CCA0), // Champagne reflection
        glassRefractionTint = Color(0x228C1D40),    // Rich wine ambient refraction

        border = Color(0x24E6CCA0),
        borderStrong = Color(0x3DE6CCA0),
        orbitLine = Color(0x2A8C1D40),

        textPrimary = Color(0xFFF7EFF3),   // Pearl with subtle warm blush
        textSecondary = Color(0xFFA8919B), // Mauve silver
        textTertiary = Color(0xFF705A65),

        accent = Color(0xFFE6CCA0),        // Warm champagne gold (royal & restrained)
        accentPressed = Color(0xFFD6B988),
        accentSoft = Color(0x24E6CCA0),
        accentContent = Color(0xFF070508),
        accentText = Color(0xFFF2DAB5),
        accentSecondary = Color(0xFF8C1D40), // Deep wine crimson

        danger = Color(0xFFE25350),
        dangerSoft = Color(0x28E25350),
        scrim = Color(0xC2070508),

        orbCore = Color(0xFF180B15),
        orbAccent = Color(0xFFE6CCA0),
        orbParticleHighlight = Color(0xFFFFF6E8),
        orbGlow = Color(0x3A8C1D40),

        glClearColor = Color(0xFF070508),
        glMoteColor = Color(0xFFF2E0CD),
        glCoreColor = Color(0xFFFAF0E4),
        glOrbitRingColor = Color(0x32E6CCA0),

        isDark = true,
    )

    // Legacy aliases for backward compatibility
    val Dark = ObsidianAurora
    val Obsidian = ObsidianAurora
    val Light = GraphiteLime
}

/**
 * Three complete visual operating system identities.
 */
enum class NexusThemeMode(
    val id: String,
    val title: String,
    val subtitle: String,
) {
    ObsidianAurora(
        id = "obsidian_aurora",
        title = "Obsidian Aurora",
        subtitle = "Luxury · Spatial · Calm",
    ),
    GraphiteLime(
        id = "graphite_lime",
        title = "Graphite Lime",
        subtitle = "Technical · Precision · Kinetic",
    ),
    MidnightBurgundy(
        id = "midnight_burgundy",
        title = "Midnight Burgundy",
        subtitle = "Intimate · Royal · Cinematic",
    );

    companion object {
        val DEFAULT = ObsidianAurora

        // Legacy compatibility mappings
        val Dark = ObsidianAurora
        val Obsidian = ObsidianAurora
        val Light = GraphiteLime

        fun fromId(id: String?): NexusThemeMode =
            values().firstOrNull { it.id == id } ?: DEFAULT
    }
}

/**
 * Bridge to Material 3 so stock primitives inherit the system.
 */
fun NexusColors.toColorScheme(): ColorScheme = darkColorScheme(
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
