package com.ryim.actin.data.workouts

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutDto(
    val id: String,
    val name: String,
    val exercises: List<WorkoutExerciseDto>
)

@Serializable
data class WorkoutExerciseDto(
    val id: String,
    val name: String
)

