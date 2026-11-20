package com.sampahkita.app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface LoginStatus {
    data object Idle : LoginStatus
    /** @param viaGoogle tombol mana yang menampilkan progress. */
    data class Loading(val viaGoogle: Boolean) : LoginStatus
    data class Error(val pesan: String) : LoginStatus
    data class Sukses(val user: User) : LoginStatus
}

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val passwordTerlihat: Boolean = false,
    val identifierError: String? = null,
    val passwordError: String? = null,
    val status: LoginStatus = LoginStatus.Idle,
) {
    val isLoading: Boolean get() = status is LoginStatus.Loading
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = (application as SampahKitaApp).container.authRepository

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifierChange(value: String) = _uiState.update {
        it.copy(identifier = value, identifierError = null, status = it.clearedError())
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, status = it.clearedError())
    }

    fun togglePasswordTerlihat() = _uiState.update { it.copy(passwordTerlihat = !it.passwordTerlihat) }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return
        val identifierError = if (state.identifier.isBlank()) "Username atau email wajib diisi" else null
        val passwordError = if (state.password.isEmpty()) "Password wajib diisi" else null
        if (identifierError != null || passwordError != null) {
            _uiState.update { it.copy(identifierError = identifierError, passwordError = passwordError) }
            return
        }
        _uiState.update { it.copy(status = LoginStatus.Loading(viaGoogle = false)) }
        viewModelScope.launch {
            val hasil = authRepository.login(state.identifier, state.password)
            _uiState.update { it.copy(status = hasil.toStatus()) }
        }
    }

    fun loginWithGoogle() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(status = LoginStatus.Loading(viaGoogle = true)) }
        viewModelScope.launch {
            val hasil = authRepository.loginWithGoogle()
            _uiState.update { it.copy(status = hasil.toStatus()) }
        }
    }

    private fun LoginUiState.clearedError(): LoginStatus =
        if (status is LoginStatus.Error) LoginStatus.Idle else status

    private fun Result<User>.toStatus(): LoginStatus = fold(
        onSuccess = { LoginStatus.Sukses(it) },
        onFailure = { LoginStatus.Error(it.message ?: "Gagal masuk, coba lagi") },
    )
}
