package com.nexus.core.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.nexus.core.animation.LocalReducedMotion

val LocalNexusColors = staticCompositionLocalOf { NexusPalette.ObsidianAurora }
val LocalNexusType = staticCompositionLocalOf { NexusType.Scale }

/**
 * NEXUS Theme Shell.
 *
 * Usage: `NexusTheme.colors.textPrimary`, `NexusTheme.colors.glassSpecularCenter`, `NexusTheme.type.headline`.
 */
object NexusTheme {
    val colors: NexusColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNexusColors.current

    val type: NexusTypeScale
        @Composable
        @ReadOnlyComposable
        get() = LocalNexusType.current
}

private val ThemeTransitionEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

/**
 * Smoothly interpolates all theme tokens across 400ms for continuous, silk-like material transitions.
 * When reduced motion is enabled, transitions apply instantaneously to prevent motion sickness.
 */
@Composable
fun animateNexusColors(target: NexusColors): NexusColors {
    val reducedMotion = LocalReducedMotion.current
    val duration = if (reducedMotion) 0 else 400
    val spec = tween<Color>(durationMillis = duration, easing = ThemeTransitionEasing)

    val background by animateColorAsState(target.background, spec, label = "bg")
    val backgroundGradientStart by animateColorAsState(target.backgroundGradientStart, spec, label = "bgStart")
    val backgroundGradientEnd by animateColorAsState(target.backgroundGradientEnd, spec, label = "bgEnd")
    val surface by animateColorAsState(target.surface, spec, label = "surface")
    val surfaceRaised by animateColorAsState(target.surfaceRaised, spec, label = "surfaceRaised")
    val surfacePressed by animateColorAsState(target.surfacePressed, spec, label = "surfacePressed")

    val glassFillTop by animateColorAsState(target.glassFillTop, spec, label = "glassFillTop")
    val glassFillMid by animateColorAsState(target.glassFillMid, spec, label = "glassFillMid")
    val glassFillBottom by animateColorAsState(target.glassFillBottom, spec, label = "glassFillBottom")
    val glassBorderTop by animateColorAsState(target.glassBorderTop, spec, label = "glassBorderTop")
    val glassBorderBottom by animateColorAsState(target.glassBorderBottom, spec, label = "glassBorderBottom")
    val glassSpecularStart by animateColorAsState(target.glassSpecularStart, spec, label = "glassSpecularStart")
    val glassSpecularCenter by animateColorAsState(target.glassSpecularCenter, spec, label = "glassSpecularCenter")
    val glassSpecularEnd by animateColorAsState(target.glassSpecularEnd, spec, label = "glassSpecularEnd")
    val glassSpecularSecondary by animateColorAsState(target.glassSpecularSecondary, spec, label = "glassSpecularSecondary")
    val glassRefractionTint by animateColorAsState(target.glassRefractionTint, spec, label = "glassRefractionTint")

    val border by animateColorAsState(target.border, spec, label = "border")
    val borderStrong by animateColorAsState(target.borderStrong, spec, label = "borderStrong")
    val orbitLine by animateColorAsState(target.orbitLine, spec, label = "orbitLine")

    val textPrimary by animateColorAsState(target.textPrimary, spec, label = "textPrimary")
    val textSecondary by animateColorAsState(target.textSecondary, spec, label = "textSecondary")
    val textTertiary by animateColorAsState(target.textTertiary, spec, label = "textTertiary")

    val accent by animateColorAsState(target.accent, spec, label = "accent")
    val accentPressed by animateColorAsState(target.accentPressed, spec, label = "accentPressed")
    val accentSoft by animateColorAsState(target.accentSoft, spec, label = "accentSoft")
    val accentContent by animateColorAsState(target.accentContent, spec, label = "accentContent")
    val accentText by animateColorAsState(target.accentText, spec, label = "accentText")
    val accentSecondary by animateColorAsState(target.accentSecondary, spec, label = "accentSecondary")

    val danger by animateColorAsState(target.danger, spec, label = "danger")
    val dangerSoft by animateColorAsState(target.dangerSoft, spec, label = "dangerSoft")
    val scrim by animateColorAsState(target.scrim, spec, label = "scrim")

    val contextViolet by animateColorAsState(target.contextViolet, spec, label = "contextViolet")
    val contextBlue by animateColorAsState(target.contextBlue, spec, label = "contextBlue")
    val contextAmber by animateColorAsState(target.contextAmber, spec, label = "contextAmber")
    val contextGreen by animateColorAsState(target.contextGreen, spec, label = "contextGreen")

    val orbCore by animateColorAsState(target.orbCore, spec, label = "orbCore")
    val orbAccent by animateColorAsState(target.orbAccent, spec, label = "orbAccent")
    val orbParticleHighlight by animateColorAsState(target.orbParticleHighlight, spec, label = "orbParticleHighlight")
    val orbGlow by animateColorAsState(target.orbGlow, spec, label = "orbGlow")

    val glClearColor by animateColorAsState(target.glClearColor, spec, label = "glClearColor")
    val glMoteColor by animateColorAsState(target.glMoteColor, spec, label = "glMoteColor")
    val glCoreColor by animateColorAsState(target.glCoreColor, spec, label = "glCoreColor")
    val glOrbitRingColor by animateColorAsState(target.glOrbitRingColor, spec, label = "glOrbitRingColor")

    return NexusColors(
        background = background,
        backgroundGradientStart = backgroundGradientStart,
        backgroundGradientEnd = backgroundGradientEnd,
        surface = surface,
        surfaceRaised = surfaceRaised,
        surfacePressed = surfacePressed,
        glassFillTop = glassFillTop,
        glassFillMid = glassFillMid,
        glassFillBottom = glassFillBottom,
        glassBorderTop = glassBorderTop,
        glassBorderBottom = glassBorderBottom,
        glassSpecularStart = glassSpecularStart,
        glassSpecularCenter = glassSpecularCenter,
        glassSpecularEnd = glassSpecularEnd,
        glassSpecularSecondary = glassSpecularSecondary,
        glassRefractionTint = glassRefractionTint,
        border = border,
        borderStrong = borderStrong,
        orbitLine = orbitLine,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textTertiary = textTertiary,
        accent = accent,
        accentPressed = accentPressed,
        accentSoft = accentSoft,
        accentContent = accentContent,
        accentText = accentText,
        accentSecondary = accentSecondary,
        danger = danger,
        dangerSoft = dangerSoft,
        scrim = scrim,
        contextViolet = contextViolet,
        contextBlue = contextBlue,
        contextAmber = contextAmber,
        contextGreen = contextGreen,
        orbCore = orbCore,
        orbAccent = orbAccent,
        orbParticleHighlight = orbParticleHighlight,
        orbGlow = orbGlow,
        glClearColor = glClearColor,
        glMoteColor = glMoteColor,
        glCoreColor = glCoreColor,
        glOrbitRingColor = glOrbitRingColor,
        isDark = target.isDark,
    )
}

@Composable
fun NexusTheme(
    mode: NexusThemeMode = NexusThemeMode.ObsidianAurora,
    content: @Composable () -> Unit,
) {
    val targetColors = when (mode) {
        NexusThemeMode.ObsidianAurora -> NexusPalette.ObsidianAurora
        NexusThemeMode.GraphiteLime -> NexusPalette.GraphiteLime
        NexusThemeMode.MidnightBurgundy -> NexusPalette.MidnightBurgundy
    }

    val animatedColors = animateNexusColors(targetColors)

    CompositionLocalProvider(
        LocalNexusColors provides animatedColors,
        LocalNexusType provides NexusType.Scale,
    ) {
        MaterialTheme(
            colorScheme = animatedColors.toColorScheme(),
            typography = NexusType.Material,
            content = content,
        )
    }
}
