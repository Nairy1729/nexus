package com.nexus.core.ui

import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.nexus.core.animation.NexusMotion
import com.nexus.core.haptics.LocalNexusHaptics

/**
 * Press behaviour shared by every tappable NEXUS surface:
 * no ripple (it fights the AMOLED aesthetic), a bounded 3% scale-down, optional haptic.
 * The scale is the feedback — visible, honest, and never blocking.
 */
@Composable
fun Modifier.nexusClickable(
    enabled: Boolean = true,
    clickHaptics: Boolean = false,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        animationSpec = tween(NexusMotion.press, easing = NexusMotion.standard),
        label = "pressScale",
    )
    val haptics = LocalNexusHaptics.current
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = {
                if (clickHaptics) haptics.select()
                onClick()
            },
            onLongClick = null,
        )
}

/** Variant with long-press (contact nodes → quick actions). */
@Composable
fun Modifier.nexusCombinedClickable(
    enabled: Boolean = true,
    clickHaptics: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        animationSpec = tween(NexusMotion.press, easing = NexusMotion.standard),
        label = "pressScale",
    )
    val haptics = LocalNexusHaptics.current
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = {
                if (clickHaptics) haptics.select()
                onClick()
            },
            onLongClick = onLongClick?.let {
                {
                    haptics.tick()
                    it()
                }
            },
        )
}

/** Haptic helper for imperative gesture code. */
fun View.nexusTick() {
    performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
}
