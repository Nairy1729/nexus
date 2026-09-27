package com.nexus.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme
import com.nexus.data.model.Contact
import com.nexus.core.ui.nexusCombinedClickable

/**
 * Avatar + name node — the atom of HOME's priority row and the orbit.
 * Recency dot comes from the data (interacted within the last hour), not decoration.
 */
@Composable
fun NexusContactNode(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: Dp = NexusSizesDefault.avatar,
    ring: AvatarRing = if (contact.isFavorite) AvatarRing.Favorite else AvatarRing.None,
    showSubline: String? = null,
    onLongClick: (() -> Unit)? = null,
    sharedAvatarModifier: Modifier = Modifier,
) {
    val recent = contact.lastInteractionMillis
        ?.let { System.currentTimeMillis() - it < 60 * 60_000L } == true

    Column(
        modifier = modifier.nexusCombinedClickable(
            clickHaptics = true,
            onLongClick = onLongClick,
            onClick = onClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        NexusAvatar(
            name = contact.name,
            size = avatarSize,
            ring = ring,
            recent = recent,
            modifier = sharedAvatarModifier,
        )
        Spacer(Modifier.height(NexusSpacing.x2))
        Text(
            text = contact.name,
            style = NexusTheme.type.meta,
            color = NexusTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(avatarSize + 34.dp),
        )
        if (showSubline != null) {
            Text(
                text = showSubline,
                style = NexusTheme.type.micro,
                color = NexusTheme.colors.textTertiary,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private object NexusSizesDefault {
    val avatar = com.nexus.core.theme.NexusSizes.avatarMd
}
