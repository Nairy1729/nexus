package com.nexus.core.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.core.animation.reducedMotion
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.theme.NexusRadii
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import kotlinx.coroutines.launch

/**
 * Swipe-to-answer.
 *
 * Drag is the primary gesture (it cannot fire from an accidental tap), but a tap on the
 * track also answers after a deliberate slide — TalkBack users get a real button, and
 * impatient users get a shortcut. Thresholds click once on the way out, not continuously.
 */
@Composable
fun NexusSwipeToAnswer(
    onAnswer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NexusTheme.colors
    val haptics = LocalNexusHaptics.current
    val scope = rememberCoroutineScope()
    val reduced = reducedMotion()
    val progress = remember { Animatable(0f) }
    var dragTarget by remember { mutableFloatStateOf(0f) }
    var armed by remember { mutableStateOf(false) }
    var fired by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(NexusRadii.pill)

    val thumbSize = 54.dp
    val interactionSource = remember { MutableInteractionSource() }

    fun answer() {
        if (fired) return
        fired = true
        haptics.confirm()
        onAnswer()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(shape)
            .background(NexusGlass.fill(GlassTier.Secondary))
            .border(1.dp, NexusGlass.borderBrush(GlassTier.Secondary), shape)
            .clickable(interactionSource = interactionSource, indication = null) {
                // Accessible tap fallback: deliberate, animated, single-fire.
                if (!fired) {
                    scope.launch {
                        if (!reduced) progress.animateTo(1f, tween(180))
                        answer()
                    }
                }
            }
            .semantics {
                role = Role.Button
                contentDescription = "Answer call. Slide the button to the right, or tap."
            },
    ) {
        val density = LocalDensity.current
        val range = maxWidth - thumbSize - 12.dp
        val thumbX = 6.dp + range * progress.value

        // Liquid charging progress wash behind thumb
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(thumbX + thumbSize / 2)
                .fillMaxHeight()
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            colors.accent.copy(alpha = 0.06f),
                            colors.accent.copy(alpha = 0.16f + 0.24f * progress.value),
                        ),
                    ),
                    shape,
                ),
        )

        // Shimmering prompt text
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 60.dp)
                .graphicsLayer { alpha = (1f - progress.value * 2.4f).coerceIn(0f, 1f) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "SLIDE TO ANSWER",
                style = NexusTheme.type.label,
                color = colors.textSecondary,
                letterSpacing = 1.5.sp,
            )
        }

        // Liquid Glass Thumb
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbX)
                .size(thumbSize)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.35f),
                            colors.accent,
                            colors.accentPressed,
                        ),
                        center = androidx.compose.ui.geometry.Offset(
                            with(density) { (thumbSize * 0.4f).toPx() },
                            with(density) { (thumbSize * 0.35f).toPx() },
                        ),
                        radius = with(density) { (thumbSize * 0.9f).toPx() },
                    ),
                    CircleShape,
                )
                .border(
                    1.dp,
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                            colors.accent.copy(alpha = 0.40f),
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.20f),
                        ),
                    ),
                    CircleShape,
                )
                .pointerInput(range) {
                    detectDragGestures(
                        onDragStart = { haptics.tick() },
                        onDrag = { change, drag ->
                            change.consume()
                            val next = (dragTarget + drag.x / with(density) { range.toPx() })
                                .coerceIn(0f, 1f)
                            dragTarget = next
                            if (!armed && next >= 0.45f) {
                                armed = true
                                haptics.tick()
                            }
                            scope.launch { progress.snapTo(next) }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (dragTarget >= 0.88f) {
                                    if (!reduced) progress.animateTo(1f, tween(120))
                                    answer()
                                } else {
                                    haptics.tick()
                                    dragTarget = 0f
                                    armed = false
                                    progress.animateTo(
                                        0f,
                                        spring(dampingRatio = 0.92f, stiffness = Spring.StiffnessMediumLow),
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                dragTarget = 0f
                                armed = false
                                progress.animateTo(0f, spring(dampingRatio = 0.92f, stiffness = Spring.StiffnessMediumLow))
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Call,
                contentDescription = null,
                tint = colors.accentContent,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
