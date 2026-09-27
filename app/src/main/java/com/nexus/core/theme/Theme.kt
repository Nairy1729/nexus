package com.nexus.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme

val LocalNexusColors = staticCompositionLocalOf { NexusPalette.Dark }
val LocalNexusType = staticCompositionLocalOf { NexusType.Scale }

/**
 * NEXUS theme shell.
 *
 * Usage: `NexusTheme.colors.textPrimary`, `NexusTheme.type.headline`.
 * [mode] is resolved by the app shell so an in-app theme switch can override the system.
 */
object NexusTheme {
    val colors: NexusColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNexusColors.current

    val type: NexusTypeScale
        @Composable
        @ReadOnlyComposable
        get() = LocalNexusType.current
}

@Composable
fun NexusTheme(
    mode: NexusThemeMode? = null,
    content: @Composable () -> Unit,
) {
    val resolved = mode ?: if (isSystemInDarkTheme()) NexusThemeMode.Dark else NexusThemeMode.Light
    val colors = when (resolved) {
        NexusThemeMode.Dark -> NexusPalette.Dark
        NexusThemeMode.Obsidian -> NexusPalette.Obsidian
        NexusThemeMode.Light -> NexusPalette.Light
    }
    CompositionLocalProvider(
        LocalNexusColors provides colors,
        LocalNexusType provides NexusType.Scale,
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = NexusType.Material,
            content = content,
        )
    }
}
