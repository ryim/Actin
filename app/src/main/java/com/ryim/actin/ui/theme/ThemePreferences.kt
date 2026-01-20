package com.ryim.actin.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

object ThemePreferences {
    val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
}

suspend fun saveDarkMode(context: Context, enabled: Boolean) {
    context.dataStore.edit { prefs ->
        prefs[ThemePreferences.DARK_MODE] = enabled
    }
}

fun loadDarkMode(context: Context) =
    context.dataStore.data.map { prefs ->
        prefs[ThemePreferences.DARK_MODE] ?: false
    }
