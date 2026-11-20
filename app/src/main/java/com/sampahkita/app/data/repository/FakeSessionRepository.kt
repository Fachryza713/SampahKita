package com.sampahkita.app.data.repository

import com.sampahkita.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sesi login sementara di memori (hilang saat aplikasi ditutup).
 * TODO: diganti AuthRepository + DataStore di fitur 03.
 */
object FakeSessionRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun setUser(user: User?) {
        _currentUser.value = user
    }
}
