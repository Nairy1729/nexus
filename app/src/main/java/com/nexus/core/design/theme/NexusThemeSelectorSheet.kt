package com.nexus.core.design.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexus.core.design.GlassTier
import com.nexus.core.design.NexusGlassSurface
import com.nexus.core.design.orb.NexusOrb
import com.nexus.core.design.orb.NexusOrbState
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.theme.NexusPalette
import com.nexus.core.theme.NexusRadii
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.core.theme.NexusThemeMode

/**
 * Interactive Spatial Identity switcher sheet.
 *
 * Displays live, interactive previews of all 3 NEXUS visual identities:
 * 1. OBSIDIAN AURORA (Default)
 * 2. GRAPHITE LIME
 * 3. MIDNIGHT BURGUNDY
 *
 * Features miniature live Thinking Orbs, material glass swatches, and instantaneous selection.
 */
@Composable
fun NexusThemeSelectorSheet(
    visible: Boolean,
    currentMode: NexusThemeMode,
    onSelectMode: (NexusThemeMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NexusTheme.colors
    val haptics = LocalNexusHaptics.current

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(200)),
    ) {
        // Scrim overlay
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        haptics.tick()
                        onDismiss()
                    },
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // Floating glass modal card
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(350),
                ) + fadeIn(tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(250),
                ) + fadeOut(tween(200)),
            ) {
                NexusGlassSurface(
                    tier = GlassTier.Floating,
                    shape = RoundedCornerShape(
                        topStart = NexusRadii.xl,
                        topEnd = NexusRadii.xl,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { /* Consume clicks inside sheet */ },
                        ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                            .padding(
                                horizontal = NexusSpacing.x5,
                                vertical = NexusSpacing.x5,
                            ),
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(colors.accent, CircleShape),
                                    )
                                    Spacer(Modifier.width(NexusSpacing.x2))
                                    Text(
                                        text = "SPATIAL IDENTITY",
                                        style = NexusTheme.type.micro,
                                        color = colors.accentText,
                                    )
                                }
                                Spacer(Modifier.height(NexusSpacing.x1))
                                Text(
                                    text = "Visual Operating Engine",
                                    style = NexusTheme.type.title,
                                    color = colors.textPrimary,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(NexusSizes.touchMin)
                                    .clip(CircleShape)
                                    .clickable {
                                        haptics.tick()
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close identity selector",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(NexusSizes.iconMd),
                                )
                            }
                        }

                        Spacer(Modifier.height(NexusSpacing.x4))

                        // Three Theme Cards
                        Column(
                            verticalArrangement = Arrangement.spacedBy(NexusSpacing.x3),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            ThemeCard(
                                mode = NexusThemeMode.ObsidianAurora,
                                isSelected = currentMode == NexusThemeMode.ObsidianAurora,
                                onClick = {
                                    haptics.confirm()
                                    onSelectMode(NexusThemeMode.ObsidianAurora)
                                },
                            )

                            ThemeCard(
                                mode = NexusThemeMode.GraphiteLime,
                                isSelected = currentMode == NexusThemeMode.GraphiteLime,
                                onClick = {
                                    haptics.confirm()
                                    onSelectMode(NexusThemeMode.GraphiteLime)
                                },
                            )

                            ThemeCard(
                                mode = NexusThemeMode.MidnightBurgundy,
                                isSelected = currentMode == NexusThemeMode.MidnightBurgundy,
                                onClick = {
                                    haptics.confirm()
                                    onSelectMode(NexusThemeMode.MidnightBurgundy)
                                },
                            )
                        }

                        Spacer(Modifier.height(NexusSpacing.x3))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    mode: NexusThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentThemeColors = NexusTheme.colors
    val modeColors = when (mode) {
        NexusThemeMode.ObsidianAurora -> NexusPalette.ObsidianAurora
        NexusThemeMode.GraphiteLime -> NexusPalette.GraphiteLime
        NexusThemeMode.MidnightBurgundy -> NexusPalette.MidnightBurgundy
    }

    val cardBorderBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(
                modeColors.accent,
                modeColors.glassBorderTop,
                modeColors.accent,
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                currentThemeColors.border,
                currentThemeColors.border.copy(alpha = 0.05f),
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NexusRadii.lg))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        modeColors.backgroundGradientStart.copy(alpha = 0.85f),
                        modeColors.surface.copy(alpha = 0.70f),
                        modeColors.backgroundGradientEnd.copy(alpha = 0.90f),
                    )
                )
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                brush = cardBorderBrush,
                shape = RoundedCornerShape(NexusRadii.lg),
            )
            .clickable(onClick = onClick)
            .padding(all = NexusSpacing.x3),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                // Live miniature Thinking Orb preview
                NexusOrb(
                    state = NexusOrbState.Idle,
                    size = 36.dp,
                    accent = modeColors.orbAccent,
                    intensity = 1.0f,
                    showAtmosphere = true,
                    showCoreGlass = true,
                    modifier = Modifier.padding(end = NexusSpacing.x3),
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mode.title,
                            style = NexusTheme.type.subhead,
                            color = if (isSelected) modeColors.accentText else currentThemeColors.textPrimary,
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(NexusSpacing.x2))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(NexusRadii.pill))
                                    .background(modeColors.accentSoft)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = NexusTheme.type.micro,
                                    color = modeColors.accentText,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = mode.subtitle,
                        style = NexusTheme.type.meta,
                        color = currentThemeColors.textSecondary,
                    )
                    Spacer(Modifier.height(NexusSpacing.x1))

                    // Swatches
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ColorSwatch(modeColors.background)
                        ColorSwatch(modeColors.surfaceRaised)
                        ColorSwatch(modeColors.accent)
                        ColorSwatch(modeColors.accentSecondary)
                    }
                }
            }

            // Selection indicator radio/check
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) modeColors.accent else currentThemeColors.surfacePressed
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) modeColors.accent else currentThemeColors.border,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = modeColors.accentContent,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
            .border(0.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
    )
}
