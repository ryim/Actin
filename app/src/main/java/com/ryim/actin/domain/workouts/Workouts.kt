package com.ryim.actin.domain.workouts

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val exercises: List<WorkoutExercise>
)

@Serializable
data class WorkoutExercise(
    val id: String = UUID.randomUUID().toString(),
    var name: String = ""
)

