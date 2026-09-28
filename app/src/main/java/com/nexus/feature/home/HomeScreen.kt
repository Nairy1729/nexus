package com.nexus.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexus.core.animation.LocalReducedMotion
import com.nexus.core.design.AvatarRing
import com.nexus.core.design.GlassTier
import com.nexus.core.design.NexusActionButton
import com.nexus.core.design.NexusActionStyle
import com.nexus.core.design.NexusAvatar
import com.nexus.core.design.NexusContactNode
import com.nexus.core.design.NexusGlassSurface
import com.nexus.core.design.NexusSectionLabel
import com.nexus.core.design.NexusTextAction
import com.nexus.core.design.NexusTimelineRow
import com.nexus.core.design.TimelineMarker
import com.nexus.core.design.TimelineSlot
import com.nexus.core.di.AppContainer
import com.nexus.core.theme.NexusRadii
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.formatClock
import com.nexus.core.ui.greeting
import com.nexus.core.ui.relativeTime
import com.nexus.data.model.ResolvedCall
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * UNIVERSE — the personal communication operating environment.
 *
 * Designed according to SPATIAL GLASS OS principles:
 * - Content is hero
 * - Layered glass material with depth, edge specular highlights, and refraction
 * - Deep graphite spatial atmosphere
 * - Calm, continuous opening experience (600ms settling)
 * - Immediate physical responsiveness
 */
@Composable
fun HomeScreen(
    onOpenContact: (String) -> Unit,
    onOpenActivity: () -> Unit,
    onOpenPeople: () -> Unit,
    onOpenDial: () -> Unit,
    onCallBack: (ResolvedCall) -> Unit,
    onSimulateIncoming: () -> Unit,
    container: AppContainer,
    modifier: Modifier = Modifier,
    onToggleTheme: () -> Unit = {},
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
    val priority by viewModel.priority.collectAsStateWithLifecycle()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle()
    val missedCount by viewModel.missedCount.collectAsStateWithLifecycle()
    val clock = rememberMinuteClock()
    val colors = NexusTheme.colors
    val reducedMotion = LocalReducedMotion.current

    // Short, elegant opening transition (~600ms)
    val entranceAnim = remember { Animatable(if (reducedMotion) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            entranceAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(600, easing = EaseOutCubic),
            )
        }
    }

    // Gentle ambient atmospheric breathing
    val infiniteTransition = rememberInfiniteTransition(label = "ambientAtmosphere")
    val ambientBreath by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathValue",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.backgroundGradientStart,
                        colors.background,
                        colors.backgroundGradientEnd,
                    ),
                ),
            ),
    ) {
        // Subtle distant light source at top-center (Atmosphere)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val auraRadius = size.width * 0.95f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accentSoft.copy(alpha = 0.16f * ambientBreath),
                        colors.accentSoft.copy(alpha = 0.05f * ambientBreath),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.5f, 0f),
                    radius = auraRadius,
                ),
                radius = auraRadius,
                center = Offset(size.width * 0.5f, 0f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NexusSpacing.gutter)
                .graphicsLayer {
                    alpha = entranceAnim.value
                    translationY = (1f - entranceAnim.value) * 18.dp.toPx()
                },
        ) {
            Spacer(Modifier.height(NexusSpacing.x3))

            // ---- Context: Primary Glass Surface ------------------------------
            NexusGlassSurface(
                tier = GlassTier.Primary,
                shape = RoundedCornerShape(NexusRadii.xl),
                contentPadding = PaddingValues(horizontal = NexusSpacing.x5, vertical = NexusSpacing.x4),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "UNIVERSE",
                                style = NexusTheme.type.label,
                                color = colors.accentText,
                            )
                            Text(
                                text = " // ${greeting().uppercase(Locale.US)}",
                                style = NexusTheme.type.label,
                                color = colors.textTertiary,
                            )
                        }
                        Spacer(Modifier.height(NexusSpacing.x1))
                        Text(
                            text = clock,
                            style = NexusTheme.type.timeHero,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(NexusSpacing.x1))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%d interactions today",
                                    todayCount,
                                ),
                                style = NexusTheme.type.meta,
                                color = colors.textSecondary,
                            )
                            if (missedCount > 0) {
                                Text(
                                    text = "  ·  ",
                                    style = NexusTheme.type.meta,
                                    color = colors.textTertiary,
                                )
                                Text(
                                    text = String.format(Locale.US, "%d missed", missedCount),
                                    style = NexusTheme.type.meta,
                                    color = colors.danger,
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.x2),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NexusActionButton(
                            icon = Icons.Rounded.Palette,
                            contentDescription = "Switch theme skin",
                            onClick = onToggleTheme,
                            size = NexusSizes.touchMin,
                            iconSize = NexusSizes.iconMd,
                            style = NexusActionStyle.Glass,
                        )
                        NexusActionButton(
                            icon = Icons.Rounded.PhoneInTalk,
                            contentDescription = "Simulate an incoming call (prototype)",
                            onClick = onSimulateIncoming,
                            size = NexusSizes.touchMin,
                            iconSize = NexusSizes.iconMd,
                            style = NexusActionStyle.Glass,
                        )
                    }
                }
            }

            Spacer(Modifier.height(NexusSpacing.x6))

            // ---- Priority People: Floating Glass Surface ----------------------
            NexusSectionLabel(
                text = "Priority",
                trailing = {
                    NexusTextAction(label = "Orbit", accent = false, onClick = onOpenPeople)
                },
            )
            Spacer(Modifier.height(NexusSpacing.x2))
            if (priority.isNotEmpty()) {
                NexusGlassSurface(
                    tier = GlassTier.Floating,
                    shape = RoundedCornerShape(NexusRadii.xl),
                    contentPadding = PaddingValues(horizontal = NexusSpacing.x4, vertical = NexusSpacing.x4),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ConnectionRow(
                        contacts = priority.take(4),
                        onOpen = onOpenContact,
                    )
                }
            }

            Spacer(Modifier.height(NexusSpacing.x6))

            // ---- Recent Events: Primary Glass Surface ------------------------
            NexusSectionLabel(
                text = "Events",
                trailing = {
                    NexusTextAction(label = "All activity", onClick = onOpenActivity)
                },
            )
            Spacer(Modifier.height(NexusSpacing.x2))
            val shown = recent.take(4)
            if (shown.isNotEmpty()) {
                NexusGlassSurface(
                    tier = GlassTier.Primary,
                    shape = RoundedCornerShape(NexusRadii.xl),
                    contentPadding = PaddingValues(horizontal = NexusSpacing.x4, vertical = NexusSpacing.x3),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        shown.forEachIndexed { index, call ->
                            val slot = when (index) {
                                0 -> TimelineSlot.First
                                shown.lastIndex -> TimelineSlot.Last
                                else -> TimelineSlot.Middle
                            }
                            RecentRow(
                                call = call,
                                slot = slot,
                                onOpen = { call.contactId?.let(onOpenContact) },
                                onCallBack = { onCallBack(call) },
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(
                    NexusSpacing.dockClearance + NexusSpacing.x5,
                ),
            )
        }

        // Floating Dial Trigger: Precision liquid glass action button
        NexusActionButton(
            icon = Icons.Rounded.Call,
            contentDescription = "Open dialer",
            onClick = onOpenDial,
            size = NexusSizes.actionMd + 4.dp,
            style = NexusActionStyle.Accent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = NexusSpacing.gutter, bottom = NexusSpacing.dockClearance + NexusSpacing.x3),
        )
    }
}

