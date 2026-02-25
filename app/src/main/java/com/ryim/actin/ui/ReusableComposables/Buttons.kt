package com.ryim.actin.ui.ReusableComposables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun RoundRectButton(
    text: String,
    mode: ButtonMode = ButtonMode.Yellow,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val (background, content) = when (mode) {
        ButtonMode.Yellow -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        ButtonMode.Blue   -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }

    Box {
        Button(
            onClick = { onClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = background,
                contentColor = content
            ),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
            ) {
                Text(text)
            }
        }
    }
}

enum class ButtonMode {
    Yellow,
    Blue
}

@Composable
fun StandardIconButton(
    colour: Color = MaterialTheme.colorScheme.secondary,
    icon: ImageVector,
    contentDescription: String = "",
    onClick: () -> Unit,
) {


    Box(
        modifier = Modifier
            .size(28.dp)   // this now controls the border diameter
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(36.dp),
            tint = colour
        )
    }
}