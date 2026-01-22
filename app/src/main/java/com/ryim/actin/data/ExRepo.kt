package com.ryim.actin.data

import android.content.Context
import com.ryim.actin.domain.ExerciseEntry
import com.ryim.actin.domain.ExerciseRepository
import com.ryim.actin.domain.noonTimestamp
import kotlinx.serialization.builtins.ListSerializer
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ExerciseRepositoryImpl(
    private val context: Context
) : ExerciseRepository {

    private val fileName = "exercises.json"
    val tsvFile = File(context.filesDir, "exercises_export.tsv")

    override suspend fun saveOrReplaceExercise(
        entry: ExerciseEntry,
        editMode: Boolean
    ) {
        // Ensure timestamp and workout exist
        val finalEntry = entry.copy(
            timestamp = entry.timestamp ?: entry.noonTimestamp(),
            workout = entry.workout ?: ""
        )

        val file = File(context.filesDir, fileName)

        val existing = if (file.exists()) {
            val text = file.readText()
            if (text.isNotBlank()) {
                Json.decodeFromString(
                    ListSerializer(ExerciseEntry.serializer()),
                    text
                )
            } else emptyList()
        } else emptyList()

        val updated = if (editMode) {
            val mutable = existing.toMutableList()
            val index = mutable.indexOfFirst {
                it.name == finalEntry.name &&
                        it.day == finalEntry.day &&
                        it.month == finalEntry.month &&
                        it.year == finalEntry.year
            }

            if (index != -1) {
                mutable.removeAt(index)
            }

            mutable + finalEntry
        } else {
            existing + finalEntry
        }

        val prettyJson = Json { prettyPrint = true }

        file.writeText(
            prettyJson.encodeToString(
                ListSerializer(ExerciseEntry.serializer()),
                updated
            )
        )
    }

    override suspend fun deleteExercise(
        name: String,
        day: Int,
        month: Int,
        year: Int
    ) {
        val file = File(context.filesDir, fileName)

        val existing = if (file.exists()) {
            val text = file.readText()
            if (text.isNotBlank()) {
                Json.decodeFromString(
                    ListSerializer(ExerciseEntry.serializer()),
                    text
                )
            } else emptyList()
        } else emptyList()

        // Remove ONE matching entry
        val updated = existing.toMutableList().apply {
            val index = indexOfFirst {
                it.name == name &&
                        it.day == day &&
                        it.month == month &&
                        it.year == year
            }
            if (index != -1) removeAt(index)
        }

        // Write back
        val prettyJson = Json {
            prettyPrint = true
//            prettyPrintIndent = "  "
        }

        file.writeText(
            prettyJson.encodeToString(
                ListSerializer(ExerciseEntry.serializer()),
                updated
            )
        )
    }

    override suspend fun loadExercises(): List<ExerciseEntry> {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)

            if (!file.exists()) {
                return@withContext emptyList()
            }

            val json = file.readText()
            if (json.isBlank()) {
                emptyList()
            } else {
                val decoded = Json.decodeFromString(
                    ListSerializer(ExerciseEntry.serializer()),
                    json
                )

                decoded.map { entry ->
                    entry.copy(
                        timestamp = entry.timestamp ?: entry.noonTimestamp(),
                        workout = entry.workout ?: ""
                    )
                }
            }
        }
    }

    override suspend fun exportJson(): String {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) file.readText() else "[]"
    }

    override suspend fun importJson(json: String) {
        val file = File(context.filesDir, fileName)
        file.writeText(json)
    }

    override suspend fun buildTsvString(): String = withContext(Dispatchers.IO) {
        val exercises = loadExercises()

        val header = listOf(
            "name",
            "sets",
            "reps",
            "weights",
            "useKg",
            "day",
            "month",
            "year",
            "timestamp",
            "workout"
        ).joinToString("\t")

        val rows = exercises.map { entry ->
            listOf(
                entry.name,
                entry.sets.toString(),
                entry.reps.joinToString(","),
                entry.weights.joinToString(","),
                entry.useKg.toString(),
                entry.day.toString(),
                entry.month.toString(),
                entry.year.toString(),
                entry.timestamp ?: "",
                entry.workout ?: ""
            ).joinToString("\t")
        }

        (listOf(header) + rows).joinToString("\n")
    }
}
