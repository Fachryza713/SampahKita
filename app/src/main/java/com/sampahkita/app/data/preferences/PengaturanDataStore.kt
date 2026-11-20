package com.sampahkita.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/** Satu DataStore untuk semua preferensi kecil aplikasi (tema, sesi, dll.). */
internal val Context.pengaturanDataStore: DataStore<Preferences> by preferencesDataStore(name = "pengaturan")
