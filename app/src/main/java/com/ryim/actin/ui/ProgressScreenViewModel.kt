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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime


@HiltViewModel
class ProgressScreenViewModel @Inject constructor(
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

            // Convert timestamp → LocalDate (user’s timezone)
            fun ExerciseEntry.date(): LocalDate =
                Instant.parse(timestamp!!)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date

            // Sort all exercises by timestamp descending
            val sorted = all.sortedByDescending { entry ->
                Instant.parse(entry.timestamp!!)
            }

            // Today’s date in local timezone
            val today: LocalDate =
                Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date

            // Start of the current week (Monday)
            val currentWeekStart =
                today.minus(DatePeriod(days = today.dayOfWeek.ordinal))

            // Generate the last 12 week-start dates (Mondays)
            val last12Weeks = (0 until 12).map { offset ->
                currentWeekStart.minus(DatePeriod(days = (11 - offset) * 7))
            }

            // Group entries by their week-start (Monday)
            val entriesByWeek = all.groupBy { entry ->
                val date = entry.date()
                date.minus(DatePeriod(days = date.dayOfWeek.ordinal))
            }

            // Build weekly counts
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

    //  Stuff for the changeable graph
    fun updateGraphData(
        exerciseName: String,
        metric: MetricType
    ) {
        val entries = uiState.value.allExercises
            .filter { it.name == exerciseName }
            .sortedBy { it.timestamp }

        when (metric) {

            MetricType.ALL_REPS -> {
                // Up to 6 lines, one per rep index
                val series = List(6) { mutableListOf<DataPoint>() }

                entries.forEach { entry ->
                    val date = entry.timestamp
                        ?.let { Instant.parse(it).toLocalDateTime(TimeZone.UTC).date }
                        ?: LocalDate(entry.year, entry.month, entry.day)

                    entry.reps.take(6).forEachIndexed { index, reps ->
                        series[index].add(DataPoint(date, reps.toFloat()))
                    }
                }

                _uiState.update { it.copy(multiGraphData = series) }
            }

            MetricType.TOTAL_REPS,
            MetricType.TOTAL_VOLUME -> {
                val sorted = entries.map { entry ->

                    val date = entry.timestamp
                        ?.let { Instant.parse(it).toLocalDateTime(TimeZone.UTC).date }
                        ?: LocalDate(entry.year, entry.month, entry.day)

                    val totalReps = entry.reps.sum()
                    val totalVolume = entry.reps.zip(entry.weights) { r, w -> r * w }.sum()

                    val yValue = when (metric) {
                        MetricType.TOTAL_REPS -> totalReps.toFloat()
                        MetricType.TOTAL_VOLUME -> totalVolume
                        else -> 0f
                    }

                    DataPoint(date, yValue)
                }
                    .sortedBy { it.date }

                _uiState.update { it.copy(multiGraphData = listOf(sorted)) }
            }
        }
    }
}

enum class MetricType(val label: String) {
    TOTAL_REPS("Total reps"),
    TOTAL_VOLUME("Total volume"),
    ALL_REPS("Reps per set")
}

data class DataPoint(
    val date: LocalDate,
    val value: Float
)

data class WeeklyCount(
    val weekStart: LocalDate,
    val count: Int
)

data class FullHistoryUIState(
    val allExercises: List<ExerciseEntry> = emptyList(),
    val weeklyCounts: List<WeeklyCount> = emptyList(),
    val graphData: List<DataPoint> = emptyList(),
    val multiGraphData: List<List<DataPoint>> = emptyList()
)
