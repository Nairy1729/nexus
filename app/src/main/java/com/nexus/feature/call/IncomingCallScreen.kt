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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nexus.core.animation.LocalReducedMotion
import com.nexus.core.animation.NexusMotion
import com.nexus.core.design.AvatarRing
import com.nexus.core.design.NexusActionButton
import com.nexus.core.design.NexusActionStyle
import com.nexus.core.design.NexusAvatar
import com.nexus.core.design.NexusSwipeToAnswer
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.relativeTime
import com.nexus.telephony.CallSession
import kotlinx.coroutines.flow.flowOf

/**
 * INCOMING — the one cinematic screen NEXUS allows itself.
 *
 * The caller's monogram grows into the full frame as a soft radial wash (no blur, no
 * photo — the person IS the type), the name sits at hero scale, and the two possible
 * futures are physically distinct: drag the thumb to answer, tap red to decline.
 */
@Composable
fun IncomingCallScreen(
    session: CallSession,
    container: com.nexus.core.di.AppContainer,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NexusTheme.colors
    val haptics = LocalNexusHaptics.current
    val reduced = LocalReducedMotion.current

    // Resolve the caller's relationship details without coupling the screen to a VM —
    // a single contact lookup, live for as long as the ring lasts.
    val contact by remember(session.contactId) {
        if (session.contactId != null) {
            container.contactRepository.observeContact(session.contactId)
        } else {
            kotlinx.coroutines.flow.flowOf(null)
        }
    }.collectAsStateWithLifecycle(initialValue = null)

    // Breathing wash — the call is alive before anyone touches the screen.
    val breath by rememberInfiniteTransition(label = "incomingBreath").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = NexusMotion.emphasized),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "incomingBreathValue",
    )
    val washAlpha = if (reduced) 0.30f else 0.22f + breath * 0.14f

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top)),
    ) {
        // Radial wash of the monogram color, bled across the whole frame.
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.maxDimension * (0.55f + washAlpha * 0.4f)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.surfaceRaised.copy(alpha = 0.9f),
                        colors.background,
                    ),
                ),
            )
            drawCircle(
                color = colors.accent.copy(alpha = washAlpha * 0.16f),
                radius = radius,
                center = Offset(size.width / 2f, size.height * 0.34f),
            )
            drawCircle(
                color = colors.accent.copy(alpha = washAlpha * 0.10f),
                radius = radius * 0.62f,
                center = Offset(size.width / 2f, size.height * 0.34f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = NexusSpacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(NexusSpacing.x16))

            Text(
                text = "INCOMING SIGNAL // ENTERING ORBIT",
                style = NexusTheme.type.micro,
                color = colors.accentText,
            )

            Spacer(Modifier.height(NexusSpacing.x10))

            NexusAvatar(
                name = session.displayName,
                size = NexusSizes.avatarXl + 24.dp,
                ring = if (contact?.isFavorite == true) AvatarRing.Favorite else AvatarRing.None,
                pulse = !reduced,
                contentDescription = null,
            )

            Spacer(Modifier.height(NexusSpacing.x8))

            Text(
                text = session.displayName.uppercase(),
                style = NexusTheme.type.hero,
                color = colors.textPrimary,
                modifier = Modifier.graphicsLayer {
                    alpha = if (reduced) 1f else 0.85f + breath * 0.15f
                },
            )

            Spacer(Modifier.height(NexusSpacing.x3))

            Text(
                text = session.number,
                style = NexusTheme.type.subhead,
                color = colors.textSecondary,
            )

            val lastSeen = contact?.lastInteractionMillis
            if (lastSeen != null) {
                Spacer(Modifier.height(NexusSpacing.x2))
                Text(
                    text = "Last interaction ${relativeTime(lastSeen)}",
                    style = NexusTheme.type.meta,
                    color = colors.textTertiary,
                )
            }

            Spacer(Modifier.weight(1f))

            NexusSwipeToAnswer(onAnswer = onAnswer)

            Spacer(Modifier.height(NexusSpacing.x8))

            // ---- Actions: Decline and quick Message -------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NexusActionButton(
                        icon = Icons.Rounded.CallEnd,
                        contentDescription = "Decline call",
                        onClick = {
                            haptics.confirm()
                            onDecline()
                        },
                        size = NexusSizes.actionMd,
                        iconSize = NexusSizes.iconMd + 2.dp,
                        style = NexusActionStyle.Danger,
                    )
                    Spacer(Modifier.height(NexusSpacing.x2))
                    Text(
                        text = "DECLINE",
                        style = NexusTheme.type.label,
                        color = colors.danger,
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NexusActionButton(
                        icon = Icons.Rounded.Message,
                        contentDescription = "Quick message reply",
                        onClick = {
                            haptics.select()
                            onDecline()
                        },
                        size = NexusSizes.actionMd,
                        iconSize = NexusSizes.iconMd + 2.dp,
                        style = NexusActionStyle.Glass,
                    )
                    Spacer(Modifier.height(NexusSpacing.x2))
                    Text(
                        text = "MESSAGE",
                        style = NexusTheme.type.label,
                        color = colors.textSecondary,
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(
                    NexusSpacing.dockClearance - NexusSpacing.x4,
                ),
            )
        }
    }
}
