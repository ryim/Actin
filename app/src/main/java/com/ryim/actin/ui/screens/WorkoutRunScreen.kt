package com.ryim.actin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryim.actin.R
import com.ryim.actin.domain.ExerciseEntry
import com.ryim.actin.domain.workouts.Workout
import java.time.LocalDate
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ryim.actin.ui.ExAddPrefill
import com.ryim.actin.ui.ReusableComposables.AppBottomBar
import com.ryim.actin.ui.SharedExAddViewModel
import com.ryim.actin.ui.SharedWorkoutViewModel
import com.ryim.actin.ui.WorkoutRunScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutRunScreen(
    onHome: () -> Unit,
    onProgress: () -> Unit,
    onExercise: () -> Unit,
    onSettings: () -> Unit,
    sharedWorkoutViewModel: SharedWorkoutViewModel,
    sharedExAddViewModel: SharedExAddViewModel,
    viewModel: WorkoutRunScreenViewModel = hiltViewModel(),
    onNavigateToExAdd: () -> Unit
) {
    val workout = sharedWorkoutViewModel.selectedWorkout.collectAsState().value

    //  Refreshing apparatus
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 12.dp),
                    ) {
                        Text(
                            workout?.name ?: "Unnamed workout",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Spacer(modifier = Modifier.height(16.dp))


            LazyColumn(
                modifier = Modifier
//                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(viewModel.workouts, key = { it.id }) { workout ->
                    WorkoutCard(
                        workout = workout,
                        history = viewModel.uiState.collectAsState().value.allExercises,
                        onEditExercise = { exerciseName ->
                            handleExerciseEdit(
                                exerciseName = exerciseName,
                                sharedWorkoutViewModel = sharedWorkoutViewModel,
                                sharedExAddViewModel = sharedExAddViewModel,
                                history = viewModel.uiState.value.allExercises,
                                onNavigateToExAdd = onNavigateToExAdd
                            )
                        }
                    )
                }
            }
        }
    }
}

fun handleExerciseEdit(
    exerciseName: String,
    sharedWorkoutViewModel: SharedWorkoutViewModel,
    sharedExAddViewModel: SharedExAddViewModel,
    history: List<ExerciseEntry>,
    onNavigateToExAdd: () -> Unit
) {
    val workoutName = sharedWorkoutViewModel.selectedWorkout.value?.name

    val latest = history.firstOrNull { it.name.equals(exerciseName, ignoreCase = true) }

    val prefill = if (latest != null) {
        ExAddPrefill(
            name = latest.name,
            sets = latest.sets,
            reps = latest.reps,
            weights = latest.weights.map { it.toString() },
            useKg = latest.useKg,
            editMode = false,
            timestamp = latest.timestamp,
            workout = workoutName
        )
    } else {
        ExAddPrefill(
            name = exerciseName,
            sets = 3,
            reps = List(3) { 8 },
            weights = List(3) { "" },
            useKg = true,
            editMode = false,
            timestamp = null,
            workout = workoutName
        )
    }

    sharedExAddViewModel.setPrefill(prefill)
    onNavigateToExAdd()
}

@Composable
fun WorkoutCard(
    workout: Workout,
    history: List<ExerciseEntry>,
    onEditExercise: (String) -> Unit
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
            // Workout name
            Text(
                text = workout.name,
                style = MaterialTheme.typography.titleMedium
            )

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

                    val doneToday = wasDoneToday(exercise.name, history)

                    IconButton(
                        onClick = { onEditExercise(exercise.name) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (doneToday)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        if (doneToday) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed today"
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add exercise"
                            )
                        }
                    }
                }
            }

        }
    }
}

private fun wasDoneToday(
    exerciseName: String,
    history: List<ExerciseEntry>
): Boolean {
    val today = LocalDate.now()

    val latest = history.firstOrNull {
        it.name.equals(exerciseName, ignoreCase = true)
    } ?: return false

    val entryDate = LocalDate.of(latest.year, latest.month, latest.day)

    return entryDate == today
}
