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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
            .height(64.dp)
            .clip(shape)
            .background(NexusGlass.fill())
            .border(1.dp, NexusGlass.borderBrush(), shape)
            .clickable(interactionSource = interactionSource, indication = null) {
                // Tap fallback: deliberate, animated, single-fire.
                if (!fired) {
                    scope.launch {
                        if (!reduced) progress.animateTo(1f, tween(180))
                        answer()
                    }
                }
            }
            .semantics {
                role = Role.Button
                contentDescription = "Answer call. Swipe the button to the right, or tap."
            },
    ) {
        val density = LocalDensity.current
        val range = maxWidth - thumbSize - 12.dp
        val thumbX = 6.dp + range * progress.value

        // Accent wash grows with the thumb — you can feel the call charging.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(thumbX + thumbSize / 2)
                .fillMaxHeight()
                .background(
                    colors.accent.copy(alpha = 0.10f + 0.22f * progress.value),
                    shape,
                ),
        )

        Text(
            text = "SWIPE TO ANSWER",
            style = NexusTheme.type.label,
            color = colors.textSecondary,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp)
                .graphicsLayer { alpha = (1f - progress.value * 2.2f).coerceIn(0f, 1f) },
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbX)
                .size(thumbSize)
                .clip(CircleShape)
                .background(colors.accent)
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
