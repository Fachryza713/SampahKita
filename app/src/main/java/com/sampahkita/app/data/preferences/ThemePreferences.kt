package com.sampahkita.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class ThemePreferences(private val context: Context) {

    val themeMode: Flow<ThemeMode> = context.pengaturanDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            prefs[KEY_THEME_MODE]
                ?.let { nama -> ThemeMode.entries.firstOrNull { it.name == nama } }
                ?: ThemeMode.SISTEM
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.pengaturanDataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
