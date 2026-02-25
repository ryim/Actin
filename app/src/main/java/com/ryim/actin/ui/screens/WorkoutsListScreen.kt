package com.ryim.actin.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.ryim.actin.R
import com.ryim.actin.domain.workouts.Workout
import com.ryim.actin.ui.ReusableComposables.AppBottomBar
import com.ryim.actin.ui.ReusableComposables.AppTopBar
import com.ryim.actin.ui.ReusableComposables.RoundRectButton
import com.ryim.actin.ui.ReusableComposables.SectionHeader
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
    navController: NavHostController,
    sharedWorkoutViewModel: SharedWorkoutViewModel,
    onRunWorkout: (Workout) -> Unit,
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
            AppTopBar(
                title = "Workouts"
            )
        },

        bottomBar = {
            AppBottomBar(
                selectedItem = 2,
                onHome = onHome,
                onProgress = onProgress,
                onExercise = onExercise,
                onSettings = onSettings
            )
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

            RoundRectButton(
                onClick = {
                    sharedWorkoutViewModel.clearSelectedWorkout()
                    onNewWorkout()
                },
                text = "New workout"
            )

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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
//                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column (
                    modifier = Modifier.weight(1f)
                ) {
                    SectionHeader(
                        title = workout.name,
                        gapAbove = false
                    )
                }

                // Start button on the right
                RoundRectButton(
                    onClick = { onRunWorkout(workout) },
                    text = "Start"
                )

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
