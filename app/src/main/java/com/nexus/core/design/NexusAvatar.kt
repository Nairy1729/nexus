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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.core.animation.LocalReducedMotion
import com.nexus.core.theme.NexusTheme

enum class AvatarRing { None, Favorite }

/**
 * The person-node face: monogram on a graphite disc, hairline edge, optional accent ring.
 *
 * Deliberately monochrome — avatars never introduce color. Status is encoded by the ring
 * (favorite) and the recency dot (interacted within the last hour), never by hue alone.
 */
@Composable
fun NexusAvatar(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    ring: AvatarRing = AvatarRing.None,
    recent: Boolean = false,
    pulse: Boolean = false,
    contentDescription: String? = null,
) {
    val colors = NexusTheme.colors
    val monogram = monogramOf(name)
    val hash = name.hashCode()
    val inverted = hash and 1 == 1
    val reduced = LocalReducedMotion.current

    val pulseProgress = if (pulse) {
        val transition = rememberInfiniteTransition(label = "avatarPulse")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "avatarPulseValue",
        ).value
    } else {
        0f
    }

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

            // Breathing halo — call screens only, parked under reduced motion.
            if (pulse && pulseProgress > 0f && !reduced) {
                drawCircle(
                    color = colors.accent.copy(alpha = 0.22f * (1f - pulseProgress)),
                    radius = radius + (6f + pulseProgress * 10f) * density,
                    style = Stroke(width = 1.5f * density),
                )
            }

            // Graphite disc with a diagonal gradient; direction alternates per person for
            // a quiet sense of individuality while staying strictly monochrome.
            drawCircle(
                brush = if (inverted) {
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(colors.surface, colors.surfaceRaised),
                    )
                } else {
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(colors.surfaceRaised, colors.surface),
                    )
                },
                radius = radius,
                center = center,
            )

            // Hairline edge
            drawCircle(
                color = colors.border,
                radius = radius - 0.5f * density,
                center = center,
                style = Stroke(width = 1f * density),
            )

            // Favorite ring — the accent appears only where it means something.
            if (ring == AvatarRing.Favorite) {
                drawCircle(
                    color = colors.accent,
                    radius = radius - 1.5f * density,
                    center = center,
                    style = Stroke(width = 1.5f * density),
                )
            }

            // Recency dot with a knockout edge so it reads on any fill.
            if (recent) {
                val dotCenter = Offset(this.size.width - 7f * density, this.size.height - 7f * density)
                drawCircle(color = colors.surface, radius = 5.5f * density, center = dotCenter)
                drawCircle(color = colors.accent, radius = 3.5f * density, center = dotCenter)
            }
        }
        Text(
            text = monogram,
            color = NexusTheme.colors.textPrimary,
            fontSize = (size.value * 0.30f).sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        )
    }
}

/** R M for "Rahul Menon", SN for "Sneha", MOM for "Mom". */
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
