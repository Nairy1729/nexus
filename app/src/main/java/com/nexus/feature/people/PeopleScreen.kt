package com.nexus.feature.people

import android.os.SystemClock
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexus.core.animation.LocalReducedMotion
import com.nexus.core.animation.NexusMotion
import com.nexus.core.design.AvatarRing
import com.nexus.core.design.NexusActionButton
import com.nexus.core.design.NexusActionStyle
import com.nexus.core.design.NexusAvatar
import com.nexus.core.design.NexusHeader
import com.nexus.core.design.NexusSearchField
import com.nexus.core.design.NexusSegmentedToggle
import com.nexus.core.di.AppContainer
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.ui.nexusClickable
import com.nexus.data.model.Contact
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * PEOPLE — the orbital network.
 *
 * ORBIT: contacts placed on three concentric rings; proximity and node size are earned by
 * interaction weight, never assigned. The gesture grammar is threefold — tap opens the
 * profile, drag a node into the center well to call it, long-press calls from where it
 * sits. LIST is the conventional fallback: search, alphabetical rows, one tap to call.
 */
@Composable
fun PeopleScreen(
    onOpenContact: (String) -> Unit,
    onCallBack: (Contact) -> Unit,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val viewModel: PeopleViewModel = viewModel(factory = PeopleViewModel.factory(container))
    val view by viewModel.view.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val weighted by viewModel.weighted.collectAsStateWithLifecycle()
    val listed by viewModel.listed.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            .padding(horizontal = NexusSpacing.gutter),
    ) {
        NexusHeader(
            title = "People",
            trailing = {
                NexusSegmentedToggle(
                    options = listOf("Orbit", "List"),
                    selectedIndex = if (view == PeopleView.Orbit) 0 else 1,
                    onSelect = { index ->
                        viewModel.setView(if (index == 0) PeopleView.Orbit else PeopleView.List)
                    },
                    modifier = Modifier.width(168.dp),
                )
            },
        )

        Spacer(Modifier.height(NexusSpacing.x4))

        when (view) {
            PeopleView.Orbit -> OrbitView(
                contacts = weighted,
                onOpenContact = onOpenContact,
                onCallBack = onCallBack,
                modifier = Modifier.weight(1f),
            )

            PeopleView.List -> {
                NexusSearchField(
                    value = query,
                    onValueChange = viewModel::setQuery,
                    placeholder = "Search people or numbers",
                )
                Spacer(Modifier.height(NexusSpacing.x3))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = NexusSpacing.dockClearance),
                    verticalArrangement = Arrangement.spacedBy(NexusSpacing.x1),
                ) {
                    items(listed, key = { it.id }) { contact ->
                        PersonRow(
                            contact = contact,
                            onOpen = { onOpenContact(contact.id) },
                            onCallBack = { onCallBack(contact) },
                        )
                    }
                    if (listed.isEmpty()) {
                        item { NoMatches() }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ORBIT
// ---------------------------------------------------------------------------

/** Where one node sits. [x]/[y]/[radius] are canvas pixels relative to the center. */
private class OrbitSlot(
    val contact: Contact,
    val x: Float,
    val y: Float,
    val radius: Float,
    val ring: Int,
    val size: Float,
)

/**
 * Pure geometry: three rings, tiers by weight rank. The inner ring holds the three
 * closest people, the middle the next three, the outer everyone else — the map from
 * data to space lives here so the composable only draws what it is told.
 */
private fun orbitSlots(
    contacts: List<Contact>,
    widthPx: Float,
    heightPx: Float,
    sizeOfRank: (Int) -> Float,
): List<OrbitSlot> {
    if (contacts.isEmpty()) return emptyList()
    val cx = widthPx / 2f
    val cy = heightPx / 2f
    // Outer ring keeps the largest node and its label inside every viewport.
    val outer = min(widthPx, heightPx) / 2f - sizeOfRank(contacts.lastIndex) * 0.9f
    val rings = floatArrayOf(outer * 0.42f, outer * 0.70f, outer * 0.96f)

    val inner = min(3, contacts.size)
    val middle = min(6, contacts.size) - inner
    val outerCount = contacts.size - inner - middle
    val counts = intArrayOf(inner, middle, outerCount)

    val slots = ArrayList<OrbitSlot>(contacts.size)
    var rank = 0
    for (ring in 0..2) {
        val count = counts[ring]
        if (count <= 0) continue
        // Each ring starts at a different angle so spokes never line up visually.
        val startAngle = -PI / 2.0 + ring * (PI / 6.0)
        for (i in 0 until count) {
            val angle = startAngle + 2.0 * PI * i / count
            slots.add(
                OrbitSlot(
                    contact = contacts[rank],
                    x = cx + (rings[ring] * cos(angle)).toFloat(),
                    y = cy + (rings[ring] * sin(angle)).toFloat(),
                    radius = rings[ring],
                    ring = ring,
                    size = sizeOfRank(rank),
                ),
            )
            rank++
        }
    }
    return slots
}

@Composable
private fun OrbitView(
    contacts: List<Contact>,
    onOpenContact: (String) -> Unit,
    onCallBack: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NexusTheme.colors
    val haptics = LocalNexusHaptics.current
    val reduced = LocalReducedMotion.current
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val cx = widthPx / 2f
        val cy = heightPx / 2f

        val sizeOfRank: (Int) -> Float = { rank ->
            with(density) {
                when {
                    rank == 0 -> NexusSizes.avatarLg.toPx()
                    rank < 3 -> (NexusSizes.avatarMd + 2.dp).toPx()
                    rank < 6 -> NexusSizes.avatarMd.toPx()
                    else -> NexusSizes.avatarSm.toPx()
                }
            }
        }
        val slots = remember(contacts, widthPx, heightPx) {
            orbitSlots(contacts, widthPx, heightPx, sizeOfRank)
        }
        val wellPx = with(density) { 38.dp.toPx() }
        val armPx = wellPx * 1.35f

        // Direct manipulation: raw offset tracks the finger, the shown offset springs
        // behind it (snappy, never floaty) and returns home on release.
        val rawDrags = remember { mutableStateMapOf<String, Offset>() }
        var armed by remember { mutableStateOf<String?>(null) }

        fun updateDrag(slot: OrbitSlot, delta: Offset) {
            val next = (rawDrags[slot.contact.id] ?: Offset.Zero) + delta
            rawDrags[slot.contact.id] = next
            val node = Offset(slot.x + next.x, slot.y + next.y)
            val nowArmed =
                if ((node - Offset(cx, cy)).getDistance() <= armPx) slot.contact.id else null
            if (nowArmed != armed) {
                haptics.tick()
                armed = nowArmed
            }
        }

        fun releaseDrag(slot: OrbitSlot, fireCall: Boolean) {
            val wasArmed = armed == slot.contact.id
            rawDrags.remove(slot.contact.id)
            armed = null
            if (fireCall && wasArmed) {
                haptics.confirm()
                onCallBack(slot.contact)
            }
        }

        // Ambient: the well breathes, signalling it is open. Parked under reduced motion.
        val breath by rememberInfiniteTransition(label = "wellBreath").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600, easing = NexusMotion.emphasized),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "wellBreathValue",
        )
        val wellAlpha = if (reduced) 0.09f else 0.06f + breath * 0.07f

        // ---- canvas: rings, spokes, the well --------------------------------
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 1.dp.toPx()
            listOf(0, 1, 2).forEach { ring ->
                val radius = slots.firstOrNull { it.ring == ring }?.radius
                if (radius != null && radius > 0f) {
                    drawCircle(
                        color = colors.orbitLine,
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = stroke),
                    )
                }
            }

            // Spokes: every person connected to you — the dragged one follows its node.
            slots.forEach { slot ->
                val drag = rawDrags[slot.contact.id] ?: Offset.Zero
                drawLine(
                    color = colors.orbitLine,
                    start = Offset(cx, cy),
                    end = Offset(slot.x + drag.x, slot.y + drag.y),
                    strokeWidth = stroke,
                )
            }

            // The well — you, at the center of the network.
            val armedHere = armed != null
            drawCircle(
                color = colors.accent.copy(alpha = wellAlpha),
                radius = wellPx,
                center = Offset(cx, cy),
            )
            drawCircle(
                color = if (armedHere) colors.accent else colors.borderStrong,
                radius = wellPx,
                center = Offset(cx, cy),
                style = Stroke(width = if (armedHere) 2.dp.toPx() else stroke),
            )
            if (armedHere) {
                drawCircle(
                    color = colors.accent.copy(alpha = 0.16f),
                    radius = armPx,
                    center = Offset(cx, cy),
                    style = Stroke(width = stroke),
                )
            }
        }

        Text(
            text = "YOU",
            style = NexusTheme.type.micro,
            color = if (armed != null) colors.accentText else colors.textTertiary,
            modifier = Modifier.align(Alignment.Center),
        )

        // ---- nodes ----------------------------------------------------------
        slots.forEachIndexed { index, slot ->
            OrbitNode(
                slot = slot,
                index = index,
                centerX = cx,
                centerY = cy,
                dragTarget = { rawDrags[slot.contact.id] ?: Offset.Zero },
                armed = armed == slot.contact.id,
                onTap = { onOpenContact(slot.contact.id) },
                onCallBack = { onCallBack(slot.contact) },
                onDragUpdate = { delta -> updateDrag(slot, delta) },
                onDragRelease = { releaseDrag(slot, fireCall = true) },
                onDragCancel = { releaseDrag(slot, fireCall = false) },
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun OrbitNode(
    slot: OrbitSlot,
    index: Int,
    centerX: Float,
    centerY: Float,
    dragTarget: () -> Offset,
    armed: Boolean,
    onTap: () -> Unit,
    onCallBack: () -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragRelease: () -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NexusTheme.colors
    val reduced = LocalReducedMotion.current
    val density = LocalDensity.current

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val entryAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(
            durationMillis = NexusMotion.relaxed,
            delayMillis = index * NexusMotion.listStagger,
            easing = NexusMotion.decelerate,
        ),
        label = "nodeEntryAlpha",
    )
    val entryScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.72f,
        animationSpec = tween(
            durationMillis = NexusMotion.relaxed,
            delayMillis = index * NexusMotion.listStagger,
            easing = NexusMotion.emphasized,
        ),
        label = "nodeEntryScale",
    )
    val armScale by animateFloatAsState(
        targetValue = if (armed) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 520f),
        label = "nodeArmScale",
    )
    // Read here (not in the parent) so a drag invalidates only the node being dragged.
    val drag by animateOffsetAsState(
        targetValue = dragTarget(),
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 620f),
        label = "nodeDrag",
    )

    val avatarSize = with(density) { slot.size.toDp() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .offset {
                IntOffset(
                    x = (slot.x + drag.x - centerX).roundToInt(),
                    y = (slot.y + drag.y - centerY).roundToInt(),
                )
            }
            .graphicsLayer {
                alpha = if (reduced) 1f else entryAlpha
                scaleX = entryScale * armScale
                scaleY = entryScale * armScale
            }
            .semantics {
                role = Role.Button
                contentDescription = "${slot.contact.name}, ${slot.contact.number}"
                onClick(label = "Open profile") {
                    onTap()
                    true
                }
                customActions = listOf(
                    CustomAccessibilityAction("Call ${slot.contact.name}") {
                        onCallBack()
                        true
                    },
                )
            }
            .orbitGestures(
                key = slot.contact.id,
                onTap = onTap,
                onCallBack = onCallBack,
                onDragUpdate = onDragUpdate,
                onDragRelease = onDragRelease,
                onDragCancel = onDragCancel,
            ),
    ) {
        NexusAvatar(
            name = slot.contact.name,
            size = avatarSize,
            ring = if (slot.contact.isFavorite) AvatarRing.Favorite else AvatarRing.None,
        )
        Spacer(Modifier.height(NexusSpacing.x1))
        Text(
            text = slot.contact.name.split(" ").first(),
            style = if (slot.ring == 2) NexusTheme.type.micro else NexusTheme.type.meta,
            color = if (armed) colors.accentText else colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = avatarSize + 28.dp),
        )
    }
}

