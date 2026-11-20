package com.sampahkita.app.data.auth

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.User
import com.sampahkita.app.data.preferences.SessionPreferences
import com.sampahkita.app.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Autentikasi palsu berbasis [DummyData]; sesi (id user) disimpan di DataStore. */
class FakeAuthRepository(
    private val session: SessionPreferences,
    private val userRepository: UserRepository,
) : AuthRepository {

    /** Selalu berisi data user terbaru (mis. saldo setelah setoran). */
    override val currentUser: Flow<User?> =
        combine(session.userId, userRepository.semuaUser) { id, users ->
            id?.let { users.firstOrNull { u -> u.id == it } }
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
        return Result.success(userRepository.getUser(akun.user.id) ?: akun.user)
    }

    override suspend fun loginWithGoogle(): Result<User> {
        // TODO: ganti dengan Credential Manager + Firebase Auth
        delay(1_000)
        val id = DummyData.nasabah.id
        session.saveUserId(id)
        return Result.success(userRepository.getUser(id) ?: DummyData.nasabah)
    }

    override suspend fun logout() {
        session.clear()
    }
}
