package com.sampahkita.app.data.auth

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.User
import com.sampahkita.app.data.preferences.SessionPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Autentikasi palsu berbasis [DummyData]; sesi (id user) disimpan di DataStore. */
class FakeAuthRepository(
    private val session: SessionPreferences,
) : AuthRepository {

    override val currentUser: Flow<User?> = session.userId.map { id ->
        id?.let { cariUserById(it) }
    }

    override suspend fun login(identifier: String, password: String): Result<User> {
        delay(800) // agar loading terlihat
        val id = identifier.trim()
        val akun = DummyData.akun.firstOrNull {
            it.user.username.equals(id, ignoreCase = true) ||
                it.user.email.equals(id, ignoreCase = true)
        }
        if (akun == null || akun.password != password) {
            return Result.failure(AuthException("Username/email atau password salah"))
        }
        session.saveUserId(akun.user.id)
        return Result.success(akun.user)
    }

    override suspend fun loginWithGoogle(): Result<User> {
        // TODO: ganti dengan Credential Manager + Firebase Auth
        delay(1_000)
        val user = DummyData.nasabah
        session.saveUserId(user.id)
        return Result.success(user)
    }

    override suspend fun logout() {
        session.clear()
    }

    private fun cariUserById(id: String): User? =
        DummyData.akun.firstOrNull { it.user.id == id }?.user
}