/**
 * One gesture surface, three intents: tap opens, long-press calls, drag-to-well calls.
 * Hand-rolled rather than composed from detectors so a single touch can never fire two
 * intents — the slop crossing promotes the gesture to a drag, everything else stays a tap.
 */
private fun Modifier.orbitGestures(
    key: Any,
    onTap: () -> Unit,
    onCallBack: () -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragRelease: () -> Unit,
    onDragCancel: () -> Unit,
): Modifier = pointerInput(key) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val downPos = down.position
        val downTime = down.uptimeMillis
        var dragging = false
        var longFired = false
        var settled = false
        var last = downPos
        try {
            while (true) {
                val remaining = if (!dragging && !longFired) {
                    (viewConfiguration.longPressTimeoutMillis -
                        (SystemClock.uptimeMillis() - downTime)).coerceAtLeast(1L)
                } else {
                    0L
                }
                val event = if (remaining > 0) {
                    withTimeoutOrNull(remaining) { awaitPointerEvent() }
                } else {
                    awaitPointerEvent()
                }

                if (event == null) {
                    // Held still long enough — the deliberate "call from here" gesture.
                    if (!longFired) {
                        longFired = true
                        onCallBack()
                    }
                    continue
                }

                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) {
                    if (!dragging && !longFired) onTap()
                    settled = true
                    break
                }

                val pos = change.position
                if (!dragging && !longFired &&
                    (pos - downPos).getDistance() > viewConfiguration.touchSlop
                ) {
                    dragging = true
                }
                if (dragging) {
                    val delta = pos - last
                    if (delta != Offset.Zero) onDragUpdate(delta)
                    change.consume()
                }
                last = pos
            }
            if (dragging) {
                settled = true
                onDragRelease()
            }
        } finally {
            // Composition left mid-gesture (navigation covering the screen): clean the
            // node home WITHOUT firing — an interrupted drag must never place a call.
            if (!settled) onDragCancel()
        }
    }
}

