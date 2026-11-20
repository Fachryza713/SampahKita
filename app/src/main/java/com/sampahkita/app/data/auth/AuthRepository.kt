package com.sampahkita.app.data.auth

import com.sampahkita.app.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** User yang sedang login, atau null jika belum login. */
    val currentUser: Flow<User?>

    /** @param identifier username atau email. */
    suspend fun login(identifier: String, password: String): Result<User>

    suspend fun loginWithGoogle(): Result<User>

    suspend fun logout()
}

/** Error autentikasi dengan pesan siap tampil ke pengguna. */
class AuthException(message: String) : Exception(message)
