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
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

@HiltViewModel
class LogScreenViewModel @Inject constructor(
    private val repo: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FullHistoryUIState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun refresh() {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val all = repo.loadExercises()

            val sorted = all.sortedByDescending { entry ->
                LocalDate.of(entry.year, entry.month, entry.day)
            }

            val today = LocalDate.now()
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

            val last12Weeks = (0 until 12).map { offset ->
                currentWeekStart.minusWeeks((11 - offset).toLong())
            }

            val entriesByWeek = all.groupBy { entry ->
                val date = LocalDate.of(entry.year, entry.month, entry.day)
                date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            }

            val weekly = last12Weeks.map { weekStart ->
                WeeklyCount(
                    weekStart = weekStart,
                    count = entriesByWeek[weekStart]?.size ?: 0
                )
            }

            _uiState.update {
                it.copy(
                    allExercises = sorted,
                    weeklyCounts = weekly
                )
            }
        }
    }


    fun deleteExercise(entry: ExerciseEntry) {
        viewModelScope.launch {
            repo.deleteExercise(entry.name, entry.day, entry.month, entry.year)
            loadHistory()   // refresh both lists
        }
    }
}


data class WeeklyCount(
    val weekStart: LocalDate,
    val count: Int
)

data class FullHistoryUIState(
    val allExercises: List<ExerciseEntry> = emptyList(),
    val weeklyCounts: List<WeeklyCount> = emptyList()
)
