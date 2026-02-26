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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class ButtonMode {
    Yellow,
    Blue,
    Bordered
}

@Composable
fun RoundRectButton(
    text: String,
    mode: ButtonMode = ButtonMode.Yellow,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(8.dp)
    val padding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)

    when (mode) {
        ButtonMode.Yellow,
        ButtonMode.Blue -> {
            val (background, content) = when (mode) {
                ButtonMode.Yellow -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
                ButtonMode.Blue   -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                else -> error("Unexpected mode")
            }

            Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                contentPadding = padding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = background,
                    contentColor = content
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text)
                }
            }
        }

        ButtonMode.Bordered -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                contentPadding = padding,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
//                border = BorderStroke(
//                    width = 1.dp,
//                    color = MaterialTheme.colorScheme.outline
//                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text)
                }
            }
        }
    }
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