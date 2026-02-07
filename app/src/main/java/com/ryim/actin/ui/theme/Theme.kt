package com.ryim.actin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ryim.actin.ui.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColDark,
    secondary = SecondaryColDark,
    tertiary = TertiaryColDark,
    onPrimary = Color(0xFFffffff),
    onSecondary = Color(0xFF000000),
    outlineVariant = SecondaryColDark,
    onSurface = Color(0xFFffffff),
    surfaceContainer = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColLight,
    secondary = SecondaryColLight,
    tertiary = TertiaryColLight,
    onPrimary = Color(0xFFffffff),
    onSecondary = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    surfaceContainer = Color(0xFFffffff),
)

//@Composable
//fun MyTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    content: @Composable () -> Unit
//) {
//    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}

@Composable
fun MyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    // Read the system setting
    val systemDark = isSystemInDarkTheme()

    // Decide which theme to use
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
