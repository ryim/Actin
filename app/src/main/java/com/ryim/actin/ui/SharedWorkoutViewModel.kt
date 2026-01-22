package com.ryim.actin.ui

import androidx.lifecycle.ViewModel
import com.ryim.actin.domain.workouts.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class SharedWorkoutViewModel @Inject constructor() : ViewModel() {

    private val _selectedWorkout = MutableStateFlow<Workout?>(null)
    val selectedWorkout = _selectedWorkout.asStateFlow()

    fun selectWorkout(workout: Workout) {
        _selectedWorkout.value = workout
    }

    fun clearSelectedWorkout() {
        _selectedWorkout.value = null
    }
}
