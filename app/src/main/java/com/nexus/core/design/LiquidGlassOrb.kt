package com.nexus.core.design

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.nexus.core.animation.LocalReducedMotion
import com.nexus.core.theme.NexusTheme
import kotlin.math.sin

/**
 * Liquid Glass Orb — Spatial Glass OS object primitive.
 *
 * Renders a tactile, translucent orb with:
 * - Ambient volumetric glow in the contact's contextual tint
 * - Breathing pulse wave (optional)
 * - Translucent liquid glass body with internal caustic core
 * - Physical top specular light catch
 * - Hairline Fresnel refractive rim
 * - Crisp centered monogram typography
 */
@Composable
fun LiquidGlassOrb(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    tint: Color = NexusTheme.colors.accent,
    pulse: Boolean = false,
    isHero: Boolean = false,
    hasRings: Boolean = false,
    contentDescription: String? = null,
) {
    val colors = NexusTheme.colors
    val monogram = monogramOf(name)
    val reduced = LocalReducedMotion.current

    val transition = rememberInfiniteTransition(label = "liquidGlassOrb")
    val pulseProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseProgress",
    )
    val causticPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500),
            repeatMode = RepeatMode.Restart,
        ),
        label = "causticPhase",
    )

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val radius = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // 1. Ambient volumetric light glow
            val glowRadius = radius * (if (isHero) 1.55f else 1.25f)
            val glowAlpha = if (reduced) 0.20f else (0.16f + 0.12f * pulseProgress)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tint.copy(alpha = glowAlpha),
                        tint.copy(alpha = glowAlpha * 0.35f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = glowRadius,
                ),
                radius = glowRadius,
                center = center,
            )

            // 2. Soft breathing pulse wave
            if (pulse && !reduced) {
                val waveRadius = radius + (4f + pulseProgress * (if (isHero) 18f else 10f)) * density
                val waveAlpha = (0.28f * (1f - pulseProgress)).coerceIn(0f, 1f)
                drawCircle(
                    color = tint.copy(alpha = waveAlpha),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(width = 1.25f * density),
                )
            }

            // 3. Translucent liquid glass sphere body
            // Multi-stop radial gradient with caustic center
            val causticShift = if (!reduced && isHero) sin(causticPhase) * 0.05f else 0f
            val focalOffset = Offset(
                center.x - radius * (0.18f + causticShift),
                center.y - radius * 0.22f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF0F1F6).copy(alpha = 0.25f),
                        tint.copy(alpha = 0.18f),
                        colors.surfaceRaised.copy(alpha = 0.88f),
                        tint.copy(alpha = 0.35f),
                    ),
                    center = focalOffset,
                    radius = radius * 1.15f,
                ),
                radius = radius,
                center = center,
            )

            // 4. Subtle internal fluid caustic reflection (for hero orbs)
            if (isHero && !reduced) {
                val innerY = center.y + sin(causticPhase + 1.5f) * radius * 0.15f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            tint.copy(alpha = 0.22f),
                            Color.Transparent,
                        ),
                        center = Offset(center.x, innerY),
                        radius = radius * 0.65f,
                    ),
                    radius = radius * 0.65f,
                    center = Offset(center.x, innerY),
                )
            }

            // 5. Hairline refractive Fresnel rim (physical light catch)
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.75f),
                        tint.copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.20f),
                    ),
                    startY = center.y - radius,
                    endY = center.y + radius,
                ),
                radius = radius - 0.6f * density,
                center = center,
                style = Stroke(width = 1.1f * density),
            )

            // 6. Top-down specular light glint (curved reflection)
            val specularWidth = radius * 1.05f
            val specularHeight = radius * 0.48f
            val specularLeft = center.x - specularWidth / 2f
            val specularTop = center.y - radius + 3f * density
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                    startY = specularTop,
                    endY = specularTop + specularHeight,
                ),
                topLeft = Offset(specularLeft, specularTop),
                size = Size(specularWidth, specularHeight),
            )

            // 7. Optional glass orbit ring (e.g. for favorites)
            if (hasRings) {
                val ringW = radius * 2.35f
                val ringH = radius * 0.70f
                drawOval(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            tint.copy(alpha = 0.50f),
                            Color.White.copy(alpha = 0.70f),
                            tint.copy(alpha = 0.50f),
                            Color.Transparent,
                        ),
                    ),
                    topLeft = Offset(center.x - ringW / 2f, center.y - ringH / 2f),
                    size = Size(ringW, ringH),
                    style = Stroke(width = 1.25f * density),
                )
            }
        }

        // Monogram in soft white
        Text(
            text = monogram,
            color = colors.textPrimary,
            fontSize = (size.value * (if (isHero) 0.32f else 0.30f)).sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )
    }
}

private fun monogramOf(name: String): String = when {
    name.isBlank() -> "#"
    else -> {
        val parts = name.trim().split(Regex("\\s+"))
        when {
            parts.size >= 2 && parts[1].isNotEmpty() ->
                "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            name.length <= 3 -> name.uppercase()
            else -> name.take(2).uppercase()
        }
    }
}
