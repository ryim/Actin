package com.ryim.actin.data.workouts

import android.content.Context
import com.ryim.actin.domain.workouts.Workout
import com.ryim.actin.domain.workouts.WorkoutRepository
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class WorkoutRepositoryImpl(
    private val context: Context
) : WorkoutRepository {

    private val file = File(context.filesDir, "workouts.json")

    private val prettyJson = Json {
        prettyPrint = true
    }

    override suspend fun saveWorkout(workout: Workout) {
        val existing = loadWorkouts().map { it.toDto() }.toMutableList()
        existing.add(workout.toDto())

        file.writeText(prettyJson.encodeToString(existing))
    }


    override suspend fun loadWorkouts(): List<Workout> {
        if (!file.exists()) return emptyList()

        val text = file.readText()
        val list = Json.decodeFromString<List<WorkoutDto>>(text)
        return list.map { it.toDomain() }
    }
}