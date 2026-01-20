package com.ryim.actin.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryim.actin.domain.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingScreenViewModel @Inject constructor(
    private val repo: ExerciseRepository,
    private val app: Application
) : ViewModel() {

    fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            val json = repo.exportJson()

            val resolver = app.contentResolver
            resolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray())
            }
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            val resolver = app.contentResolver
            val json = resolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }

            if (json != null) {
                repo.importJson(json)
                // No loadHistory() here — Settings screen doesn't own history state
            }
        }
    }
}
