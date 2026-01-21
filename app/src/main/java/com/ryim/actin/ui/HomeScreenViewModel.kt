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
import java.time.LocalDate

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
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val all = repo.loadExercises()

            val latestByName = all
                .groupBy { it.name }
                .mapValues { (_, entries) ->
                    entries.maxByOrNull { entry ->
                        LocalDate.of(entry.year, entry.month, entry.day)
                    }
                }
                .values
                .filterNotNull()
                .sortedByDescending { LocalDate.of(it.year, it.month, it.day) }

            _uiState.update { it.copy(latestExercises = latestByName) }
        }
    }

    fun deleteExercise(entry: ExerciseEntry) {
        viewModelScope.launch {
            repo.deleteExercise(entry.name, entry.day, entry.month, entry.year)
            repo.loadExercises()   // ← now safe, because we're inside a coroutine
        }
    }

    private fun ExerciseEntry.toLocalDate(): LocalDate =
        LocalDate.of(year, month, day)

    private fun loadUngroupedHistForStats() {
        viewModelScope.launch {
            val all = repo.loadExercises()

            val today = LocalDate.now()

            // Rolling week boundaries
            val startOfThisWeek = today.minusDays(6)   // last 7 days including today
            val startOfLastWeek = today.minusDays(13)  // 8–14 days ago
            val endOfLastWeek = today.minusDays(7)

            // Convert once for efficiency
            fun ExerciseEntry.date(): LocalDate =
                LocalDate.of(year, month, day)

            // This rolling week: last 7 days
            val thisWeek = all.filter { entry ->
                val date = entry.date()
                !date.isBefore(startOfThisWeek) && !date.isAfter(today)
            }

            // Last rolling week: 8–14 days ago
            val lastWeek = all.filter { entry ->
                val date = entry.date()
                !date.isBefore(startOfLastWeek) && !date.isAfter(endOfLastWeek)
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