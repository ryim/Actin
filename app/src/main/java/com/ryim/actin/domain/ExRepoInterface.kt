package com.ryim.actin.domain

interface ExerciseRepository {
//    suspend fun saveExercise(entry: ExerciseEntry)
    suspend fun saveOrReplaceExercise(
        entry: ExerciseEntry,
        editMode: Boolean
    )

    suspend fun deleteExercise(
        id: String
    )
    suspend fun loadExercises(): List<ExerciseEntry>

    suspend fun exportJson(): String

    suspend fun importJson(json: String)

    suspend fun buildTsvString(): String
}
