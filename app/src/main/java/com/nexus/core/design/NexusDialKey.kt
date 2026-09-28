package com.nexus.core.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.nexus.core.animation.NexusMotion
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusTheme

/**
 * Precision Glass Dial Key.
 *
 * Implements the Spatial Glass OS Secondary tier:
 * - Translucent glass disc with top specular edge catch
 * - Subtle inward press deformation (physical tactile spring)
 * - Restrained light reaction when pressed
 * - Kept in the conventional 3x4 layout for instant muscle memory
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

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(NexusMotion.press, easing = NexusMotion.standard),
        label = "keyScale",
    )

    NexusGlassSurface(
        tier = GlassTier.Secondary,
        shape = CircleShape,
        tint = if (pressed) colors.accent else null,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
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
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = digit,
                style = NexusTheme.type.keyNumber,
                color = if (pressed) colors.accentText else colors.textPrimary,
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
