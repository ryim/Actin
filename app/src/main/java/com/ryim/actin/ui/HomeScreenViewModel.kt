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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock
import kotlin.time.ExperimentalTime

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

    @OptIn(ExperimentalTime::class)
    private fun loadHistory() {
        viewModelScope.launch {
            val all = repo.loadExercises()

            val latestByName = all
                .groupBy { it.name }
                .mapValues { (_, entries) ->
                    entries.maxByOrNull { entry ->
                        Instant.parse(entry.timestamp!!)
                    }
                }
                .values
                .filterNotNull()
                .sortedByDescending { entry ->
                    Instant.parse(entry.timestamp!!)
                }

            _uiState.update { it.copy(latestExercises = latestByName) }
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

            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date

            // Rolling week boundaries
            val startOfThisWeek = today.minus(DatePeriod(days = 6))   // last 7 days
            val startOfLastWeek = today.minus(DatePeriod(days = 13))  // 8–14 days ago
            val endOfLastWeek = today.minus(DatePeriod(days = 7))

            // Convert timestamp → LocalDate
            fun ExerciseEntry.date(): LocalDate =
                Instant.parse(timestamp!!)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date

            // This rolling week: last 7 days
            val thisWeek = all.filter { entry ->
                val date = entry.date()
                date in startOfThisWeek..today
            }

            // Last rolling week: 8–14 days ago
            val lastWeek = all.filter { entry ->
                val date = entry.date()
                date in startOfLastWeek..endOfLastWeek
            }

            _uiState.update {
                it.copy(
                    lastWeekExercises = lastWeek,
                    thisWeekExercises = thisWeek
                )
            }
        }
    }
}

data class MainUiState(
    val latestExercises: List<ExerciseEntry> = emptyList(),
    val lastWeekExercises: List<ExerciseEntry> = emptyList(),
    val thisWeekExercises: List<ExerciseEntry> = emptyList()
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