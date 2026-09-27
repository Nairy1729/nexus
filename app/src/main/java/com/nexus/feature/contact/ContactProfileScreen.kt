package com.nexus.feature.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexus.core.design.AvatarRing
import com.nexus.core.design.NexusActionButton
import com.nexus.core.design.NexusActionStyle
import com.nexus.core.design.NexusAvatar
import com.nexus.core.design.NexusHeader
import com.nexus.core.design.NexusLabeledAction
import com.nexus.core.design.NexusSectionLabel
import com.nexus.core.design.NexusTimelineRow
import com.nexus.core.design.TimelineMarker
import com.nexus.core.design.TimelineSlot
import com.nexus.core.design.timelineLegendDescription
import com.nexus.core.di.AppContainer
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.formatDuration
import com.nexus.core.ui.relativeTime
import com.nexus.data.model.Contact
import java.util.Locale

/**
 * CONTACT — one person, three depths: identity (avatar, number, actions), the numbers
 * the relationship produces (stats, derived — never hard-coded), and the Communication
 * DNA timeline that shows what actually happened between you two.
 */
@Composable
fun ContactProfileScreen(
    contactId: String,
    onBack: () -> Unit,
    onCallBack: (Contact) -> Unit,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val viewModel: ContactViewModel = viewModel(
        key = "contact-$contactId",
        factory = ContactViewModel.factory(contactId, container),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalNexusHaptics.current
    val clipboard = LocalClipboardManager.current

    val contact = state.contact
    if (contact == null) {
        // Unknown id (deep link into a person that no longer exists) — leave quietly.
        Column(
            modifier = modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .padding(horizontal = NexusSpacing.gutter),
        ) {
            NexusHeader(title = "Contact", onBack = onBack)
            Text(
                text = "NOT FOUND",
                style = NexusTheme.type.label,
                color = NexusTheme.colors.textTertiary,
                modifier = Modifier.padding(top = NexusSpacing.x8),
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            .padding(horizontal = NexusSpacing.gutter)
            .verticalScroll(rememberScrollState()),
    ) {
        NexusHeader(title = contact.name, onBack = onBack)

        Spacer(Modifier.height(NexusSpacing.x6))

        // ---- Identity -------------------------------------------------------
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NexusAvatar(
                name = contact.name,
                size = NexusSizes.avatarXl,
                ring = if (contact.isFavorite) AvatarRing.Favorite else AvatarRing.None,
                contentDescription = "${contact.name}, profile photo placeholder",
            )
            Spacer(Modifier.height(NexusSpacing.x4))
            Text(
                text = contact.name,
                style = NexusTheme.type.hero,
                color = NexusTheme.colors.textPrimary,
            )
            Spacer(Modifier.height(NexusSpacing.x2))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.number,
                    style = NexusTheme.type.subhead,
                    color = NexusTheme.colors.textSecondary,
                )
                Spacer(Modifier.size(NexusSpacing.x3))
                NexusActionButton(
                    icon = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy number ${contact.number}",
                    onClick = {
                        haptics.select()
                        clipboard.setText(AnnotatedString(contact.number))
                    },
                    size = 36.dp,
                    iconSize = NexusSizes.iconSm,
                    style = NexusActionStyle.Glass,
                )
            }
            if (contact.isFavorite) {
                Spacer(Modifier.height(NexusSpacing.x2))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = NexusTheme.colors.accentText,
                        modifier = Modifier.size(NexusSizes.iconSm),
                    )
                    Spacer(Modifier.size(NexusSpacing.x2))
                    Text(
                        text = "FAVORITE",
                        style = NexusTheme.type.label,
                        color = NexusTheme.colors.accentText,
                    )
                }
            }
        }

        Spacer(Modifier.height(NexusSpacing.x8))

        // ---- Actions --------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            NexusLabeledAction(
                icon = Icons.Rounded.Call,
                label = "Call",
                style = NexusActionStyle.Accent,
                onClick = { onCallBack(contact) },
            )
        }

        Spacer(Modifier.height(NexusSpacing.x8))

        // ---- Stats ----------------------------------------------------------
        val stats = state.stats
        if (stats != null) {
            NexusSectionLabel(text = "Communication")
            Spacer(Modifier.height(NexusSpacing.x4))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCell(
                    label = "Calls",
                    value = stats.totalCalls.toString(),
                    showDivider = false,
                    modifier = Modifier.weight(1f),
                )
                StatCell(
                    label = "Avg call",
                    value = formatDuration(stats.averageDurationSeconds),
                    modifier = Modifier.weight(1f),
                )
                StatCell(
                    label = "Messages",
                    value = stats.messages.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCell(
                    label = "This week",
                    value = stats.interactionsThisWeek.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(NexusSpacing.x3))
            val lastSeen = stats.lastInteractionMillis?.let { relativeTime(it) }
            Text(
                text = if (lastSeen != null) {
                    String.format(Locale.US, "Last interaction %s", lastSeen)
                } else {
                    "No interactions yet"
                },
                style = NexusTheme.type.meta,
                color = NexusTheme.colors.textTertiary,
            )
            if (stats.missedCalls > 0) {
                Spacer(Modifier.height(NexusSpacing.x2))
                Text(
                    text = String.format(Locale.US, "%d missed", stats.missedCalls),
                    style = NexusTheme.type.meta,
                    color = NexusTheme.colors.danger,
                )
            }
        }

        Spacer(Modifier.height(NexusSpacing.x8))

        // ---- Communication DNA ---------------------------------------------
        NexusSectionLabel(
            text = "Communication DNA",
            trailing = {
                androidx.compose.material3.Text(
                    text = "9 events",
                    style = NexusTheme.type.micro,
                    color = NexusTheme.colors.textTertiary,
                )
            },
        )
        Spacer(Modifier.height(NexusSpacing.x2))
        Column(
            modifier = Modifier.semantics {
                contentDescription = timelineLegendDescription()
            },
        ) {
            if (state.dna.isEmpty()) {
                Spacer(Modifier.height(NexusSpacing.x6))
                Text(
                    text = "NO HISTORY YET",
                    style = NexusTheme.type.label,
                    color = NexusTheme.colors.textTertiary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            state.dna.forEachIndexed { index, entry ->
                val slot = when (index) {
                    0 -> if (state.dna.size == 1) TimelineSlot.Single else TimelineSlot.First
                    state.dna.lastIndex -> TimelineSlot.Last
                    else -> TimelineSlot.Middle
                }
                DnaRow(entry = entry, slot = slot)
            }
        }

        Spacer(
            modifier = Modifier.height(
                NexusSpacing.dockClearance + NexusSpacing.x4,
            ),
        )
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val lineColor = NexusTheme.colors.border
    Column(
        modifier = modifier.then(
            if (showDivider) {
                Modifier.drawBehind {
                    val x = 0f
                    drawLine(
                        color = lineColor,
                        start = Offset(x, 8f * density),
                        end = Offset(x, size.height - 8f * density),
                        strokeWidth = 1f * density,
                    )
                }
            } else {
                Modifier
            },
        ).padding(end = NexusSpacing.x2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = NexusTheme.type.statValue,
            color = NexusTheme.colors.textPrimary,
        )
        Spacer(Modifier.height(NexusSpacing.x1))
        Text(
            text = label.uppercase(),
            style = NexusTheme.type.micro,
            color = NexusTheme.colors.textTertiary,
            maxLines = 1,
        )
    }
}

@Composable
private fun DnaRow(
    entry: DnaEntry,
    slot: TimelineSlot,
) {
    val colors = NexusTheme.colors
    val marker = when (entry.kind) {
        DnaKind.Incoming -> TimelineMarker.Incoming
        DnaKind.Outgoing -> TimelineMarker.Outgoing
        DnaKind.Missed -> TimelineMarker.Missed
        DnaKind.Message -> TimelineMarker.Message
    }
    val headline = when (entry.kind) {
        DnaKind.Incoming -> "Incoming call"
        DnaKind.Outgoing -> "Outgoing call"
        DnaKind.Missed -> "Missed call"
        DnaKind.Message -> if (entry.outgoing) "Message sent" else "Message received"
    }
    val detail = buildString {
        append(relativeTime(entry.timestampMillis))
        if (entry.durationSeconds > 0) {
            append("  ·  ")
            append(formatDuration(entry.durationSeconds.toLong()))
        }
    }

    NexusTimelineRow(slot = slot, marker = marker) {
        Column(Modifier.weight(1f)) {
            Text(
                text = headline,
                style = NexusTheme.type.subhead,
                color = colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = detail,
                style = NexusTheme.type.meta,
                color = if (entry.kind == DnaKind.Missed) colors.danger else colors.textTertiary,
                maxLines = 1,
            )
        }
    }
}
