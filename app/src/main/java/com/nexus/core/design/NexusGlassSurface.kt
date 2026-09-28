package com.nexus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.core.theme.NexusRadii
import com.nexus.core.theme.NexusTheme

/**
 * The NEXUS SPATIAL GLASS OS material tiers.
 *
 * Glass behaves like an advanced physical material with depth, edge highlights,
 * refractive light catches, and soft ambient falloff.
 */
enum class GlassTier {
    /** Major interactive surfaces (Universe Context, Timeline containers, Profiles). */
    Primary,
    /** Secondary controls (buttons, dial keys, selection chips). */
    Secondary,
    /** Contextual floating HUDs and elevated cards. */
    Floating,
    /** Minimal chrome (floating dock, subtle status bars, pill containers). */
    Minimal,
}

object NexusGlass {

    /**
     * Multilayered glass fill brush for the specified [tier].
     * Supports optional [tint] for contextual light passing through glass.
     */
    @Composable
    fun fill(tier: GlassTier = GlassTier.Primary, tint: Color? = null): Brush {
        val isDark = NexusTheme.colors.isDark
        if (!isDark) {
            val baseAlphaTop = when (tier) {
                GlassTier.Primary -> 0.92f
                GlassTier.Secondary -> 0.88f
                GlassTier.Floating -> 0.96f
                GlassTier.Minimal -> 0.82f
            }
            val baseAlphaBottom = when (tier) {
                GlassTier.Primary -> 0.72f
                GlassTier.Secondary -> 0.65f
                GlassTier.Floating -> 0.80f
                GlassTier.Minimal -> 0.58f
            }
            return Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = baseAlphaTop),
                    Color.White.copy(alpha = baseAlphaBottom),
                ),
            )
        }

        // Dark / Obsidian mode: layered graphite with subtle white sheen
        val (alphaTop, alphaMid, alphaBottom) = when (tier) {
            GlassTier.Primary -> Triple(0.080f, 0.045f, 0.025f)
            GlassTier.Secondary -> Triple(0.095f, 0.055f, 0.035f)
            GlassTier.Floating -> Triple(0.120f, 0.075f, 0.045f)
            GlassTier.Minimal -> Triple(0.055f, 0.030f, 0.018f)
        }

        val baseColors = if (tint != null) {
            listOf(
                Color.White.copy(alpha = alphaTop * 0.7f),
                tint.copy(alpha = 0.035f),
                Color.White.copy(alpha = alphaBottom * 0.7f),
            )
        } else {
            listOf(
                Color.White.copy(alpha = alphaTop),
                Color.White.copy(alpha = alphaMid),
                Color.White.copy(alpha = alphaBottom),
            )
        }

        return Brush.verticalGradient(colors = baseColors)
    }

    /**
     * Precision edge-lit border brush — brightest where ambient light hits the top edge,
     * fading into the surrounding spatial atmosphere toward the bottom.
     */
    @Composable
    fun borderBrush(tier: GlassTier = GlassTier.Primary, tint: Color? = null): Brush {
        val isDark = NexusTheme.colors.isDark
        if (!isDark) {
            val base = NexusTheme.colors.borderStrong
            return Brush.verticalGradient(
                colors = listOf(base, base.copy(alpha = base.alpha * 0.40f)),
            )
        }

        val topAlpha = when (tier) {
            GlassTier.Primary -> 0.28f
            GlassTier.Secondary -> 0.32f
            GlassTier.Floating -> 0.38f
            GlassTier.Minimal -> 0.18f
        }
        val bottomAlpha = topAlpha * 0.28f

        return if (tint != null) {
            Brush.verticalGradient(
                colors = listOf(
                    tint.copy(alpha = topAlpha * 1.1f),
                    Color.White.copy(alpha = (topAlpha + bottomAlpha) / 2f),
                    tint.copy(alpha = bottomAlpha),
                ),
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = topAlpha),
                    Color.White.copy(alpha = bottomAlpha),
                ),
            )
        }
    }
}

/**
 * Standard NEXUS Spatial Glass Surface.
 *
 * Implements physical edge highlights, top specular light catch, hairline borders,
 * and contextual light tints without expensive runtime blurs.
 */
@Composable
fun NexusGlassSurface(
    modifier: Modifier = Modifier,
    tier: GlassTier = GlassTier.Primary,
    shape: Shape = RoundedCornerShape(NexusRadii.lg),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    highlightEdge: Boolean = true,
    tint: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = NexusTheme.colors.isDark
    val fillBrush = NexusGlass.fill(tier, tint)
    val borderBrush = NexusGlass.borderBrush(tier, tint)

    Box(
        modifier = modifier
            .clip(shape)
            .background(fillBrush)
            .then(
                if (highlightEdge) {
                    Modifier.drawWithCache {
                        val strokeWidth = 1.25.dp.toPx()
                        val topHighlightAlpha = when (tier) {
                            GlassTier.Primary -> if (isDark) 0.24f else 0.70f
                            GlassTier.Secondary -> if (isDark) 0.30f else 0.80f
                            GlassTier.Floating -> if (isDark) 0.36f else 0.85f
                            GlassTier.Minimal -> if (isDark) 0.16f else 0.50f
                        }
                        val highlightColor = tint?.copy(alpha = topHighlightAlpha * 0.6f)
                            ?: Color.White.copy(alpha = topHighlightAlpha)

                        onDrawBehind {
                            // Top edge specular gleam: physical light hitting top of the glass pane
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        highlightColor,
                                        Color.Transparent,
                                    ),
                                ),
                                start = Offset(size.width * 0.08f, strokeWidth / 2f),
                                end = Offset(size.width * 0.92f, strokeWidth / 2f),
                                strokeWidth = strokeWidth,
                            )

                            // For floating tier, add a faint bottom refraction reflection
                            if (tier == GlassTier.Floating) {
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = topHighlightAlpha * 0.18f),
                                            Color.Transparent,
                                        ),
                                    ),
                                    start = Offset(size.width * 0.25f, size.height - strokeWidth / 2f),
                                    end = Offset(size.width * 0.75f, size.height - strokeWidth / 2f),
                                    strokeWidth = strokeWidth * 0.75f,
                                )
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .padding(contentPadding),
        content = content,
    )
}

/** Hairline that fades out at both ends — section separators, list edges. */
@Composable
fun Modifier.nexusFadeDivider(): Modifier {
    val line = NexusTheme.colors.borderStrong
    return this.drawWithCache {
        val stroke = 1.dp.toPx()
        onDrawBehind {
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, line, Color.Transparent),
                ),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = stroke,
            )
        }
    }
}

/** Standard vertical screen padding for floating docks. */
@Composable
fun dockClearance(): Dp = com.nexus.core.theme.NexusSpacing.dockClearance

@Composable
fun Alignment.Companion.centerBottom(): Alignment = Alignment.BottomCenter
