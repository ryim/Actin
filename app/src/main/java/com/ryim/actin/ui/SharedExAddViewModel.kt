package com.ryim.actin.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class SharedExAddViewModel @Inject constructor() : ViewModel() {

    private val _prefill = MutableStateFlow<ExAddPrefill?>(null)
    val prefill = _prefill.asStateFlow()

    fun setPrefill(prefill: ExAddPrefill) {
        _prefill.value = prefill
    }

    fun clearPrefill() {
        _prefill.value = null
    }
}

data class ExAddPrefill(
    val listOfExercises: List<String> = emptyList(),
    val name: String,
    val sets: Int,
    val reps: List<Int>,
    val weights: List<String>,
    val useKg: Boolean,
    val editMode: Boolean,
    val timestamp: String?,
    val workout: String?,
    val id: String
)
