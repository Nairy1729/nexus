package com.nexus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nexus.core.ui.nexusClickable
import com.nexus.core.theme.NexusRadii
import com.nexus.core.theme.NexusSizes
import com.nexus.core.theme.NexusSpacing
import com.nexus.core.theme.NexusTheme

/**
 * Search input on a glass pill. One field, no chrome — the field IS the surface.
 */
@Composable
fun NexusSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Search,
) {
    val colors = NexusTheme.colors
    val shape = RoundedCornerShape(NexusRadii.pill)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(NexusGlass.fill())
            .border(1.dp, NexusGlass.borderBrush(), shape)
            .padding(horizontal = NexusSpacing.x4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.padding(end = NexusSpacing.x3),
        )
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = NexusTheme.type.subhead,
                    color = colors.textTertiary,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = NexusTheme.type.subhead.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(NexusRadii.pill))
                    .background(colors.surfacePressed, RoundedCornerShape(NexusRadii.pill))
                    .padding(horizontal = NexusSpacing.x3, vertical = NexusSpacing.x1)
                    .nexusClickable(clickHaptics = true) { onValueChange("") },
            ) {
                Text(
                    text = "CLEAR",
                    style = NexusTheme.type.micro,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

/** Large dialer entry display — reserved height so the layout never jumps. */
@Composable
fun NexusNumberDisplay(
    number: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = NexusTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(NexusSizes.avatarXl + NexusSpacing.x2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            Text(
                text = if (number.isEmpty()) " " else number,
                style = NexusTheme.type.numberDisplay,
                color = colors.textPrimary,
            )
        }
        if (trailing != null) trailing()
    }
}
