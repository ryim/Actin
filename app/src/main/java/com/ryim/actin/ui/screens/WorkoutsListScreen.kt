package com.ryim.actin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.ryim.actin.R
import com.ryim.actin.domain.ExerciseEntry
import com.ryim.actin.domain.workouts.Workout
import com.ryim.actin.ui.ExAddPrefill
import com.ryim.actin.ui.WorkoutListScreenViewModel
import com.ryim.actin.ui.SharedWorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    onHome: () -> Unit,
    onProgress: () -> Unit,
    onExercise: () -> Unit,
    onSettings: () -> Unit,
    onNewWorkout: () -> Unit,
    onRunWorkout: (Workout) -> Unit,
    navController: NavHostController,
    sharedWorkoutViewModel: SharedWorkoutViewModel,
    viewModel: WorkoutListScreenViewModel,
    onEditWorkout: (Workout) -> Unit = { selected ->
        sharedWorkoutViewModel.selectWorkout(selected)
        onNewWorkout()
    },
    onDeleteWorkout: (Workout) -> Unit
) {
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            if (entry.destination.route == "WorkoutListScreen") {
                viewModel.refresh()
            }
        }
    }

    val scrollState = rememberScrollState()
    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 12.dp),
                    ) {
                        Text(
                            "Workouts",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.logo8),
                        contentDescription = "App icon",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(54.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },

        bottomBar = {

            var selectedItem by remember { mutableIntStateOf(2) }

            val navBarItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.secondary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {

                // Home
                NavigationBarItem(
                    selected = selectedItem == 0,
                    onClick = onHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = navBarItemColors
                )

                // Progress
                NavigationBarItem(
                    selected = selectedItem == 1,
                    onClick = onProgress,
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Home") },
                    label = { Text("Progress") },
                    colors = navBarItemColors
                )

                // Exercise configuration
                NavigationBarItem(
                    selected = selectedItem == 2,
                    onClick = onExercise,
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Placeholder 2") },
                    label = { Text("Workouts") },
                    colors = navBarItemColors
                )

                // --- Settings ---
                NavigationBarItem(
                    selected = selectedItem == 4,
                    onClick = onSettings,
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = navBarItemColors
                )
            }
        }


    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
//                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    sharedWorkoutViewModel.clearSelectedWorkout()
                    onNewWorkout()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary, // different from box
                    contentColor = Color.White
                )
            ) {
                Text(
                    "New workout",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }


            LazyColumn(
                modifier = Modifier
//                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(viewModel.workouts.asReversed(), key = { it.id }) { workout ->
                    WorkoutListCard(
                        workout = workout,
                        onRunWorkout = onRunWorkout,
                        onEditWorkout = onEditWorkout,
                        onDeleteRequest = { selected ->
                            workoutToDelete = selected
                        }
                    )
                }
            }

            //  Workout deletion dialog alert
            if (workoutToDelete != null) {
                AlertDialog(
                    onDismissRequest = { workoutToDelete = null },
                    title = { Text("Delete workout?") },
                    text = { Text("This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDeleteWorkout(workoutToDelete!!)
                                workoutToDelete = null
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { workoutToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

        }
    }
}

@Composable
fun WorkoutListCard(
    workout: Workout,
    onRunWorkout: (Workout) -> Unit,
    onEditWorkout: (Workout) -> Unit,
    onDeleteRequest: (Workout) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title on the left, allowed to wrap under the button
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)   // take all remaining space
                        .padding(end = 8.dp)  // small spacing before button
                )

                // Start button on the right
                Button(
                    onClick = { onRunWorkout(workout) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Start", color = MaterialTheme.colorScheme.onSecondary)
                }

                //  Edit and delete dropdown
                var menuExpanded by remember { mutableStateOf(false) }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {

                        // Edit
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                menuExpanded = false
                                onEditWorkout(workout)
                            }
                        )

                        // Delete
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuExpanded = false
                                onDeleteRequest(workout)
                            }
                        )
                    }
                }
            }

            // Exercises
            workout.exercises.forEach { exercise ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${exercise.name}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
