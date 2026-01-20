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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

//  Function for saving the data on confirmation, hooking it into the domain layer
@HiltViewModel
class ExAddViewModel @Inject constructor(
    private val repo: ExerciseRepository
) : ViewModel() {

    //  Today's date
    private val today: LocalDate = java.time.LocalDate.now()
    private val now: java.time.LocalTime? = java.time.LocalTime.now()

    // Default number of sets
    private val initialSets = 3

    // Backing state
    private val _uiState = MutableStateFlow(
        ExAddUiState(
            sets = initialSets,
            reps = List(initialSets) { 8 },
            weights = List(initialSets) { "" },
            day = today.dayOfMonth.toString(),
            month = today.monthValue.toString(),
            year = today.year.toString(),
            hour = now?.hour.toString(),
            minute = now?.minute.toString(),
            useKg = true,
            editMode = false,
            workout = ""
        )
    )
    val uiState = _uiState.asStateFlow()

    //  Set the parameters if they have been passed here
    @OptIn(ExperimentalTime::class)
    fun setPrefillParams(
        name: String,
        oldSets: Int,
        oldReps: List<Int>,
        oldWeights: List<String>,
        oldUseKg: Boolean,
        editMode: Boolean,
        oldTimestamp: String?,
        workout: String?
    ) {
        _uiState.update {
            it.copy(
                name = name,
                sets = oldSets,
                reps = oldReps,
                weights = oldWeights,
                useKg = oldUseKg,
                editMode = editMode,
                workout = workout
            )
        }
        if (editMode && oldTimestamp != null) {
            val instant = Instant.parse(oldTimestamp)
            val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

            _uiState.update {
                it.copy(
                    day = date.dayOfMonth.toString(),
                    month = date.monthNumber.toString(),
                    year = date.year.toString()
                )
            }
        }

        //  Update the time and date pickers with the current timestamp
        if (editMode && oldTimestamp != null) {
            val instant = Instant.parse(oldTimestamp)
            val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            _uiState.update {
                it.copy(
                    day = ldt.date.dayOfMonth.toString(),
                    month = ldt.date.monthNumber.toString(),
                    year = ldt.date.year.toString(),
                    hour = ldt.time.hour.toString(),
                    minute = ldt.time.minute.toString()
                )
            }
        }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun incrementSets() {
        _uiState.update { state ->
            val newSets = state.sets + 1

            state.copy(
                sets = newSets,
                weights = List(newSets) { i -> state.weights.getOrNull(i) ?: "" },
                reps = List(newSets) { i -> state.reps.getOrNull(i) ?: 0 }
            )
        }
    }

    fun decrementSets() {
        _uiState.update { state ->
            if (state.sets == 0) return@update state

            val newSets = state.sets - 1

            state.copy(
                sets = newSets,
                reps = state.reps.take(newSets),
                weights = state.weights.take(newSets)
            )
        }
    }

    fun incrementRep(index: Int) {
        _uiState.update { state ->
            val updated = state.reps.toMutableList().apply {
                this[index] = this[index] + 1
            }
            state.copy(reps = updated)
        }
    }

    fun decrementRep(index: Int) {
        _uiState.update { state ->
            val updated = state.reps.toMutableList().apply {
                if (this[index] > 0) this[index] = this[index] - 1
            }
            state.copy(reps = updated)
        }
    }

    fun updateWeight(index: Int, newText: String) {
        _uiState.update { state ->
            val updated = state.weights.toMutableList().apply {
                this[index] = newText
            }
            state.copy(weights = updated)
        }
    }

    fun onUseKgChanged(newValue: Boolean) {
        _uiState.update { it.copy(useKg = newValue) }
    }

    fun updateDate(day: Int, month: Int, year: Int) {
        _uiState.update {
            it.copy(
                day = day.toString(),
                month = month.toString(),
                year = year.toString()
            )
        }
    }

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update {
            it.copy(
                hour = hour.toString(),
                minute = minute.toString()
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    fun saveExercise() {
        val state = uiState.value

        val date = kotlinx.datetime.LocalDate(
            state.year.toInt(),
            state.month.toInt(),
            state.day.toInt()
        )

        val time = LocalTime(
            state.hour.toInt(),
            state.minute.toInt()
        )

        val localDateTime = LocalDateTime(date, time)
        val zone = TimeZone.currentSystemDefault()
        val timestamp = localDateTime.toInstant(zone).toString()


        val entry = ExerciseEntry(
            name = state.name,
            sets = state.sets,
            reps = state.reps,
            weights = state.weights.map { it.toFloatOrNull() ?: 0f },
            useKg = state.useKg,
            day = state.day.toInt(),
            month = state.month.toInt(),
            year = state.year.toInt(),
            timestamp = timestamp,
            workout = state.workout
        )

        viewModelScope.launch {
            repo.saveOrReplaceExercise(entry, state.editMode)
        }
    }
}

data class ExAddUiState(
    val name: String = "",
    val sets: Int = 3,
    val reps: List<Int> = emptyList(),
    val weights: List<String> = emptyList(),
    val useKg: Boolean = true,
    val editMode: Boolean = false,
    val workout: String? = null,

    // Existing date fields
    val day: String = "",
    val month: String = "",
    val year: String = "",

    // New time fields
    val hour: String = "12",
    val minute: String = "00"
)
