package com.ryim.actin.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryim.actin.domain.ExerciseRepository
import com.ryim.actin.domain.workouts.Workout
import com.ryim.actin.domain.workouts.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class WorkoutListScreenViewModel @Inject constructor(
    private val repo: WorkoutRepository,
    private val exRepo: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FullHistoryUIState())
    val uiState = _uiState.asStateFlow()

    var workouts by mutableStateOf<List<Workout>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            workouts = repo.loadWorkouts()
        }
//        loadHistory()
    }

    suspend fun refresh() {
        workouts = repo.loadWorkouts()
//        loadHistory()
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repo.deleteWorkout(workout.id)
            refresh()
        }
    }

//    private fun loadHistory() {
//        viewModelScope.launch {
//            val all = exRepo.loadExercises()
//
//            val sorted = all.sortedByDescending { entry ->
//                LocalDate.of(entry.year, entry.month, entry.day)
//            }
//
//            _uiState.update {
//                it.copy(
//                    allExercises = sorted,
//                )
//            }
//        }
//    }
}
