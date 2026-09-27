package com.nexus.core.design

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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

/** The four areas of NEXUS. */
enum class NexusArea(val route: String, val label: String) {
    Home("home", "HOME"),
    People("people", "PEOPLE"),
    Dial("dial", "DIAL"),
    Activity("activity", "ACTIVITY");

    fun icon(): ImageVector = when (this) {
        Home -> Icons.Rounded.Home
        People -> Icons.Rounded.Person
        Dial -> Icons.Rounded.Dialpad
        Activity -> Icons.Rounded.History
    }

    companion object {
        fun from(route: String?): NexusArea? = entries.firstOrNull { it.route == route }
    }
}

/**
 * The navigation dock — a floating glass pill.
 *
 * Bottom navigation exists here because usability won: four destinations, always one thumb
 * away. What makes it NEXUS is the treatment — frosted pill, quiet labels, a single sliding
 * accent indicator, no Material bar.
 */
@Composable
fun NexusDock(
    selectedRoute: String?,
    onSelect: (NexusArea) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NexusTheme.colors
    val entries = NexusArea.entries
    val selectedArea = NexusArea.from(selectedRoute)
    val shape = RoundedCornerShape(NexusRadii.pill)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            .padding(horizontal = NexusSpacing.x5)
            .height(NexusSizes.dockHeight),
    ) {
        val itemWidth = maxWidth / entries.size
        val indicatorOffset by animateDpAsState(
            targetValue = selectedArea
                ?.let { area ->
                    itemWidth * entries.indexOf(area) + itemWidth / 2 - NexusSpacing.x3
                }
                ?: 0.dp,
            animationSpec = tween(NexusMotion.relaxed, easing = NexusMotion.emphasized),
            label = "dockIndicator",
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(shape)
                .background(NexusGlass.fill())
                .border(1.dp, NexusGlass.borderBrush(), shape),
        ) {
            if (selectedArea != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = NexusSpacing.x2)
                        .offset(x = indicatorOffset)
                        .width(NexusSpacing.x6)
                        .height(3.dp)
                        .clip(RoundedCornerShape(NexusRadii.pill))
                        .background(colors.accent),
                )
            }

            Row(Modifier.matchParentSize()) {
                entries.forEach { area ->
                    val active = area == selectedArea
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics {
                                role = Role.Tab
                                this.selected = active
                                contentDescription =
                                    "${area.label}${if (active) ", selected" else ""}"
                            }
                            .nexusClickable(clickHaptics = true) { onSelect(area) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = area.icon(),
                            contentDescription = null,
                            tint = if (active) colors.accentText else colors.textTertiary,
                            modifier = Modifier.height(NexusSizes.iconMd),
                        )
                        Text(
                            text = area.label,
                            style = NexusTheme.type.micro,
                            color = if (active) colors.accentText else colors.textTertiary,
                            modifier = Modifier.padding(top = NexusSpacing.x1),
                        )
                    }
                }
            }
        }
    }
}
