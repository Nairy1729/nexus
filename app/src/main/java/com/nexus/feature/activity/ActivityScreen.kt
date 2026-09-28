package com.nexus.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexus.core.design.NexusActionButton
import com.nexus.core.design.NexusActionStyle
import com.nexus.core.design.NexusAvatar
import com.nexus.core.design.NexusChipRow
import com.nexus.core.design.NexusHeader
import com.nexus.core.design.NexusSearchField
import com.nexus.core.design.NexusSectionLabel
import com.nexus.core.design.NexusTimelineRow
import com.nexus.core.design.TimelineMarker
import com.nexus.core.design.TimelineSlot
import com.nexus.core.design.timelineLegendDescription
import com.nexus.core.di.AppContainer
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.formatDuration
import com.nexus.core.ui.relativeTime
import com.nexus.data.model.CallType
import com.nexus.data.model.ResolvedCall

/**
 * ACTIVITY — the full communication timeline.
 *
 * Day-grouped, filterable, searchable. The rail and markers are shape-coded (solid,
 * hollow, red, diamond) so the log is readable without color — the legend is exposed
 * to assistive tech as a single description.
 */
@Composable
fun ActivityScreen(
    onOpenContact: (String) -> Unit,
    onCallBack: (ResolvedCall) -> Unit,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivityViewModel = viewModel(factory = ActivityViewModel.factory(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            .padding(horizontal = NexusSpacing.gutter),
    ) {
        NexusHeader(
            title = "Events",
            trailing = {
                Text(
                    text = "${state.visibleCount} of ${state.totalCount}",
                    style = NexusTheme.type.meta,
                    color = NexusTheme.colors.textTertiary,
                )
            },
        )

        Spacer(Modifier.height(NexusSpacing.x3))

        NexusSearchField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            placeholder = "Search events",
        )

        Spacer(Modifier.height(NexusSpacing.x3))

        NexusChipRow(
            chips = ActivityFilter.entries.map { it.label },
            selectedIndex = ActivityFilter.entries.indexOf(state.filter),
            onSelect = { index -> viewModel.setFilter(ActivityFilter.entries[index]) },
        )

        Spacer(Modifier.height(NexusSpacing.x4))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = NexusSpacing.dockClearance),
        ) {
            if (state.groups.isEmpty()) {
                item { EmptyLog(filter = state.filter) }
            }
            state.groups.forEach { group ->
                item(key = "header-${group.label}") {
                    NexusSectionLabel(
                        text = group.label,
                        modifier = Modifier.padding(top = NexusSpacing.x4),
                    )
                }
                items(
                    count = group.calls.size,
                    key = { index -> group.calls[index].record.id },
                ) { index ->
                    val call = group.calls[index]
                    val slot = when (index) {
                        0 -> if (group.calls.size == 1) TimelineSlot.Single else TimelineSlot.First
                        group.calls.lastIndex -> TimelineSlot.Last
                        else -> TimelineSlot.Middle
                    }
                    ActivityRow(
                        call = call,
                        slot = slot,
                        onOpen = { call.contactId?.let(onOpenContact) },
                        onCallBack = { onCallBack(call) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(
    call: ResolvedCall,
    slot: TimelineSlot,
    onOpen: () -> Unit,
    onCallBack: () -> Unit,
) {
    val colors = NexusTheme.colors
    val marker = when (call.type) {
        CallType.Incoming -> TimelineMarker.Incoming
        CallType.Outgoing -> TimelineMarker.Outgoing
        CallType.Missed -> TimelineMarker.Missed
    }
    val typeLabel = when (call.type) {
        CallType.Incoming -> "Incoming"
        CallType.Outgoing -> "Outgoing"
        CallType.Missed -> "Missed"
    }
    val contactTint = com.nexus.core.spatial.model.SpatialContactMapper.contactTintColor(call.contactId)

    NexusTimelineRow(slot = slot, marker = marker, onClick = onOpen) {
        com.nexus.core.design.LiquidGlassOrb(
            name = call.displayName,
            size = 36.dp,
            tint = contactTint,
            modifier = Modifier.padding(end = NexusSpacing.x3),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = call.displayName,
                style = NexusTheme.type.subhead,
                color = colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = "$typeLabel · ${relativeTime(call.timestampMillis)}",
                style = NexusTheme.type.meta,
                color = if (call.isMissed) colors.danger else colors.textTertiary,
                maxLines = 1,
            )
        }
        if (call.durationSeconds > 0) {
            Text(
                text = formatDuration(call.durationSeconds.toLong()),
                style = NexusTheme.type.meta,
                color = colors.textTertiary,
                modifier = Modifier.padding(end = NexusSpacing.x3),
            )
        }
        NexusActionButton(
            icon = Icons.Rounded.Call,
            contentDescription = "Call ${call.displayName}",
            onClick = onCallBack,
            size = 44.dp,
            iconSize = NexusSizes.iconSm,
            style = NexusActionStyle.Glass,
        )
    }
}

@Composable
private fun EmptyLog(filter: ActivityFilter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = NexusSpacing.x16)
            .semantics {
                contentDescription = timelineLegendDescription()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (filter == ActivityFilter.All) {
                "NO ACTIVITY YET"
            } else {
                "NO ${filter.label} CALLS"
            },
            style = NexusTheme.type.label,
            color = NexusTheme.colors.textTertiary,
        )
        Spacer(Modifier.height(NexusSpacing.x3))
        Text(
            text = "Calls you make and receive will land here.",
            style = NexusTheme.type.meta,
            color = NexusTheme.colors.textTertiary,
        )
    }
}
