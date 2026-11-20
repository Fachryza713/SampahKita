package com.sampahkita.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Menyimpan id user yang sedang login. */
class SessionPreferences(private val context: Context) {

    val userId: Flow<String?> = context.pengaturanDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_USER_ID] }

    suspend fun saveUserId(id: String) {
        context.pengaturanDataStore.edit { it[KEY_USER_ID] = id }
    }

    suspend fun clear() {
        context.pengaturanDataStore.edit { it.remove(KEY_USER_ID) }
    }

    private companion object {
        val KEY_USER_ID = stringPreferencesKey("session_user_id")
    }
}
