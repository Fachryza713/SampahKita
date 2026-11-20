package com.sampahkita.app.ui.profil

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Nama anggota kelompok untuk dialog "Tentang SampahKita". Ganti dengan nama asli. */
val ANGGOTA_KELOMPOK = listOf(
    "Anggota 1 — NIM",
    "Anggota 2 — NIM",
    "Anggota 3 — NIM",
    "Anggota 4 — NIM",
)

/** "Rina Lestari" → "RL", "Budi" → "B". */
fun inisial(nama: String): String =
    nama.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }

class ProfilViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = (application as SampahKitaApp).container.authRepository

    val user: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Versi aplikasi dari manifest, mis. "1.0". */
    val versiAplikasi: String = runCatching {
        application.packageManager.getPackageInfo(application.packageName, 0).versionName
    }.getOrNull() ?: "-"

    fun logout(selesai: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            selesai()
        }
    }
}
