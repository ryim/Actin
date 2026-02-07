package com.ryim.actin.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ryim.actin.ui.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

object ThemePreferences {
    val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
}

//suspend fun saveDarkMode(context: Context, enabled: Boolean) {
//    context.dataStore.edit { prefs ->
//        prefs[ThemePreferences.DARK_MODE] = enabled
//    }
//}
//
//fun loadDarkMode(context: Context) =
//    context.dataStore.data.map { prefs ->
//        prefs[ThemePreferences.DARK_MODE] ?: false
//    }

suspend fun saveThemeMode(context: Context, mode: ThemeMode) {
    context.dataStore.edit { prefs ->
        prefs[stringPreferencesKey("theme_mode")] = mode.name
    }
}

fun loadThemeMode(context: Context): Flow<ThemeMode> =
    context.dataStore.data.map { prefs ->
        prefs[stringPreferencesKey("theme_mode")]?.let { ThemeMode.valueOf(it) }
            ?: ThemeMode.SYSTEM
    }
