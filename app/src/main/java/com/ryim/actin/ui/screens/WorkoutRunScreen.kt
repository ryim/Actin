package com.ryim.actin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryim.actin.domain.ExerciseEntry
import com.ryim.actin.domain.workouts.Workout
import java.time.LocalDate
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ryim.actin.ui.ExAddPrefill
import com.ryim.actin.ui.ReusableComposables.AppBottomBar
import com.ryim.actin.ui.ReusableComposables.AppTopBar
import com.ryim.actin.ui.ReusableComposables.SectionHeader
import com.ryim.actin.ui.SharedExAddViewModel
import com.ryim.actin.ui.SharedWorkoutViewModel
import com.ryim.actin.ui.WorkoutRunScreenViewModel
import java.util.UUID

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
            AppTopBar(
                title = workout?.name ?: "Unnamed workout"
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
            val selectedWorkout by sharedWorkoutViewModel.selectedWorkout.collectAsState()

            selectedWorkout?.let { workout ->
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
                    },
                    modifier = Modifier.padding(16.dp)
                )
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
            workout = workoutName,
            id = UUID.randomUUID().toString()
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
            workout = workoutName,
            id = UUID.randomUUID().toString()
        )
    }

    sharedExAddViewModel.setPrefill(prefill)
    onNavigateToExAdd()
}

@Composable
fun WorkoutCard(
    workout: Workout,
    history: List<ExerciseEntry>,
    onEditExercise: (String) -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(
                title = workout.name,
                gapAbove = false
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
