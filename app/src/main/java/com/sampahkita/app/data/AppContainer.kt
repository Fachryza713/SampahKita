package com.sampahkita.app.data

import android.content.Context
import com.sampahkita.app.data.auth.AuthRepository
import com.sampahkita.app.data.auth.FakeAuthRepository
import com.sampahkita.app.data.preferences.SessionPreferences

/**
 * Tempat membuat repository satu kali untuk seluruh aplikasi.
 * Ganti implementasi Fake... di sini saat backend asli siap.
 */
class AppContainer(context: Context) {
    val authRepository: AuthRepository = FakeAuthRepository(SessionPreferences(context))
}
