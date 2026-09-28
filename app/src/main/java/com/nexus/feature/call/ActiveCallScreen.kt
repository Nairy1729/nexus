package com.nexus.feature.call

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.core.animation.LocalReducedMotion
import com.nexus.core.design.GlassTier
import com.nexus.core.design.LiquidGlassOrb
import com.nexus.core.design.NexusActionButton
import com.nexus.core.design.NexusActionStyle
import com.nexus.core.design.NexusCallControl
import com.nexus.core.design.NexusDialKey
import com.nexus.core.design.NexusGlassSurface
import com.nexus.core.design.NexusIconButton
import kotlinx.coroutines.launch
import com.nexus.core.design.NexusTextAction
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.spatial.model.SpatialContactMapper
import com.nexus.core.theme.NexusRadii
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.formatDuration
import com.nexus.telephony.CallPhase
import com.nexus.telephony.CallSession
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sin

/** Key definitions for DTMF in-call tones. */
private data class DtmfKey(val digit: String, val letters: String?)
private val InCallPad = listOf(
    DtmfKey("1", null), DtmfKey("2", "ABC"), DtmfKey("3", "DEF"),
    DtmfKey("4", "GHI"), DtmfKey("5", "JKL"), DtmfKey("6", "MNO"),
    DtmfKey("7", "PQRS"), DtmfKey("8", "TUV"), DtmfKey("9", "WXYZ"),
    DtmfKey("*", null), DtmfKey("0", "+"), DtmfKey("#", null),
)

