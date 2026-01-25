package com.ryim.actin.ui.ReusableComposables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun AppBottomBar(
    selectedItem: Int,
    onHome: () -> Unit,
    onProgress: () -> Unit,
    onExercise: () -> Unit,
    onSettings: () -> Unit
) {
    val navBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onSecondary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = MaterialTheme.colorScheme.secondary
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = selectedItem == 0,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = navBarItemColors
        )

        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = onProgress,
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Progress") },
            label = { Text("Progress") },
            colors = navBarItemColors
        )

        NavigationBarItem(
            selected = selectedItem == 2,
            onClick = onExercise,
            icon = { Icon(Icons.Default.EditNote, contentDescription = "Workouts") },
            label = { Text("Workouts") },
            colors = navBarItemColors
        )

        NavigationBarItem(
            selected = selectedItem == 4,
            onClick = onSettings,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = navBarItemColors
        )
    }
}
