package com.ryim.actin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryim.actin.domain.ExerciseEntry
import com.ryim.actin.domain.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.daysUntil

//  Function for saving the data on confirmation, hooking it into the domain layer
@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val repo: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHistory()
        loadUngroupedHistForStats()
    }

    fun refresh() {
        loadHistory()
        loadUngroupedHistForStats()
    }

    private suspend fun loadEntriesWithAges(): List<DatedEntry> {
        val all = repo.loadExercises()
        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone).date

        return all.map { entry ->
            val instant = Instant.parse(entry.timestamp!!)
            val date = instant.toLocalDateTime(zone).date
            val age = date.daysUntil(today)

            DatedEntry(entry, date, age, instant)
        }
    }

    private fun loadUngroupedHistForStats() {
        viewModelScope.launch {
            val entries = loadEntriesWithAges()

            val thisWeek = entries.filter { it.ageInDays in 0..6 }
            val lastWeek = entries.filter { it.ageInDays in 7..13 }

            _uiState.update {
                it.copy(
                    lastWeekExercises = lastWeek.map { it.entry },
                    thisWeekExercises = thisWeek.map { it.entry },
                    thisWeekWorkoutCount = countWorkouts(thisWeek),
                    lastWeekWorkoutCount = countWorkouts(lastWeek)
                )
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val entries = loadEntriesWithAges()

            val recent = entries
                .filter { it.ageInDays in 0..14 }
                .sortedByDescending { it.instant }
                .map { it.entry }

            _uiState.update { it.copy(latestExercises = recent) }
        }
    }

    fun deleteExercise(entry: ExerciseEntry) {
        viewModelScope.launch {
            repo.deleteExercise(entry.name, entry.day, entry.month, entry.year)
            repo.loadExercises()   // ← now safe, because we're inside a coroutine
        }
    }

    private fun countWorkouts(entries: List<DatedEntry>): Int {
        if (entries.isEmpty()) return 0

        val sorted = entries.sortedBy { it.instant }

        var workouts = 1
        var lastTime = sorted.first().instant

        for (i in 1 until sorted.size) {
            val current = sorted[i].instant
            val diff = current - lastTime

            if (diff.inWholeSeconds >= 7200) {
                workouts++
            }

            lastTime = current
        }

        return workouts
    }
}

data class MainUiState(
    val latestExercises: List<ExerciseEntry> = emptyList(),
    val lastWeekExercises: List<ExerciseEntry> = emptyList(),
    val thisWeekExercises: List<ExerciseEntry> = emptyList(),

    // New fields for workout counts
    val lastWeekWorkoutCount: Int = 0,
    val thisWeekWorkoutCount: Int = 0
)

data class DatedEntry(
    val entry: ExerciseEntry,
    val date: LocalDate,
    val ageInDays: Int,
    val instant: Instant
)