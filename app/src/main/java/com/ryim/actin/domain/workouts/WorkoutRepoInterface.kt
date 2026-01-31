package com.ryim.actin.domain.workouts

interface WorkoutRepository {
//    suspend fun saveWorkout(workout: Workout)

    suspend fun saveOrReplaceWorkout(
        workout: Workout,
        editMode: Boolean)

    suspend fun loadWorkouts(): List<Workout>

    suspend fun deleteWorkout(id: String)

    suspend fun exportJson(): String

    suspend fun importJson(json: String)
}
