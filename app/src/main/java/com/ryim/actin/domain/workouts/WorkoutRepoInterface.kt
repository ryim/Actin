package com.ryim.actin.domain.workouts

interface WorkoutRepository {
    suspend fun saveWorkout(workout: Workout)
    suspend fun loadWorkouts(): List<Workout>
}
