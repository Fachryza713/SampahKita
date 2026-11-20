package com.sampahkita.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.data.model.Transaksi
import com.sampahkita.app.data.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val user: User? = null,
    val kategori: List<KategoriSampah> = emptyList(),
    val bankTerdekat: BankSampah? = null,
    val transaksiTerakhir: List<Transaksi> = emptyList(),
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SampahKitaApp).container

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    /** Dipanggil oleh pull-to-refresh. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            delay(500) // simulasi jaringan
            val user = container.authRepository.currentUser.first()
            val kategori = container.kategoriRepository.getKategori()
            val bankTerdekat = container.bankSampahRepository.getBankSampah().firstOrNull()
            val transaksi = container.transaksiRepository.getRiwayat().take(3)
            _uiState.value = HomeUiState(
                isLoading = false,
                isRefreshing = false,
                user = user,
                kategori = kategori,
                bankTerdekat = bankTerdekat,
                transaksiTerakhir = transaksi,
            )
        }
    }
}
