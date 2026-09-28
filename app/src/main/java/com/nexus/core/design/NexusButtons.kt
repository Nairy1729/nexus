package com.nexus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.nexusClickable

enum class NexusActionStyle { Accent, Glass, Outline, Danger }

/**
 * Circular icon button — the only button shape in NEXUS.
 * Accent style carries a restrained outer glow; everything else stays flat and quiet.
 */
@Composable
fun NexusActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = NexusSizes.actionMd,
    iconSize: Dp = NexusSizes.iconLg,
    style: NexusActionStyle = NexusActionStyle.Glass,
    tint: androidx.compose.ui.graphics.Color? = null,
) {
    val colors = NexusTheme.colors
    val visual: Modifier = when (style) {
        NexusActionStyle.Accent -> Modifier
            .drawBehind {
                drawCircle(
                    color = colors.accent.copy(alpha = 0.16f),
                    radius = this.size.minDimension / 2f + 8f * density,
                )
            }
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        colors.accent,
                        colors.accentPressed,
                    ),
                ),
                CircleShape,
            )
            .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.30f), CircleShape)

        NexusActionStyle.Glass -> Modifier
            .background(NexusGlass.fill(GlassTier.Secondary), CircleShape)
            .border(1.dp, NexusGlass.borderBrush(GlassTier.Secondary), CircleShape)

        NexusActionStyle.Outline -> Modifier
            .border(1.dp, colors.borderStrong, CircleShape)

        NexusActionStyle.Danger -> Modifier
            .drawBehind {
                drawCircle(
                    color = colors.danger.copy(alpha = 0.16f),
                    radius = this.size.minDimension / 2f + 6f * density,
                )
            }
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        colors.danger.copy(alpha = 0.28f),
                        colors.danger.copy(alpha = 0.12f),
                    ),
                ),
                CircleShape,
            )
            .border(
                1.dp,
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.40f),
                        colors.danger.copy(alpha = 0.65f),
                    ),
                ),
                CircleShape,
            )
    }

    val contentColor = when (style) {
        NexusActionStyle.Accent -> colors.accentContent
        NexusActionStyle.Danger -> colors.danger
        else -> colors.textPrimary
    }

    Box(
        modifier = modifier
            .size(size)
            .then(visual)
            .semantics { this.contentDescription = contentDescription }
            .nexusClickable(clickHaptics = true, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint ?: contentColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** Circle + uppercase label underneath — the CALL / MESSAGE pair on profiles. */
@Composable
fun NexusLabeledAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = NexusSizes.actionLg,
    style: NexusActionStyle = NexusActionStyle.Glass,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        NexusActionButton(
            icon = icon,
            contentDescription = label,
            onClick = onClick,
            size = size,
            style = style,
            iconSize = NexusSizes.iconLg + 2.dp,
        )
        Spacer(Modifier.height(NexusSpacing.x2))
        Text(
            text = label.uppercase(),
            style = NexusTheme.type.label,
            color = NexusTheme.colors.textSecondary,
        )
    }
}

/** Small glass icon button for headers (back, search, toggles). */
@Composable
fun NexusIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = NexusSizes.touchMin,
) {
    NexusActionButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        size = size,
        iconSize = NexusSizes.iconMd,
        style = NexusActionStyle.Glass,
    )
}

/** Eyebrow section label: PRIORITY PEOPLE, RECENT, COMMUNICATION DNA. */
@Composable
fun NexusSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(),
            style = NexusTheme.type.label,
            color = NexusTheme.colors.textTertiary,
        )
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            trailing()
        }
    }
}

/** Screen header: optional back, title, trailing actions. */
@Composable
fun NexusHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(NexusSizes.touchMin)
            .padding(vertical = NexusSpacing.x1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            NexusIconButton(
                icon = Icons.Rounded.KeyboardArrowLeft,
                contentDescription = "Back",
                onClick = onBack,
                size = 44.dp,
            )
            Spacer(Modifier.size(NexusSpacing.x3))
        }
        Text(
            text = title.uppercase(),
            style = NexusTheme.type.title,
            color = NexusTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) trailing()
    }
}