// ---------------------------------------------------------------------------
// LIST
// ---------------------------------------------------------------------------

@Composable
private fun PersonRow(
    contact: Contact,
    onOpen: () -> Unit,
    onCallBack: () -> Unit,
) {
    val colors = NexusTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = NexusSpacing.x2)
            .semantics {
                role = Role.Button
                contentDescription = "${contact.name}, ${contact.number}"
            }
            .nexusClickable(clickHaptics = true, onClick = onOpen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NexusAvatar(
            name = contact.name,
            size = NexusSizes.avatarSm,
            ring = if (contact.isFavorite) AvatarRing.Favorite else AvatarRing.None,
            modifier = Modifier.padding(end = NexusSpacing.x4),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = NexusTheme.type.subhead,
                color = colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = "${contact.number}  ·  ${contact.subtitle}",
                style = NexusTheme.type.meta,
                color = colors.textTertiary,
                maxLines = 1,
            )
        }
        NexusActionButton(
            icon = Icons.Rounded.Call,
            contentDescription = "Call ${contact.name}",
            onClick = onCallBack,
            size = 44.dp,
            iconSize = NexusSizes.iconSm,
            style = NexusActionStyle.Glass,
        )
    }
}

@Composable
private fun NoMatches() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = NexusSpacing.x16),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "NO MATCHES",
            style = NexusTheme.type.label,
            color = NexusTheme.colors.textTertiary,
        )
    }
}
