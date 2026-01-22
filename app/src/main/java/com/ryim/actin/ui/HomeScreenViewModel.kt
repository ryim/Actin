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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

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

    private fun loadHistory() {
        viewModelScope.launch {
            val all = repo.loadExercises()

            val zone = TimeZone.currentSystemDefault()
            val today = Clock.System.now()
                .toLocalDateTime(zone)
                .date

            fun ExerciseEntry.date(): LocalDate =
                Instant.parse(timestamp!!)
                    .toLocalDateTime(zone)
                    .date

            // Keep only entries from the last 21 days
            val recent = all.filter { entry ->
                val entryDate = entry.date()
                val ageInDays = entryDate.daysUntil(today)   // 0 = today, 1 = yesterday, etc.
                ageInDays in 0..21
            }
                .sortedByDescending { entry ->
                    Instant.parse(entry.timestamp!!)
                }

            _uiState.update { it.copy(latestExercises = recent) }
        }
    }

    fun deleteExercise(entry: ExerciseEntry) {
        viewModelScope.launch {
            repo.deleteExercise(entry.name, entry.day, entry.month, entry.year)
            repo.loadExercises()   // ← now safe, because we're inside a coroutine
        }
    }

//    private fun ExerciseEntry.toLocalDate(): LocalDate =
//        LocalDate.of(year, month, day)

    private fun loadUngroupedHistForStats() {
        viewModelScope.launch {
            val all = repo.loadExercises()

            val zone = TimeZone.currentSystemDefault()
            val today: LocalDate = Clock.System.now()
                .toLocalDateTime(zone)
                .date

            fun ExerciseEntry.date(): LocalDate =
                Instant.parse(timestamp!!)
                    .toLocalDateTime(zone)
                    .date

            // daysUntil(other) is positive when `other` is after `this`
            val thisWeek = all.filter { entry ->
                val d = entry.date()
                val diff = d.daysUntil(today)  // 0 = today, 1 = yesterday, etc.
                diff in 0..6
            }

            val lastWeek = all.filter { entry ->
                val d = entry.date()
                val diff = d.daysUntil(today)
                diff in 7..13
            }

            _uiState.update {
                it.copy(
                    lastWeekExercises = lastWeek,
                    thisWeekExercises = thisWeek,
                    thisWeekWorkoutCount = countWorkouts(thisWeek),
                    lastWeekWorkoutCount = countWorkouts(lastWeek)
                )
            }
        }
    }

    private fun countWorkouts(entries: List<ExerciseEntry>): Int {
        if (entries.isEmpty()) return 0

        val sorted = entries.sortedBy { Instant.parse(it.timestamp!!) }

        var workouts = 1
        var lastTime = Instant.parse(sorted.first().timestamp!!)

        for (i in 1 until sorted.size) {
            val current = Instant.parse(sorted[i].timestamp!!)
            val diff = current - lastTime

            // 2 hours = 7200 seconds
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


//    fun deleteExercise(entry: ExerciseEntry) {
//        viewModelScope.launch {
//            repo.deleteExercise(
//                name = entry.name,
//                day = entry.day,
//                month = entry.month,
//                year = entry.year
//            )
//        }
//        repo.loadExercises()
//    }