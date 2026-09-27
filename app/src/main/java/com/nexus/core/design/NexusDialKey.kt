package com.nexus.core.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.core.animation.NexusMotion
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusTheme

/**
 * Circular dial key.
 *
 * Kept in the conventional 3x4 arrangement on purpose: muscle memory is faster than any
 * radial experiment — the futurism lives in the geometry, the motion and the type, not in
 * re-learning where 7 is. Every press is a dry haptic tick.
 */
@Composable
fun NexusDialKey(
    digit: String,
    letters: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = NexusSizes.dialKey,
    onLongClick: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val colors = NexusTheme.colors
    val haptics = LocalNexusHaptics.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val background by animateColorAsState(
        targetValue = if (pressed) colors.surfacePressed else colors.surface.copy(alpha = 0.55f),
        animationSpec = tween(NexusMotion.press, easing = NexusMotion.standard),
        label = "keyBg",
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = tween(NexusMotion.press, easing = NexusMotion.standard),
        label = "keyScale",
    )
    val border by animateColorAsState(
        targetValue = if (pressed) colors.borderStrong else colors.border,
        animationSpec = tween(NexusMotion.press),
        label = "keyBorder",
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
                    ?: (if (letters != null) "$digit, $letters" else digit)
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.key()
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = digit,
                style = NexusTheme.type.keyNumber,
                color = colors.textPrimary,
            )
            if (letters != null) {
                Text(
                    text = letters,
                    style = NexusTheme.type.keyLetters,
                    color = colors.textTertiary,
                )
            }
        }
    }
}
