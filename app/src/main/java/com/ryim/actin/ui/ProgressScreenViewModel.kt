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

            // Extract unique exercise names (sorted alphabetically)
            val uniqueNames = sorted
                .map { it.name }
                .distinct()
                .sorted()

            // Pick a default selected exercise
            // Option A: first alphabetically
            val defaultExercise = sorted.firstOrNull()?.name

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
                    weeklyCounts = weekly,
                    exerciseNames = uniqueNames,
                    selectedExerciseName = defaultExercise
                )
            }

            // Now that state is ready, update the graph
            updateGraphData(defaultExercise, uiState.value.selectedMetric)
        }
    }

    fun deleteExercise(entry: ExerciseEntry) {
        viewModelScope.launch {
            repo.deleteExercise(entry.id)
            loadHistory()   // refresh both lists
        }
    }

    fun setMetric(metric: MetricType, exerciseName: String) {
        _uiState.update { it.copy(selectedMetric = metric) }
        updateGraphData(exerciseName, metric)
    }

    fun setSelectedExercise(name: String) {
        _uiState.update { it.copy(selectedExerciseName = name) }
        updateGraphData(name, uiState.value.selectedMetric)
    }

    fun setTimePeriod(period: TimePeriod) {
        _uiState.update { it.copy(selectedTimePeriod = period) }

        val exercise = uiState.value.selectedExerciseName ?: return
        val metric = uiState.value.selectedMetric

        updateGraphData(exercise, metric)
    }

    //  Stuff for the changeable graph
    fun updateGraphData(
        exerciseName: String?,
        metric: MetricType
    ) {
        if (exerciseName == null) return
        val state = uiState.value

        // 1. Determine cutoff date based on selected time period
        val months = state.selectedTimePeriod.months
        val cutoffDate = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .minus(DatePeriod(months = months))

        // 2. Filter entries by exercise name AND time period
        val entries = state.allExercises
            .filter { it.name == exerciseName }
            .filter { entry ->
                val date = entry.timestamp
                    ?.let { Instant.parse(it).toLocalDateTime(TimeZone.currentSystemDefault()).date }
                    ?: LocalDate(entry.year, entry.month, entry.day)

                date >= cutoffDate
            }
            .sortedBy { it.timestamp }

        // 3. Build graph data based on metric type
        when (metric) {

            MetricType.ALL_REPS -> {
                val series = List(6) { mutableListOf<DataPoint>() }

                entries.forEach { entry ->
                    val date = entry.timestamp
                        ?.let { Instant.parse(it).toLocalDateTime(TimeZone.currentSystemDefault()).date }
                        ?: LocalDate(entry.year, entry.month, entry.day)

                    entry.reps.take(6).forEachIndexed { index, reps ->
                        series[index].add(DataPoint(date, reps.toFloat()))
                    }
                }

                _uiState.update { it.copy(multiGraphData = series) }
            }

            MetricType.TOTAL_REPS,
            MetricType.TOTAL_VOLUME,
            MetricType.LAST_SET_REPS -> {

                val sorted = entries.map { entry ->

                    val date = entry.timestamp
                        ?.let { Instant.parse(it).toLocalDateTime(TimeZone.currentSystemDefault()).date }
                        ?: LocalDate(entry.year, entry.month, entry.day)

                    val totalReps = entry.reps.sum()
                    val totalVolume = entry.reps.zip(entry.weights) { r, w -> r * w }.sum()
                    val lastSetReps = entry.reps.lastOrNull()?.toFloat() ?: 0f   // NEW

                    val yValue = when (metric) {
                        MetricType.TOTAL_REPS -> totalReps.toFloat()
                        MetricType.TOTAL_VOLUME -> totalVolume
                        MetricType.LAST_SET_REPS -> lastSetReps
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
    ALL_REPS("Reps per set"),
    LAST_SET_REPS("Last set reps")
}

enum class TimePeriod(val label: String, val months: Int) {
    ONE_MONTH("1 month", 1),
    THREE_MONTHS("3 months", 3),
    SIX_MONTHS("6 months", 6),
    ONE_YEAR("1 year", 12)
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
    val multiGraphData: List<List<DataPoint>> = emptyList(),
    val selectedTimePeriod: TimePeriod = TimePeriod.THREE_MONTHS,
    val selectedMetric: MetricType = MetricType.TOTAL_REPS,
    val exerciseNames: List<String> = emptyList(),
    val selectedExerciseName: String? = null,
)