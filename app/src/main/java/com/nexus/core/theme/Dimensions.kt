package com.nexus.core.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale (4pt base), radius system, and canonical component sizes.
 * Every measurement in the app comes from here — no magic numbers in features.
 */
object NexusSpacing {
    val x1: Dp = 4.dp
    val x2: Dp = 8.dp
    val x3: Dp = 12.dp
    val x4: Dp = 16.dp
    val x5: Dp = 20.dp
    val x6: Dp = 24.dp
    val x8: Dp = 32.dp
    val x10: Dp = 40.dp
    val x12: Dp = 48.dp
    val x16: Dp = 64.dp

    /** Standard horizontal screen gutter. */
    val gutter: Dp = 24.dp

    /** Space reserved so floating docks never cover content. */
    val dockClearance: Dp = 108.dp
}

object NexusRadii {
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 22.dp
    val xl: Dp = 28.dp
    val xxl: Dp = 36.dp
    val pill: Dp = 999.dp
}

/** Canonical sizes — touch targets never drop below [touchMin]. */
object NexusSizes {
    val hairline: Dp = 1.dp
    val touchMin: Dp = 48.dp

    val iconSm: Dp = 18.dp
    val iconMd: Dp = 22.dp
    val iconLg: Dp = 26.dp

    val avatarXs: Dp = 32.dp
    val avatarSm: Dp = 40.dp
    val avatarMd: Dp = 52.dp
    val avatarLg: Dp = 64.dp
    val avatarXl: Dp = 96.dp

    val actionSm: Dp = 56.dp
    val actionMd: Dp = 64.dp
    val actionLg: Dp = 76.dp

    val dialKey: Dp = 70.dp
    val dockHeight: Dp = 64.dp
    val chipHeight: Dp = 38.dp
}
