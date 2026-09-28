package com.nexus.core.design.orb

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.core.animation.LocalReducedMotion
import com.nexus.core.theme.NexusTheme

/**
 * NexusOrb — Spatial Glass OS communication orb primitive.
 *
 * Integrates the animation principles and mathematics of Jakub Antalik's open-source
 * thinking-orbs project into the NEXUS Spatial Glass aesthetic:
 *
 * - Zero GC allocations per frame using reusable primitive frame buffers
 * - Translucent liquid glass core with Fresnel edge highlight
 * - Volumetric ambient aura in contextual contact tint
 * - 3D-depth projected luminous particles and laser filaments
 * - Full reduced motion accessibility support (static deterministic frame)
 *
 * @param state Current orb state ([NexusOrbState])
 * @param modifier Composable modifier
 * @param size Display size of the orb
 * @param intensity Brightness / alpha multiplier for particles and glow
 * @param accent Contextual tint color
 * @param speed Time progression multiplier
 * @param interactive Whether the orb responds to touch interactions
 * @param paused Whether the animation is temporarily frozen
 * @param seed Deterministic seed for contact-specific variation
 * @param showAtmosphere Whether to render the soft radial ambient back-glow
 * @param showCoreGlass Whether to render the internal translucent glass sphere
 * @param reducedMotion Accessible reduced motion setting
 * @param onClick Optional tap callback
 */
@Composable
fun NexusOrb(
    state: NexusOrbState = NexusOrbState.Idle,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    intensity: Float = 1.0f,
    accent: Color = NexusTheme.colors.accent,
    speed: Float = 1.0f,
    interactive: Boolean = false,
    paused: Boolean = false,
    seed: Long = 0L,
    showAtmosphere: Boolean = true,
    showCoreGlass: Boolean = true,
    reducedMotion: Boolean = LocalReducedMotion.current,
    onClick: (() -> Unit)? = null,
) {
    val colors = NexusTheme.colors
    val buffer = remember { OrbFrameBuffer() }

    // Precise VSYNC-driven time clock with zero allocations
    var elapsedSec by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(paused, reducedMotion, speed) {
        if (reducedMotion) {
            elapsedSec = 0.6f
            return@LaunchedEffect
        }
        var lastNanos = 0L
        while (!paused) {
            withFrameNanos { nowNanos ->
                if (lastNanos != 0L) {
                    val dt = (nowNanos - lastNanos) * 1e-9f
                    val clampedDt = dt.coerceIn(0f, 0.1f)
                    elapsedSec += clampedDt * speed
                }
                lastNanos = nowNanos
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = "Nexus Orb: ${state.label}" }
            .then(
                if (interactive && onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val sizePx = this.size.minDimension
            val center = Offset(this.size.width * 0.5f, this.size.height * 0.5f)
            val radius = sizePx * 0.5f * 0.82f

            // 1. Atmospheric Ambient Back-Glow
            if (showAtmosphere) {
                val auraRadius = sizePx * 0.62f
                val auraAlpha = (0.16f * intensity).coerceIn(0f, 1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = auraAlpha),
                            accent.copy(alpha = auraAlpha * 0.35f),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = auraRadius,
                    ),
                    radius = auraRadius,
                    center = center,
                )
            }

            // 2. Translucent Liquid Glass Core Sphere
            if (showCoreGlass) {
                // Internal glass volume
                val glassTopAlpha = if (colors.isDark) 0.20f else 0.45f
                val glassBottomAlpha = if (colors.isDark) 0.06f else 0.18f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.surfaceRaised.copy(alpha = glassTopAlpha * intensity),
                            colors.surface.copy(alpha = glassBottomAlpha * intensity),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                    center = center,
                )

                // Hairline refractive Fresnel rim
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f * intensity),
                            accent.copy(alpha = 0.15f * intensity),
                            colors.border.copy(alpha = 0.12f * intensity),
                        ),
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y + radius),
                    ),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 0.8f.dp.toPx()),
                )

                // Specular top light catch
                val specRadius = radius * 0.42f
                val specCenter = Offset(center.x, center.y - radius * 0.48f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f * intensity),
                            Color.White.copy(alpha = 0.06f * intensity),
                            Color.Transparent,
                        ),
                        center = specCenter,
                        radius = specRadius,
                    ),
                    radius = specRadius,
                    center = specCenter,
                )
            }

            // 3. Compute Frame via Pure-Math Geometry Engine
            OrbGeometryEngine.computeFrame(
                state = state,
                size = sizePx,
                timeSec = elapsedSec,
                buffer = buffer,
                seed = seed,
            )

            // 4. Draw Lines / Laser Filaments (e.g. Connecting State)
            for (li in 0 until buffer.lineCount) {
                val x1 = buffer.lineX1[li]
                val y1 = buffer.lineY1[li]
                val x2 = buffer.lineX2[li]
                val y2 = buffer.lineY2[li]
                val white = buffer.lineWhite[li]
                val alpha = (buffer.lineAlpha[li] * intensity).coerceIn(0f, 1f)
                val w = buffer.lineW[li]

                val lineCol = lerp(
                    accent.copy(alpha = alpha * 0.45f),
                    Color.White.copy(alpha = alpha * 0.85f),
                    1f - white,
                )

                drawLine(
                    color = lineCol,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = w,
                    cap = StrokeCap.Round,
                )
            }

            // 5. Draw Luminous Light Particles & Glass Beads
            for (di in 0 until buffer.dotCount) {
                val idx = buffer.dotIndices[di]
                val x = buffer.dotX[idx]
                val y = buffer.dotY[idx]
                val z = buffer.dotZ[idx]
                val r = buffer.dotR[idx]
                val white = buffer.dotWhite[idx]
                val alpha = (buffer.dotAlpha[idx] * intensity).coerceIn(0f, 1f)

                val depth = ((z / radius + 1f) * 0.5f).coerceIn(0f, 1f)

                // High specular gleam for close nodes, glowing accent for mid, muted cool tone for back
                val dotColor = if (depth > 0.80f && white < 0.22f) {
                    Color.White.copy(alpha = alpha)
                } else {
                    lerp(
                        colors.textTertiary.copy(alpha = alpha * 0.55f),
                        accent.copy(alpha = alpha),
                        (depth * 0.85f + (1f - white) * 0.15f).coerceIn(0f, 1f),
                    )
                }

                drawCircle(
                    color = dotColor,
                    radius = r,
                    center = Offset(x, y),
                )
            }
        }
    }
}
