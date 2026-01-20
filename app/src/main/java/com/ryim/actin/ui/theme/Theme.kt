package com.ryim.actin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColDark,
    secondary = SecondaryColDark,
    tertiary = TertiaryColDark,
    onPrimary = Color(0xFFffffff),
    onSecondary = Color(0xFF000000),
    outlineVariant = SecondaryColDark,
    onSurface = Color(0xFFffffff),
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColLight,
    secondary = SecondaryColLight,
    tertiary = TertiaryColLight,
    onPrimary = Color(0xFFffffff),
    onSecondary = Color(0xFF000000),
    onSurface = Color(0xFF000000),
)

@Composable
fun MyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
