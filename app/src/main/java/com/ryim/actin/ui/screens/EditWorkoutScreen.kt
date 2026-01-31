package com.ryim.actin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryim.actin.domain.workouts.WorkoutExercise
import com.ryim.actin.ui.EditWorkoutViewModel
import com.ryim.actin.ui.SharedWorkoutViewModel

@Composable
fun ExerciseRow(
    listOfExercises: List<String>,
    exercise: WorkoutExercise,
    onNameChange: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        IconButton(onClick = onMoveUp) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
        }

        IconButton(onClick = onMoveDown) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
        }

        val isError = exercise.name.isBlank()

        val suggestions = remember(exercise.name, listOfExercises) {
            listOfExercises
                .filter { it.contains(exercise.name, ignoreCase = true) }
                .sorted()
                .take(6)
        }

        var expanded by remember { mutableStateOf(false) }
        var isFocused by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }

        Column(
            modifier = Modifier
                .weight(1f)   // width control happens here
        ) {
            TextField(
                value = exercise.name,
                onValueChange = {
                    onNameChange(it)
                    expanded = true
                },
                label = { Text("Exercise name") },
                isError = isError,
                supportingText = {
                    if (isError) Text("Name is required")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        isFocused = state.isFocused
                        if (!state.isFocused) expanded = false
                    }
            )

            if (expanded && isFocused && suggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .focusProperties { canFocus = false }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .focusProperties { canFocus = false }
                    ) {
                        items(suggestions) { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onNameChange(suggestion)
                                        expanded = false
                                        focusRequester.requestFocus()
                                    }
                                    .padding(12.dp)
                                    .focusProperties { canFocus = false }
                            )
                        }
                    }
                }
            }
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutScreen(
    onBack: () -> Unit,
    sharedWorkoutViewModel: SharedWorkoutViewModel,
    viewModel: EditWorkoutViewModel = hiltViewModel()
) {
    val selectedWorkout by sharedWorkoutViewModel.selectedWorkout.collectAsState()

    LaunchedEffect(selectedWorkout) {
        println("Selected workout = $selectedWorkout")
        println("Edit mode = ${viewModel.editMode}")

        if (selectedWorkout != null && !viewModel.editMode) {
            viewModel.loadWorkout(selectedWorkout!!)
        }
    }

    val canSave = viewModel.name.isNotBlank() &&
            viewModel.exercises.all { it.name.isNotBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Workout",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .navigationBarsPadding()   // ← This is the magic line
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // different from box
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Back",
//                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Button(
                    onClick = {
                        viewModel.save()
                        onBack()
                    },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary, // different from box
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        "Confirm",
//                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
//                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            val isNameError = viewModel.name.isBlank()

            TextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Workout name") },
                isError = isNameError,
                supportingText = {
                    if (isNameError) {
                        Text("Name is required")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )


            Button(onClick = { viewModel.addExercise() }) {
                Text("Add Exercise")
            }

//            Text("Count: ${viewModel.exercises.size}")

            Spacer(Modifier.height(16.dp))

            // Dynamic list of exercise text fields
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(
                    items = viewModel.exercises,
                    key = { it.id }
                ) { exercise ->

                    ExerciseRow(
                        listOfExercises = viewModel.uiState.collectAsState().value.exerciseNames,
                        exercise = exercise,
                        onNameChange = { viewModel.updateExercise(exercise.id, it) },
                        onMoveUp = { viewModel.moveUp(exercise.id) },
                        onMoveDown = { viewModel.moveDown(exercise.id) },
                        onDelete = { viewModel.removeExercise(exercise.id) }
                    )
                }
            }
        }
    }
}

