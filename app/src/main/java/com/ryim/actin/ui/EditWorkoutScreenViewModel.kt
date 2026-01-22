package com.ryim.actin.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryim.actin.domain.workouts.Workout
import com.ryim.actin.domain.workouts.WorkoutExercise
import com.ryim.actin.domain.workouts.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class EditWorkoutViewModel @Inject constructor(
    private val repo: WorkoutRepository
) : ViewModel() {

    var name by mutableStateOf("")
    var exercises = mutableStateListOf<WorkoutExercise>()
        private set

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
            repo.saveWorkout(
                Workout(
                    name = name,
                    exercises = exercises.toList()
                )
            )
        }
    }
}
