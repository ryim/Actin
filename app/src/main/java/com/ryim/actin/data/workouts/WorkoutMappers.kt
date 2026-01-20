package com.ryim.actin.data.workouts

import com.ryim.actin.domain.workouts.Workout
import com.ryim.actin.domain.workouts.WorkoutExercise

fun Workout.toDto() = WorkoutDto(id, name, exercises.map { it.toDto() })
fun WorkoutDto.toDomain() = Workout(id, name, exercises.map { it.toDomain() })

fun WorkoutExercise.toDto() = WorkoutExerciseDto(id, name)

fun WorkoutExerciseDto.toDomain() = WorkoutExercise(
    id = id,
    name = name
)