/**
 * ACTIVE CALL — Spatial Glass OS "Shared Space".
 *
 * Visual hierarchy:
 * 1. Status Eyebrow: SHARED SPACE // SPATIAL LINK ACTIVE
 * 2. Shared Space Bridge: Two connected translucent liquid glass orbs (YOU + CALLER)
 *    linked by an active fluid energy stream with pulsating harmonics.
 * 3. Hero Identity & Timer: Soft white typography with high contrast clarity.
 * 4. Translucent Floating Glass Controls Tray: Precision glass controls with pill reaction.
 * 5. End Call: Frosted danger glass circle with restrained red specular tint.
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
    val reduced = LocalReducedMotion.current

    var muted by remember { mutableStateOf(false) }
    var speaker by remember { mutableStateOf(false) }
    var video by remember { mutableStateOf(false) }
    var bluetooth by remember { mutableStateOf(false) }
    var holding by remember { mutableStateOf(false) }
    var showKeypad by remember { mutableStateOf(false) }
    var dtmfDigits by remember { mutableStateOf("") }

    val elapsed by elapsedSeconds.collectAsStateWithLifecycle()
    val connecting = session.phase != CallPhase.Active

    // Contextual contact glass tint
    val contactTint = remember(session.contactId) {
        SpatialContactMapper.contactTintColor(session.contactId)
    }

    // Fluid energy link animation
    val transition = rememberInfiniteTransition(label = "energyLink")
    val linkPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "linkPhase",
    )
    val beadPos by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "beadPos",
    )

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var isEnding by remember { androidx.compose.runtime.mutableStateOf(false) }

    val callerOrbState = when {
        isEnding -> com.nexus.core.design.orb.NexusOrbState.Shaping
        connecting -> com.nexus.core.design.orb.NexusOrbState.Connecting
        else -> com.nexus.core.design.orb.NexusOrbState.Responding
    }
    val callerSeed = remember(session.contactId, session.number) {
        (session.contactId?.hashCode() ?: session.number.hashCode()).toLong()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            .padding(horizontal = NexusSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(NexusSpacing.x8))

        // Status Eyebrow
        Text(
            text = when {
                isEnding -> "SESSION CLOSING // DISPERSING"
                connecting -> "SIGNAL LINKING // ACQUIRING"
                else -> "SHARED SPACE // SPATIAL LINK ACTIVE"
            },
            style = NexusTheme.type.micro,
            color = if (connecting) colors.textTertiary else colors.accentText,
            letterSpacing = 1.5.sp,
        )

        Spacer(Modifier.height(NexusSpacing.x4))

        if (!showKeypad) {
            // ---- SHARED SPACE: YOU (Core) linked to CALLER (Orb) ------------
            NexusGlassSurface(
                tier = GlassTier.Floating,
                shape = RoundedCornerShape(NexusRadii.card),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = NexusSpacing.x4, vertical = NexusSpacing.x4),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // YOU Core Orb
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LiquidGlassOrb(
                            name = "YOU",
                            size = 64.dp,
                            tint = Color(0xFFE8EEFF),
                            pulse = false,
                            orbState = com.nexus.core.design.orb.NexusOrbState.Idle,
                            seed = 101L,
                        )
                        Spacer(Modifier.height(NexusSpacing.x1))
                        Text(
                            text = "YOU",
                            style = NexusTheme.type.micro,
                            color = colors.accent,
                            letterSpacing = 1.sp,
                        )
                    }

                    // Shared Fluid Energy Link
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .padding(horizontal = NexusSpacing.x2),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val midY = size.height / 2f
                            val width = size.width

                            // Ambient glow stream
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        colors.accent.copy(alpha = 0.20f),
                                        contactTint.copy(alpha = 0.20f),
                                    ),
                                ),
                                start = Offset(0f, midY),
                                end = Offset(width, midY),
                                strokeWidth = 8.dp.toPx(),
                                cap = StrokeCap.Round,
                            )

                            // Fluid harmonic wave
                            val points = 32
                            var lastX = 0f
                            var lastY = midY
                            for (i in 0..points) {
                                val x = (i.toFloat() / points) * width
                                val wave = if (!reduced) sin(i * 0.45f + linkPhase) * 4.dp.toPx() else 0f
                                val currentY = midY + wave
                                if (i > 0) {
                                    drawLine(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                colors.accent.copy(alpha = 0.70f),
                                                contactTint.copy(alpha = 0.70f),
                                            ),
                                        ),
                                        start = Offset(lastX, lastY),
                                        end = Offset(x, currentY),
                                        strokeWidth = 2.dp.toPx(),
                                        cap = StrokeCap.Round,
                                    )
                                }
                                lastX = x
                                lastY = currentY
                            }

                            // Pulsating energy beads flowing across stream
                            if (!reduced) {
                                val beadX = width * beadPos
                                val beadY = midY + sin(beadPos * 8f + linkPhase) * 4.dp.toPx()
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.45f),
                                    radius = 5.dp.toPx(),
                                    center = Offset(beadX, beadY),
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5.dp.toPx(),
                                    center = Offset(beadX, beadY),
                                )
                            }
                        }
                    }

                    // Caller Liquid Orb
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LiquidGlassOrb(
                            name = session.displayName,
                            size = 64.dp,
                            tint = contactTint,
                            pulse = !reduced,
                            orbState = callerOrbState,
                            seed = callerSeed,
                        )
                        Spacer(Modifier.height(NexusSpacing.x1))
                        Text(
                            text = session.displayName.take(8).uppercase(),
                            style = NexusTheme.type.micro,
                            color = colors.textPrimary,
                            maxLines = 1,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }
        } else {
            LiquidGlassOrb(
                name = session.displayName,
                size = 72.dp,
                tint = contactTint,
                pulse = false,
            )
        }

        Spacer(Modifier.height(NexusSpacing.x5))

        // Hero Identity Text
        Text(
            text = session.displayName.uppercase(),
            style = if (showKeypad) NexusTheme.type.title else NexusTheme.type.hero,
            color = colors.textPrimary,
        )

        Spacer(Modifier.height(NexusSpacing.x2))

        // Duration or Frequency status
        Text(
            text = if (connecting) {
                session.number
            } else {
                formatDuration(elapsed)
            },
            style = if (connecting) {
                NexusTheme.type.subhead
            } else {
                if (showKeypad) NexusTheme.type.title else NexusTheme.type.numberDisplay
            },
            color = if (connecting) colors.textSecondary else colors.textPrimary,
        )

        if (session.phase == CallPhase.Connecting) {
            Spacer(Modifier.height(NexusSpacing.x2))
            Text(
                text = "CARRIER FREQUENCY ACQUIRED",
                style = NexusTheme.type.meta,
                color = colors.textTertiary,
                letterSpacing = 1.sp,
            )
        }

        if (onOpenProfile != null && !showKeypad) {
            Spacer(Modifier.height(NexusSpacing.x2))
            NexusTextAction(
                label = "Profile",
                accent = false,
                onClick = onOpenProfile,
            )
        }

        Spacer(Modifier.weight(1f))

        if (showKeypad) {
            // ---- In-call DTMF Keypad ----------------------------------------
            NexusGlassSurface(
                tier = GlassTier.Primary,
                shape = RoundedCornerShape(NexusRadii.card),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(NexusSpacing.x4),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NexusSpacing.x2),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (dtmfDigits.isNotEmpty()) dtmfDigits else "Touch tones",
                            style = NexusTheme.type.title,
                            color = if (dtmfDigits.isNotEmpty()) colors.textPrimary else colors.textTertiary,
                        )
                        Row {
                            if (dtmfDigits.isNotEmpty()) {
                                NexusIconButton(
                                    icon = Icons.Rounded.Backspace,
                                    contentDescription = "Delete digit",
                                    onClick = {
                                        haptics.tick()
                                        dtmfDigits = dtmfDigits.dropLast(1)
                                    },
                                    size = 36.dp,
                                )
                            }
                            NexusIconButton(
                                icon = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Hide keypad",
                                onClick = {
                                    haptics.select()
                                    showKeypad = false
                                },
                                size = 36.dp,
                            )
                        }
                    }

                    Spacer(Modifier.height(NexusSpacing.x3))

                    InCallPad.chunked(3).forEach { rowKeys ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            rowKeys.forEach { key ->
                                NexusDialKey(
                                    digit = key.digit,
                                    letters = key.letters,
                                    size = 52.dp,
                                    onClick = {
                                        haptics.tick()
                                        dtmfDigits += key.digit
                                    },
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ---- Translucent Glass Call Controls Panel ----------------------
            NexusGlassSurface(
                tier = GlassTier.Primary,
                shape = RoundedCornerShape(NexusRadii.card),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = NexusSpacing.x6, horizontal = NexusSpacing.x2),
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
                            onClick = {
                                haptics.select()
                                showKeypad = true
                            },
                        )
                        NexusCallControl(
                            icon = if (speaker) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                            label = if (speaker) "Speaker on" else "Speaker",
                            selected = speaker,
                            onClick = {
                                speaker = !speaker
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
                            icon = Icons.Rounded.Videocam,
                            label = if (video) "Stop video" else "Video",
                            selected = video,
                            onClick = {
                                video = !video
                                haptics.select()
                            },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(NexusSpacing.x8))

        // ---- End Call: strictly centered, frosted glass danger circle --------
        NexusActionButton(
            icon = Icons.Rounded.CallEnd,
            contentDescription = "End call",
            onClick = {
                haptics.confirm()
                if (!reduced) {
                    isEnding = true
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(420)
                        onEnd()
                    }
                } else {
                    onEnd()
                }
            },
            size = 64.dp,
            iconSize = NexusSizes.iconLg + 2.dp,
            style = NexusActionStyle.Danger,
        )

        Spacer(
            modifier = Modifier.height(
                NexusSpacing.dockClearance - NexusSpacing.x4,
            ),
        )
    }
}
