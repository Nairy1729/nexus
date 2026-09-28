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
     * Driven directly by active [NexusTheme.colors] tokens.
     */
    @Composable
    fun fill(tier: GlassTier = GlassTier.Primary, tint: Color? = null): Brush {
        val colors = NexusTheme.colors

        val (mulTop, mulMid, mulBottom) = when (tier) {
            GlassTier.Primary -> Triple(1.0f, 1.0f, 1.0f)
            GlassTier.Secondary -> Triple(1.22f, 1.18f, 1.15f)
            GlassTier.Floating -> Triple(1.45f, 1.35f, 1.25f)
            GlassTier.Minimal -> Triple(0.70f, 0.65f, 0.60f)
        }

        val topColor = colors.glassFillTop.copy(alpha = (colors.glassFillTop.alpha * mulTop).coerceIn(0f, 1f))
        val midColor = if (tint != null) {
            tint.copy(alpha = 0.05f)
        } else {
            colors.glassFillMid.copy(alpha = (colors.glassFillMid.alpha * mulMid).coerceIn(0f, 1f))
        }
        val bottomColor = colors.glassFillBottom.copy(alpha = (colors.glassFillBottom.alpha * mulBottom).coerceIn(0f, 1f))

        return Brush.verticalGradient(
            colors = listOf(topColor, midColor, bottomColor),
        )
    }

    /**
     * Precision edge-lit border brush — brightest where ambient light hits the top edge,
     * fading into the surrounding spatial atmosphere toward the bottom.
     */
    @Composable
    fun borderBrush(tier: GlassTier = GlassTier.Primary, tint: Color? = null): Brush {
        val colors = NexusTheme.colors

        val topMul = when (tier) {
            GlassTier.Primary -> 1.0f
            GlassTier.Secondary -> 1.15f
            GlassTier.Floating -> 1.30f
            GlassTier.Minimal -> 0.70f
        }

        val topColor = if (tint != null) {
            tint.copy(alpha = (colors.glassBorderTop.alpha * topMul * 1.1f).coerceIn(0f, 1f))
        } else {
            colors.glassBorderTop.copy(alpha = (colors.glassBorderTop.alpha * topMul).coerceIn(0f, 1f))
        }

        val bottomColor = if (tint != null) {
            tint.copy(alpha = (colors.glassBorderBottom.alpha * 0.7f).coerceIn(0f, 1f))
        } else {
            colors.glassBorderBottom
        }

        return Brush.verticalGradient(
            colors = listOf(
                topColor,
                topColor.copy(alpha = (topColor.alpha + bottomColor.alpha) * 0.45f),
                bottomColor,
            ),
        )
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
    val colors = NexusTheme.colors
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
                        val specMultiplier = when (tier) {
                            GlassTier.Primary -> 1.0f
                            GlassTier.Secondary -> 1.2f
                            GlassTier.Floating -> 1.4f
                            GlassTier.Minimal -> 0.65f
                        }

                        val specColors = if (tint != null) {
                            listOf(
                                Color.Transparent,
                                tint.copy(alpha = (0.35f * specMultiplier).coerceIn(0f, 1f)),
                                colors.glassSpecularCenter.copy(
                                    alpha = (colors.glassSpecularCenter.alpha * specMultiplier).coerceIn(0f, 1f)
                                ),
                                tint.copy(alpha = (0.25f * specMultiplier).coerceIn(0f, 1f)),
                                Color.Transparent,
                            )
                        } else {
                            listOf(
                                colors.glassSpecularStart,
                                colors.glassSpecularSecondary.copy(
                                    alpha = (colors.glassSpecularSecondary.alpha * specMultiplier).coerceIn(0f, 1f)
                                ),
                                colors.glassSpecularCenter.copy(
                                    alpha = (colors.glassSpecularCenter.alpha * specMultiplier).coerceIn(0f, 1f)
                                ),
                                colors.glassSpecularSecondary.copy(
                                    alpha = (colors.glassSpecularSecondary.alpha * specMultiplier).coerceIn(0f, 1f)
                                ),
                                colors.glassSpecularEnd,
                            )
                        }

                        onDrawBehind {
                            // Top edge specular gleam: physical light hitting top of the glass pane
                            drawLine(
                                brush = Brush.horizontalGradient(colors = specColors),
                                start = Offset(size.width * 0.06f, strokeWidth / 2f),
                                end = Offset(size.width * 0.94f, strokeWidth / 2f),
                                strokeWidth = strokeWidth,
                            )

                            // For floating tier, add a faint bottom refraction reflection
                            if (tier == GlassTier.Floating) {
                                val refractionColor = colors.glassRefractionTint.copy(
                                    alpha = (colors.glassRefractionTint.alpha * 0.8f).coerceIn(0f, 1f)
                                )
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            refractionColor,
                                            Color.Transparent,
                                        ),
                                    ),
                                    start = Offset(size.width * 0.22f, size.height - strokeWidth / 2f),
                                    end = Offset(size.width * 0.78f, size.height - strokeWidth / 2f),
                                    strokeWidth = strokeWidth * 0.8f,
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
