package com.ryim.actin.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onHome: () -> Unit,
    onGraph: () -> Unit,
    onExercise: () -> Unit,
    onTimer: () -> Unit,
    onSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            var menuExpanded by remember { mutableStateOf(false) }

            TopAppBar(
                title = {
                    Text(
                        "Timer",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },

        bottomBar = {

            var selectedItem by remember { mutableIntStateOf(3) }

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {

                // Home
                NavigationBarItem(
                    selected = selectedItem == 0,
                    onClick = onHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )

                // Graphs
                NavigationBarItem(
                    selected = selectedItem == 1,
                    onClick = onGraph,
                    icon = { Icon(Icons.Default.SsidChart, contentDescription = "Placeholder 2") },
                    label = { Text("Graphs") }
                )

                // Exercise configuration
                NavigationBarItem(
                    selected = selectedItem == 2,
                    onClick = onExercise,
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Placeholder 2") },
                    label = { Text("Workouts") }
                )

                // Timer settings
                NavigationBarItem(
                    selected = selectedItem == 3,
                    onClick = onTimer,
                    icon = { Icon(Icons.Default.AccessAlarm, contentDescription = "Placeholder 3") },
                    label = { Text("Timer") }
                )

                // --- Settings ---
                NavigationBarItem(
                    selected = selectedItem == 4,
                    onClick = onSettings,
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }


    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}