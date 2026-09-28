package com.nexus.feature.call

import androidx.compose.animation.core.Animatable
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
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.core.animation.LocalReducedMotion
import com.nexus.core.animation.NexusMotion
import com.nexus.core.design.GlassTier
import com.nexus.core.design.LiquidGlassOrb
import com.nexus.core.design.NexusActionButton
import com.nexus.core.design.NexusActionStyle
import com.nexus.core.design.NexusGlassSurface
import com.nexus.core.design.NexusSwipeToAnswer
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.spatial.model.SpatialContactMapper
import com.nexus.core.theme.NexusRadii
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.relativeTime
import com.nexus.telephony.CallSession
import kotlinx.coroutines.flow.flowOf

/**
 * INCOMING CALL — Spatial Glass OS Primary Wow Experience.
 *
 * Continuous material transition:
 * 1. Background darkens slightly into deep graphite atmosphere.
 * 2. Translucent glass surface forms from center.
 * 3. Caller's liquid glass orb emerges from depth with ambient contextual light glow.
 * 4. Caller identity text settles with calm typography.
 * 5. Frosted glass action controls slide into place.
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

    // Observe caller details
    val contact by remember(session.contactId) {
        if (session.contactId != null) {
            container.contactRepository.observeContact(session.contactId)
        } else {
            flowOf(null)
        }
    }.collectAsStateWithLifecycle(initialValue = null)

    // Contextual contact glass tint
    val contactTint = remember(session.contactId) {
        SpatialContactMapper.contactTintColor(session.contactId)
    }

    // Deterministic caller seed based on identity
    val callerSeed = remember(session.contactId, session.number) {
        (session.contactId?.hashCode() ?: session.number.hashCode()).toLong()
    }

    // Material entrance transition
    val entranceAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entranceAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(650, easing = NexusMotion.decelerate),
        )
    }
    val entrance = if (reduced) 1f else entranceAnim.value

    // Spatial Orb Materialization Sequence:
    // Signal Particles appear -> CONNECTING -> Orb forms -> Takes on caller accent -> RESPONDING
    val incomingOrbState by androidx.compose.runtime.produceState(
        initialValue = com.nexus.core.design.orb.NexusOrbState.Connecting,
        key1 = reduced,
    ) {
        if (reduced) {
            value = com.nexus.core.design.orb.NexusOrbState.Idle
            return@produceState
        }
        value = com.nexus.core.design.orb.NexusOrbState.Connecting
        kotlinx.coroutines.delay(1100)
        value = com.nexus.core.design.orb.NexusOrbState.Responding
    }

    // Subtle breathing pulse in atmospheric light
    val breath by rememberInfiniteTransition(label = "incomingBreath").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = NexusMotion.emphasized),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "incomingBreathValue",
    )
    val washAlpha = if (reduced) 0.28f else 0.20f + breath * 0.16f

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top)),
    ) {
        // Step 1: Deep graphite atmosphere with contextual light diffusion
        Canvas(Modifier.fillMaxSize()) {
            val centerOrbY = size.height * 0.32f
            val radius = size.maxDimension * (0.50f + washAlpha * 0.35f)

            // Deep background gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.surfaceRaised.copy(alpha = 0.95f),
                        colors.background,
                    ),
                ),
            )

            // Volumetric ambient glow centered on the emerging orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        contactTint.copy(alpha = washAlpha * 0.22f * entrance),
                        contactTint.copy(alpha = washAlpha * 0.08f * entrance),
                        androidx.compose.ui.graphics.Color.Transparent,
                    ),
                    center = Offset(size.width / 2f, centerOrbY),
                    radius = radius,
                ),
                radius = radius,
                center = Offset(size.width / 2f, centerOrbY),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = NexusSpacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(NexusSpacing.x10))

            // Step 4: Status eyebrow
            Text(
                text = "INCOMING SIGNAL // SPATIAL LINK",
                style = NexusTheme.type.micro,
                color = colors.accentText,
                letterSpacing = 1.5.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = entrance.coerceIn(0f, 1f)
                },
            )

            Spacer(Modifier.height(NexusSpacing.x6))

            // Step 2 & 3: Floating Glass Card enclosing the Caller Identity & Liquid Orb
            NexusGlassSurface(
                tier = GlassTier.Floating,
                shape = RoundedCornerShape(NexusRadii.card),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = 0.92f + 0.08f * entrance
                        scaleY = 0.92f + 0.08f * entrance
                        alpha = entrance.coerceIn(0f, 1f)
                    },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = NexusSpacing.x6, horizontal = NexusSpacing.x4),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Step 3: Emerging Hero Liquid Glass Orb
                    LiquidGlassOrb(
                        name = session.displayName,
                        size = 144.dp,
                        tint = contactTint,
                        pulse = !reduced,
                        isHero = true,
                        hasRings = contact?.isFavorite == true,
                        orbState = incomingOrbState,
                        seed = callerSeed,
                        contentDescription = "Incoming call from ${session.displayName}",
                        modifier = Modifier.graphicsLayer {
                            scaleX = 0.85f + 0.15f * entrance
                            scaleY = 0.85f + 0.15f * entrance
                        },
                    )

                    Spacer(Modifier.height(NexusSpacing.x5))

                    // Step 4: Hero Caller Identity
                    Text(
                        text = session.displayName.uppercase(),
                        style = NexusTheme.type.hero,
                        color = colors.textPrimary,
                        modifier = Modifier.graphicsLayer {
                            alpha = if (reduced) 1f else ((entrance - 0.20f) / 0.80f).coerceIn(0f, 1f)
                        },
                    )

                    Spacer(Modifier.height(NexusSpacing.x2))

                    Text(
                        text = session.number,
                        style = NexusTheme.type.subhead,
                        color = colors.textSecondary,
                        modifier = Modifier.graphicsLayer {
                            alpha = if (reduced) 1f else ((entrance - 0.30f) / 0.70f).coerceIn(0f, 1f)
                        },
                    )

                    Spacer(Modifier.height(NexusSpacing.x3))

                    // Relationship context badge
                    val contextLabel = when {
                        contact?.isFavorite == true -> "INNER ORBIT · PRIORITY CONTACT"
                        contact?.lastInteractionMillis != null ->
                            "LAST INTERACTION ${relativeTime(contact!!.lastInteractionMillis!!).uppercase()}"
                        contact?.subtitle != null -> contact!!.subtitle.uppercase()
                        else -> "DIRECT FREQUENCY"
                    }

                    NexusGlassSurface(
                        tier = GlassTier.Minimal,
                        shape = RoundedCornerShape(NexusRadii.pill),
                    ) {
                        Text(
                            text = contextLabel,
                            style = NexusTheme.type.micro,
                            color = colors.textTertiary,
                            modifier = Modifier.padding(horizontal = NexusSpacing.x3, vertical = NexusSpacing.x1),
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Step 5: Frosted Glass Action Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = if (reduced) 1f else ((entrance - 0.35f) / 0.65f).coerceIn(0f, 1f)
                        translationY = if (reduced) 0f else (24.dp.value * (1f - entrance))
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Liquid Glass Thumb Slider
                NexusSwipeToAnswer(onAnswer = onAnswer)

                Spacer(Modifier.height(NexusSpacing.x8))

                // Actions: Decline (frosted red glass) & Quick Message (frosted neutral glass)
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
                            size = 56.dp,
                            iconSize = NexusSizes.iconMd + 4.dp,
                            style = NexusActionStyle.Danger,
                        )
                        Spacer(Modifier.height(NexusSpacing.x2))
                        Text(
                            text = "DECLINE",
                            style = NexusTheme.type.label,
                            color = colors.danger,
                            letterSpacing = 1.sp,
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
                            size = 56.dp,
                            iconSize = NexusSizes.iconMd + 4.dp,
                            style = NexusActionStyle.Glass,
                        )
                        Spacer(Modifier.height(NexusSpacing.x2))
                        Text(
                            text = "MESSAGE",
                            style = NexusTheme.type.label,
                            color = colors.textSecondary,
                            letterSpacing = 1.sp,
                        )
                    }
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
