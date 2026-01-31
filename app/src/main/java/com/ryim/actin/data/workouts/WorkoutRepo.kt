package com.ryim.actin.data.workouts

import android.content.Context
import com.ryim.actin.domain.workouts.Workout
import com.ryim.actin.domain.workouts.WorkoutRepository
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

class WorkoutRepositoryImpl(
    private val context: Context
) : WorkoutRepository {

    private val fileName = File(context.filesDir, "workouts.json")

    private val prettyJson = Json {
        prettyPrint = true
    }

    override suspend fun saveOrReplaceWorkout(workout: Workout, editMode: Boolean) {

        // Load existing workouts (DTO list)
        val existing = if (fileName.exists()) {
            val text = fileName.readText()
            if (text.isNotBlank()) {
                Json.decodeFromString(
                    ListSerializer(WorkoutDto.serializer()),
                    text
                )
            } else emptyList()
        } else emptyList()

        val finalDto = workout.toDto()

        val updated = if (editMode) {
            val mutable = existing.toMutableList()

            // Replace by ID
            val index = mutable.indexOfFirst { it.id == finalDto.id }
            if (index != -1) {
                mutable.removeAt(index)
            }

            mutable + finalDto
        } else {
            existing + finalDto
        }

        fileName.writeText(
            prettyJson.encodeToString(
                ListSerializer(WorkoutDto.serializer()),
                updated
            )
        )
    }

    override suspend fun loadWorkouts(): List<Workout> {
        if (!fileName.exists()) return emptyList()

        val text = fileName.readText()
        val list = Json.decodeFromString<List<WorkoutDto>>(text)
        return list.map { it.toDomain() }
    }

    override suspend fun deleteWorkout(id: String) {

        val existing = if (fileName.exists()) {
            val text = fileName.readText()
            if (text.isNotBlank()) {
                Json.decodeFromString(
                    ListSerializer(WorkoutDto.serializer()),
                    text
                )
            } else emptyList()
        } else emptyList()

        val updated = existing.filterNot { it.id == id }

        val prettyJson = Json { prettyPrint = true }

        fileName.writeText(
            prettyJson.encodeToString(
                ListSerializer(WorkoutDto.serializer()),
                updated
            )
        )
    }

    override suspend fun exportJson(): String {
        return if (fileName.exists()) fileName.readText() else "[]"
    }

    override suspend fun importJson(json: String) {
        fileName.writeText(json)
    }
}