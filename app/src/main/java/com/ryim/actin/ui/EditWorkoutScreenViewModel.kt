package com.ryim.actin.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryim.actin.domain.ExerciseEntry
import com.ryim.actin.domain.ExerciseRepository
import com.ryim.actin.domain.workouts.Workout
import com.ryim.actin.domain.workouts.WorkoutExercise
import com.ryim.actin.domain.workouts.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

@HiltViewModel
class EditWorkoutViewModel @Inject constructor(
    private val repo: WorkoutRepository,
    private val exRepo: ExerciseRepository
) : ViewModel() {


    init {
        loadHistory()
    }

    var workoutId: String? = null
    var editMode by mutableStateOf(false)
    var name by mutableStateOf("")
    var exercises = mutableStateListOf<WorkoutExercise>()
        private set

    fun loadWorkout(workout: Workout) {
        workoutId = workout.id
        name = workout.name
        exercises.clear()
        exercises.addAll(workout.exercises)
        editMode = true
    }

    fun addExercise() {
        exercises.add(WorkoutExercise())
    }

    fun updateExercise(id: String, newName: String) {
        val index = exercises.indexOfFirst { it.id == id }
        if (index != -1) {
            exercises[index] = exercises[index].copy(name = newName)
        }
    }

    fun removeExercise(id: String) {
        exercises.removeAll { it.id == id }
    }

    fun moveUp(id: String) {
        val index = exercises.indexOfFirst { it.id == id }
        if (index > 0) {
            exercises.add(index - 1, exercises.removeAt(index))
        }
    }

    fun moveDown(id: String) {
        val index = exercises.indexOfFirst { it.id == id }
        if (index != -1 && index < exercises.lastIndex) {
            exercises.add(index + 1, exercises.removeAt(index))
        }
    }

    fun save() {
        viewModelScope.launch {
            val workout = Workout(
                id = workoutId ?: UUID.randomUUID().toString(),
                name = name,
                exercises = exercises.toList()
            )

            repo.saveOrReplaceWorkout(
                workout,
                editMode = editMode
            )
        }
    }

    //  Make a uiState and store the unique exercise names in it
    private val _uiState = MutableStateFlow(FullHistoryUIState())
    val uiState = _uiState.asStateFlow()

    private fun loadHistory() {
        viewModelScope.launch {
            val all = exRepo.loadExercises()

            // Extract unique exercise names (sorted alphabetically)
            val uniqueNames = all
                .map { it.name }
                .distinct()
                .sorted()

            _uiState.update {
                it.copy(
                    exerciseNames = uniqueNames,
                )
            }
        }
    }
}