@Composable
private fun ConnectionRow(
    contacts: List<com.nexus.data.model.Contact>,
    onOpen: (String) -> Unit,
) {
    val colors = NexusTheme.colors
    val reduced = LocalReducedMotion.current
    val centers = remember { androidx.compose.runtime.mutableStateListOf<Offset>() }
    val sweep = rememberInfiniteTransition(label = "connectionSweep")
    val sweepProgress = sweep.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = com.nexus.core.animation.NexusMotion.standard),
            repeatMode = RepeatMode.Restart,
        ),
        label = "connectionSweepValue",
    ).value

    Box(Modifier.fillMaxWidth()) {
        Canvas(Modifier.matchParentSize()) {
            if (centers.size >= 2) {
                // The network line: your people, connected
                val path = Path().apply {
                    moveTo(centers.first().x, centers.first().y)
                    centers.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path,
                    color = colors.orbitLine,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.25f * density),
                )

                if (!reduced) {
                    // Subtle light pulse traveling the filament
                    val lengths = centers.zipWithNext { a, b -> (b - a).getDistance() }
                    val total = lengths.sum().takeIf { it > 0f } ?: return@Canvas
                    val target = sweepProgress * total
                    var accumulated = 0f
                    var point: Offset? = null
                    lengths.forEachIndexed { i, len ->
                        if (point == null && accumulated + len >= target) {
                            val t = (target - accumulated) / len
                            point = Offset(
                                x = centers[i].x + (centers[i + 1].x - centers[i].x) * t,
                                y = centers[i].y + (centers[i + 1].y - centers[i].y) * t,
                            )
                        }
                        accumulated += len
                    }
                    point?.let {
                        drawCircle(color = colors.accent.copy(alpha = 0.20f), radius = 12f * density, center = it)
                        drawCircle(color = colors.accent, radius = 3.0f * density, center = it)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            contacts.forEachIndexed { index, contact ->
                val avatarSize =
                    if (index == 0) NexusSizes.avatarLg else NexusSizes.avatarMd + 2.dp
                val density = androidx.compose.ui.platform.LocalDensity.current
                NexusContactNode(
                    contact = contact,
                    onClick = { onOpen(contact.id) },
                    avatarSize = avatarSize,
                    ring = if (contact.isFavorite) AvatarRing.Favorite else AvatarRing.None,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        while (centers.size <= index) centers.add(Offset.Zero)
                        centers[index] = coordinates.positionInParent() +
                            Offset(
                                coordinates.size.width / 2f,
                                with(density) { avatarSize.toPx() } / 2f,
                            )
                    },
                )
            }
        }
    }
}

@Composable
private fun RecentRow(
    call: ResolvedCall,
    slot: TimelineSlot,
    onOpen: () -> Unit,
    onCallBack: () -> Unit,
) {
    val colors = NexusTheme.colors
    val marker = when (call.type) {
        com.nexus.data.model.CallType.Incoming -> TimelineMarker.Incoming
        com.nexus.data.model.CallType.Outgoing -> TimelineMarker.Outgoing
        com.nexus.data.model.CallType.Missed -> TimelineMarker.Missed
    }
    val typeLabel = when (call.type) {
        com.nexus.data.model.CallType.Incoming -> "Incoming"
        com.nexus.data.model.CallType.Outgoing -> "Outgoing"
        com.nexus.data.model.CallType.Missed -> "Missed"
    }

    NexusTimelineRow(
        slot = slot,
        marker = marker,
        onClick = onOpen,
    ) {
        NexusAvatar(
            name = call.displayName,
            size = NexusSizes.avatarXs,
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
                text = formatDurationSafe(call.durationSeconds),
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

private fun formatDurationSafe(seconds: Int): String =
    com.nexus.core.ui.formatDuration(seconds.toLong())

@Composable
private fun rememberMinuteClock(): String {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            val msToNextMinute = 60_000L - (now % 60_000L)
            delay(msToNextMinute + 10)
        }
    }
    return formatClock(now)
}
