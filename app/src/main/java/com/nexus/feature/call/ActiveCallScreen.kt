package com.nexus.feature.call

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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.core.design.NexusActionButton
import com.nexus.core.design.NexusActionStyle
import com.nexus.core.design.NexusAvatar
import com.nexus.core.design.NexusCallControl
import com.nexus.core.design.NexusTextAction
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.formatDuration
import com.nexus.telephony.CallPhase
import com.nexus.telephony.CallSession
import kotlinx.coroutines.flow.StateFlow

/**
 * ACTIVE — the call, held steady.
 *
 * Name and timer own the top half; controls sit in a thumb-reachable grid where every
 * toggle announces four signals (fill, border, icon color, spoken state). Ending the
 * call is the only red thing on the screen.
 */
@Composable
fun ActiveCallScreen(
    session: CallSession,
    elapsedSeconds: StateFlow<Long>,
    onEnd: () -> Unit,
    onOpenProfile: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = NexusTheme.colors
    val haptics = LocalNexusHaptics.current

    var muted by remember { mutableStateOf(false) }
    var speaker by remember { mutableStateOf(false) }
    var video by remember { mutableStateOf(false) }
    var bluetooth by remember { mutableStateOf(false) }
    var holding by remember { mutableStateOf(false) }

    val elapsed by elapsedSeconds.collectAsStateWithLifecycle()
    val connecting = session.phase != CallPhase.Active

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            .padding(horizontal = NexusSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(NexusSpacing.x10))

        Text(
            text = if (connecting) "CONNECTING" else "CALL IN PROGRESS",
            style = NexusTheme.type.label,
            color = if (connecting) colors.textTertiary else colors.accentText,
        )

        Spacer(Modifier.height(NexusSpacing.x8))

        NexusAvatar(
            name = session.displayName,
            size = NexusSizes.avatarXl + 16.dp,
            pulse = !com.nexus.core.animation.reducedMotion(),
        )

        Spacer(Modifier.height(NexusSpacing.x6))

        Text(
            text = session.displayName.uppercase(),
            style = NexusTheme.type.hero,
            color = colors.textPrimary,
        )

        Spacer(Modifier.height(NexusSpacing.x3))

        Text(
            text = if (connecting) {
                session.number
            } else {
                formatDuration(elapsed)
            },
            style = if (connecting) {
                NexusTheme.type.subhead
            } else {
                NexusTheme.type.numberDisplay
            },
            color = if (connecting) colors.textSecondary else colors.textPrimary,
        )

        if (session.phase == CallPhase.Connecting) {
            Spacer(Modifier.height(NexusSpacing.x2))
            Text(
                text = session.number,
                style = NexusTheme.type.meta,
                color = colors.textTertiary,
            )
        }

        Spacer(Modifier.weight(1f))

        // ---- Control grid ---------------------------------------------------
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                NexusCallControl(
                    icon = if (muted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                    label = if (muted) "Unmute" else "Mute",
                    selected = muted,
                    onClick = {
                        muted = !muted
                        haptics.select()
                    },
                )
                NexusCallControl(
                    icon = Icons.Rounded.Dialpad,
                    label = "Keypad",
                    selected = false,
                    onClick = { haptics.select() },
                )
                NexusCallControl(
                    icon = Icons.Rounded.Videocam,
                    label = if (video) "Stop video" else "Video",
                    selected = video,
                    onClick = {
                        video = !video
                        haptics.select()
                    },
                )
            }

            Spacer(Modifier.height(NexusSpacing.x6))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                NexusCallControl(
                    icon = Icons.Rounded.BluetoothAudio,
                    label = if (bluetooth) "BT on" else "Bluetooth",
                    selected = bluetooth,
                    onClick = {
                        bluetooth = !bluetooth
                        haptics.select()
                    },
                )
                NexusCallControl(
                    icon = Icons.Rounded.Backup,
                    label = if (holding) "Resume" else "Hold",
                    selected = holding,
                    onClick = {
                        holding = !holding
                        haptics.select()
                    },
                )
                NexusCallControl(
                    icon = Icons.Rounded.People,
                    label = "Add call",
                    selected = false,
                    onClick = { haptics.select() },
                )
            }
        }

        Spacer(Modifier.height(NexusSpacing.x8))

        // ---- End + profile ---------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onOpenProfile != null) {
                NexusTextAction(label = "Profile", onClick = onOpenProfile)
                Spacer(Modifier.padding(horizontal = NexusSpacing.x4))
            }
            NexusActionButton(
                icon = Icons.Rounded.CallEnd,
                contentDescription = "End call",
                onClick = {
                    haptics.confirm()
                    onEnd()
                },
                size = NexusSizes.actionLg,
                iconSize = NexusSizes.iconLg + 2.dp,
                style = NexusActionStyle.Danger,
            )
        }

        Spacer(
            modifier = Modifier.height(
                NexusSpacing.dockClearance - NexusSpacing.x6,
            ),
        )
    }
}
