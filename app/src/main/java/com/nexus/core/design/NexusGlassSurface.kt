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
 * The NEXUS glass surface.
 *
 * Deliberately not a RenderEffect blur: blurring behind every panel is the fastest way to
 * drop frames on a mid-range phone. Frosted depth is faked with a two-stop translucent
 * gradient, a hairline border, and one top highlight — visually indistinguishable at these
 * opacities, orders of magnitude cheaper.
 */
object NexusGlass {

    /** Vertical glass fill for the current theme. */
    @Composable
    fun fill(): Brush {
        val dark = NexusTheme.colors.isDark
        return if (dark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.060f),
                    Color.White.copy(alpha = 0.024f),
                ),
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.70f),
                ),
            )
        }
    }

    /** Hairline border brush — fades toward the bottom like real edge-lit glass. */
    @Composable
    fun borderBrush(): Brush {
        val base = NexusTheme.colors.borderStrong
        return Brush.verticalGradient(
            colors = listOf(base, base.copy(alpha = base.alpha * 0.35f)),
        )
    }
}

/**
 * Rounded glass panel. [contentPadding] keeps inner rhythm on the spacing scale.
 */
@Composable
fun NexusGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(NexusRadii.lg),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    highlightEdge: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = NexusTheme.colors.isDark
    Box(
        modifier = modifier
            .clip(shape)
            .background(NexusGlass.fill())
            .then(
                if (highlightEdge) {
                    Modifier.drawWithCache {
                        val strokeWidth = 1.dp.toPx()
                        onDrawBehind {
                            // Top edge highlight — the "lit from above" tell of real glass.
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = if (isDark) 0.16f else 0.65f),
                                        Color.Transparent,
                                    ),
                                ),
                                start = Offset(0f, strokeWidth / 2),
                                end = Offset(size.width, strokeWidth / 2),
                                strokeWidth = strokeWidth,
                            )
                        }
                    }
                } else {
                    Modifier
                }
            )
            .border(width = 1.dp, brush = NexusGlass.borderBrush(), shape = shape)
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
