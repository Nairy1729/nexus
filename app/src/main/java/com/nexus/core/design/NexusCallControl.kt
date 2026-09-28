package com.nexus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.core.theme.NexusRadii
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.nexusClickable

/**
 * In-call control: glass circle + quiet label. Selected state changes fill, border, icon
 * color and the announced state — four signals, never color alone.
 */
@Composable
fun NexusCallControl(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = NexusSizes.actionSm,
) {
    val colors = NexusTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .then(
                    if (selected) {
                        Modifier
                            .drawBehind {
                                drawCircle(
                                    color = colors.accent.copy(alpha = 0.22f),
                                    radius = this.size.minDimension / 2f + 4f * density,
                                )
                            }
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        colors.accentSoft,
                                        colors.accent.copy(alpha = 0.22f),
                                    ),
                                ),
                                CircleShape,
                            )
                            .border(
                                1.dp,
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.45f),
                                        colors.accent.copy(alpha = 0.70f),
                                    ),
                                ),
                                CircleShape,
                            )
                    } else {
                        Modifier
                            .background(NexusGlass.fill(GlassTier.Secondary), CircleShape)
                            .border(1.dp, NexusGlass.borderBrush(GlassTier.Secondary), CircleShape)
                    }
                )
                .semantics {
                    role = Role.Switch
                    this.selected = selected
                    contentDescription = "$label${if (selected) ", on" else ", off"}"
                }
                .nexusClickable(clickHaptics = true, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) colors.accentText else colors.textPrimary,
                modifier = Modifier.size(NexusSizes.iconMd + 2.dp),
            )
        }
        Spacer(Modifier.height(NexusSpacing.x2))
        Text(
            text = label.uppercase(),
            style = NexusTheme.type.micro,
            color = if (selected) colors.accentText else colors.textTertiary,
        )
    }
}

/** Text-only secondary action ("PROFILE", "ADD CALL"). */
@Composable
fun NexusTextAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    val colors = NexusTheme.colors
    Box(
        modifier = modifier
            .clip(NexusRadii.pill.let { androidx.compose.foundation.shape.RoundedCornerShape(it) })
            .padding(horizontal = NexusSpacing.x4, vertical = NexusSpacing.x2)
            .semantics {
                role = Role.Button
                contentDescription = label
            }
            .nexusClickable(clickHaptics = true, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = NexusTheme.type.label,
            color = if (accent) colors.accentText else colors.textSecondary,
        )
    }
}
