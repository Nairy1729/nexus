package com.nexus.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.nexusClickable

/** What happened — encoded as a shape, never by color alone. */
enum class TimelineMarker { Incoming, Outgoing, Missed, Message, Unknown }

/** Where the row sits in the rail so line segments terminate cleanly. */
enum class TimelineSlot { First, Middle, Last, Single }

/**
 * One entry in the NEXUS communication timeline.
 *
 * The rail and marker are drawn per row, which keeps this composable usable inside both
 * Column and LazyColumn without a custom layout — and only visible rows pay for drawing.
 */
@Composable
fun NexusTimelineRow(
    slot: TimelineSlot,
    marker: TimelineMarker,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = NexusSpacing.x3),
    content: @Composable RowScope.() -> Unit,
) {
    val colors = NexusTheme.colors
    val railX = NexusSpacing.x3

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.nexusClickable(clickHaptics = true, onClick = onClick) else Modifier),
    ) {
        Canvas(Modifier.matchParentSize()) {
            val railPx = railX.toPx()
            val centerY = size.height / 2f
            val stroke = 1.25f * density

            // Rail segments — First starts at the marker, Last ends at it.
            val railGlowColor = colors.accent.copy(alpha = 0.08f)
            if (slot != TimelineSlot.First && slot != TimelineSlot.Single) {
                // Subtle glow rail
                drawLine(
                    color = railGlowColor,
                    start = androidx.compose.ui.geometry.Offset(railPx, 0f),
                    end = androidx.compose.ui.geometry.Offset(railPx, centerY),
                    strokeWidth = 3f * density,
                    cap = StrokeCap.Round,
                )
                // Core rail
                drawLine(
                    color = colors.orbitLine,
                    start = androidx.compose.ui.geometry.Offset(railPx, 0f),
                    end = androidx.compose.ui.geometry.Offset(railPx, centerY),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
            if (slot != TimelineSlot.Last && slot != TimelineSlot.Single) {
                // Subtle glow rail
                drawLine(
                    color = railGlowColor,
                    start = androidx.compose.ui.geometry.Offset(railPx, centerY),
                    end = androidx.compose.ui.geometry.Offset(railPx, size.height),
                    strokeWidth = 3f * density,
                    cap = StrokeCap.Round,
                )
                // Core rail
                drawLine(
                    color = colors.orbitLine,
                    start = androidx.compose.ui.geometry.Offset(railPx, centerY),
                    end = androidx.compose.ui.geometry.Offset(railPx, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }

            val center = androidx.compose.ui.geometry.Offset(railPx, centerY)
            when (marker) {
                TimelineMarker.Incoming -> {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.20f),
                        radius = 7f * density,
                        center = center,
                    )
                    drawCircle(
                        color = colors.textPrimary,
                        radius = 4f * density,
                        center = center,
                    )
                }

                TimelineMarker.Outgoing -> {
                    drawCircle(
                        color = colors.surface,
                        radius = 5.5f * density,
                        center = center,
                    )
                    drawCircle(
                        color = colors.textSecondary,
                        radius = 4.5f * density,
                        center = center,
                        style = Stroke(width = 1.5f * density),
                    )
                }

                TimelineMarker.Missed -> {
                    drawCircle(
                        color = colors.danger.copy(alpha = 0.28f),
                        radius = 9f * density,
                        center = center,
                    )
                    drawCircle(
                        color = colors.danger,
                        radius = 4.5f * density,
                        center = center,
                    )
                }

                TimelineMarker.Message -> {
                    val r = 5f * density
                    val path = Path().apply {
                        moveTo(center.x, center.y - r)
                        lineTo(center.x + r, center.y)
                        lineTo(center.x, center.y + r)
                        lineTo(center.x - r, center.y)
                        close()
                    }
                    drawPath(path, color = colors.textTertiary)
                }

                TimelineMarker.Unknown -> drawCircle(
                    color = colors.textTertiary,
                    radius = 4.5f * density,
                    center = center,
                    style = Stroke(width = 1.5f * density),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = railX + NexusSpacing.x4,
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding(),
                )
                .padding(end = NexusSpacing.x1),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/** Shape legend for the DNA / activity timelines — exposed to TalkBack as one string. */
fun timelineLegendDescription(): String =
    "Timeline legend: solid dot is incoming, hollow dot is outgoing, red dot is missed, diamond is message"
