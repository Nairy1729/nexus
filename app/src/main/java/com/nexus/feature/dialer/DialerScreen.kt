package com.nexus.feature.dialer

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexus.core.animation.LocalReducedMotion
import com.nexus.core.animation.NexusMotion
import com.nexus.core.design.NexusActionButton
import com.nexus.core.design.NexusActionStyle
import com.nexus.core.design.NexusAvatar
import com.nexus.core.design.NexusDialKey
import com.nexus.core.design.NexusNumberDisplay
import com.nexus.core.di.AppContainer
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.data.model.Contact

/** One physical key: digit + the letters burned into the key by the last century. */
private data class KeyDef(val digit: String, val letters: String?)

private val Keypad = listOf(
    KeyDef("1", null),
    KeyDef("2", "ABC"),
    KeyDef("3", "DEF"),
    KeyDef("4", "GHI"),
    KeyDef("5", "JKL"),
    KeyDef("6", "MNO"),
    KeyDef("7", "PQRS"),
    KeyDef("8", "TUV"),
    KeyDef("9", "WXYZ"),
    KeyDef("*", null),
    KeyDef("0", "+"),
    KeyDef("#", null),
)

/**
 * DIAL — the conventional keypad, dressed for 2026.
 *
 * The layout is deliberately the one everyone already knows: muscle memory beats novelty,
 * and the futurism lives in the geometry (perfect circles, hairline edges), the type, and
 * the entry motion — keys are born outward from the center of the grid on screen entry,
 * the "black hole" idea kept as choreography instead of as a layout.
 */
@Composable
fun DialerScreen(
    onCallBack: (digits: String, match: Contact?) -> Unit,
    onOpenContact: (String) -> Unit,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val viewModel: DialerViewModel = viewModel(factory = DialerViewModel.factory(container))
    val digits by viewModel.digits.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            .padding(horizontal = NexusSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(NexusSpacing.x4))

        val colors = NexusTheme.colors

        // Signal Acquisition Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NexusSpacing.x2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (digits.isEmpty()) "SIGNAL // STANDBY" else "SIGNAL // ACQUIRING",
                style = NexusTheme.type.micro,
                color = if (digits.isEmpty()) colors.textTertiary else colors.accent,
            )
            Text(
                text = if (digits.isEmpty()) "CARRIER READY" else "${digits.length} TONES EMITTED",
                style = NexusTheme.type.micro,
                color = colors.textTertiary,
            )
        }

        Spacer(Modifier.height(NexusSpacing.x2))

        // ---- Display with Radial Signal Wavefront ---------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Signal wavefront expanding behind the number
            if (digits.isNotEmpty()) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val count = minOf(3, digits.length)
                    for (i in 1..count) {
                        val r = (32f + i * 30f)
                        drawCircle(
                            color = colors.accent.copy(alpha = (0.24f / i)),
                            radius = r,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }
                }
            }

            NexusNumberDisplay(
                number = digits,
                trailing = {
                    if (digits.isNotEmpty()) {
                        NexusActionButton(
                            icon = Icons.Rounded.Backspace,
                            contentDescription = "Delete last digit",
                            onClick = viewModel::backspace,
                            size = NexusSizes.touchMin,
                            iconSize = NexusSizes.iconMd,
                            style = NexusActionStyle.Glass,
                        )
                    }
                },
            )
        }

        // ---- Dynamic matching / Target Acquired -----------------------------
        Box(modifier = Modifier.height(NexusSizes.avatarSm + NexusSpacing.x3)) {
            val match = matches.firstOrNull()
            if (digits.isNotEmpty() && match != null) {
                com.nexus.core.design.NexusGlassSurface(
                    tier = com.nexus.core.design.GlassTier.Floating,
                    tint = colors.accent,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(com.nexus.core.theme.NexusRadii.xl),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NexusSpacing.x3, vertical = NexusSpacing.x2),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NexusAvatar(
                            name = match.name,
                            size = NexusSizes.avatarSm,
                            ring = com.nexus.core.design.AvatarRing.Favorite,
                            modifier = Modifier.padding(end = NexusSpacing.x3),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "SIGNAL LOCKED",
                                style = NexusTheme.type.micro,
                                color = colors.accent,
                            )
                            Text(
                                text = match.name,
                                style = NexusTheme.type.subhead,
                                color = colors.textPrimary,
                                maxLines = 1,
                            )
                            Text(
                                text = match.number,
                                style = NexusTheme.type.meta,
                                color = colors.textTertiary,
                                maxLines = 1,
                            )
                        }
                        NexusActionButton(
                            icon = Icons.Rounded.Call,
                            contentDescription = "Call ${match.name}",
                            onClick = { onCallBack(digits, match) },
                            size = 44.dp,
                            iconSize = NexusSizes.iconSm,
                            style = NexusActionStyle.Accent,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(NexusSpacing.x2))

        // ---- Keypad ---------------------------------------------------------
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Keypad.chunked(3).forEach { rowKeys ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = NexusSpacing.x3),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    rowKeys.forEach { key ->
                        // Radial birth: delay grows with distance from the grid's
                        // heart, so the pad blooms outward exactly once per entry.
                        val index = Keypad.indexOf(key)
                        val row = index / 3
                        val col = index % 3
                        val radialDistance = kotlin.math.hypot(
                            (col - 1).toFloat(),
                            (row - 1.5f),
                        )
                        BirthOnEnter(
                            delayMillis = (radialDistance * NexusMotion.radialStagger).toInt(),
                        ) {
                            NexusDialKey(
                                digit = key.digit,
                                letters = key.letters,
                                contentDescription = if (key.letters != null) {
                                    "${key.digit}, ${key.letters}"
                                } else {
                                    key.digit
                                },
                                onClick = { viewModel.append(key.digit[0]) },
                                onLongClick = if (key.digit == "0") {
                                    { viewModel.append('+') }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(NexusSpacing.x4))

        // ---- Call -----------------------------------------------------------
        BirthOnEnter(delayMillis = NexusMotion.radialStagger * 3) {
            if (digits.isEmpty()) {
                // The accent belongs to the action; while there is nothing to call,
                // the button rests as outline — the state reads before you touch it.
                NexusActionButton(
                    icon = Icons.Rounded.Call,
                    contentDescription = "Call. Enter a number first.",
                    onClick = { /* Nothing to call yet — the button rests. */ },
                    size = NexusSizes.actionLg,
                    iconSize = NexusSizes.iconLg + 2.dp,
                    style = NexusActionStyle.Outline,
                )
            } else {
                NexusActionButton(
                    icon = Icons.Rounded.Call,
                    contentDescription = "Call $digits",
                    onClick = {
                        onCallBack(digits, matches.firstOrNull())
                    },
                    size = NexusSizes.actionLg,
                    iconSize = NexusSizes.iconLg + 2.dp,
                    style = NexusActionStyle.Accent,
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

/**
 * Entry choreography: content blooms from scale-down with an out-stretch, once per
 * screen entry. Under reduced motion it simply appears — content is never gated
 * behind animation for accessibility.
 */
@Composable
private fun BirthOnEnter(
    delayMillis: Int,
    content: @Composable () -> Unit,
) {
    val reduced = LocalReducedMotion.current
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val progress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(
            durationMillis = NexusMotion.relaxed,
            delayMillis = if (reduced) 0 else delayMillis,
            easing = NexusMotion.emphasized,
        ),
        label = "birth",
    )
    if (reduced) {
        content()
    } else {
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = progress
                scaleX = 0.86f + 0.14f * progress
                scaleY = 0.86f + 0.14f * progress
            },
        ) {
            content()
        }
    }
}
