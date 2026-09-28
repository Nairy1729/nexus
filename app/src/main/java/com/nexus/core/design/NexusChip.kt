package com.nexus.core.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement as LayoutArrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.core.animation.NexusMotion
import com.nexus.core.theme.NexusRadii
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.nexusClickable

/**
 * Filter pill. Selection is marked by fill + border + label color — three signals, so it
 * never depends on color alone.
 */
@Composable
fun NexusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NexusTheme.colors
    val shape = RoundedCornerShape(NexusRadii.pill)

    val backgroundModifier = if (selected) {
        Modifier
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        colors.accentSoft,
                        colors.accent.copy(alpha = 0.16f),
                    ),
                ),
                shape,
            )
            .border(
                1.dp,
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.45f),
                        colors.accent.copy(alpha = 0.65f),
                    ),
                ),
                shape,
            )
    } else {
        Modifier
            .background(NexusGlass.fill(GlassTier.Minimal), shape)
            .border(1.dp, NexusGlass.borderBrush(GlassTier.Minimal), shape)
    }

    Box(
        modifier = modifier
            .height(NexusSizes.chipHeight)
            .clip(shape)
            .then(backgroundModifier)
            .semantics {
                role = Role.Button
                this.selected = selected
                contentDescription = "$label filter${if (selected) ", selected" else ""}"
            }
            .nexusClickable(clickHaptics = true, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = NexusTheme.type.label,
            color = if (selected) colors.accentText else colors.textSecondary,
        )
    }
}

/**
 * Two-up segmented toggle (ORBIT / LIST). The active segment gets a filled background and
 * stronger label — position, fill and color all carry the state.
 */
@Composable
fun NexusSegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NexusTheme.colors
    val shape = RoundedCornerShape(NexusRadii.pill)

    BoxWithConstraints(
        modifier = modifier
            .height(NexusSizes.chipHeight)
            .clip(shape)
            .background(NexusGlass.fill(GlassTier.Minimal))
            .border(1.dp, NexusGlass.borderBrush(GlassTier.Minimal), shape)
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / options.size.coerceAtLeast(1)
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = tween(NexusMotion.standardMs, easing = NexusMotion.emphasized),
            label = "segmentOffset",
        )

        // Sliding indicator pill
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(shape)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            colors.accentSoft,
                            colors.accent.copy(alpha = 0.20f),
                        ),
                    ),
                    shape,
                )
                .border(
                    1.dp,
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.40f),
                            colors.accent.copy(alpha = 0.65f),
                        ),
                    ),
                    shape,
                ),
        )

        Row(Modifier.fillMaxHeight()) {
            options.forEachIndexed { index, option ->
                val active = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics {
                            role = Role.Tab
                            this.selected = active
                            contentDescription = "$option${if (active) ", selected" else ""}"
                        }
                        .nexusClickable(clickHaptics = true) { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option.uppercase(),
                        style = NexusTheme.type.label,
                        color = if (active) colors.accentText else colors.textTertiary,
                    )
                }
            }
        }
    }
}

/** Horizontally scrolling filter row. */
@Composable
fun NexusChipRow(
    chips: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = NexusSpacing.gutter),
        horizontalArrangement = LayoutArrangement.spacedBy(NexusSpacing.x2),
    ) {
        items(chips.size) { index ->
            NexusChip(
                label = chips[index],
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
            )
        }
    }
}
