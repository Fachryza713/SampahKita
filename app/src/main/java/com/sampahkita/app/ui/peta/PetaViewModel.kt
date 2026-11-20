package com.sampahkita.app.ui.peta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.isBukaPada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

/** Bank Sampah beserta status buka saat ini. */
data class BankSampahItem(val bank: BankSampah, val isBuka: Boolean)

data class PetaUiState(
    val query: String = "",
    /** Urutkan dari yang terdekat (jika mati: urut nama A–Z). */
    val filterTerdekat: Boolean = true,
    val filterBukaSekarang: Boolean = false,
    val semua: List<BankSampahItem> = emptyList(),
    val terpilih: BankSampahItem? = null,
) {
    /** Daftar setelah pencarian dan filter diterapkan. */
    val hasil: List<BankSampahItem>
        get() {
            val q = query.trim()
            return semua
                .filter { q.isEmpty() || it.bank.nama.contains(q, true) || it.bank.alamat.contains(q, true) }
                .filter { !filterBukaSekarang || it.isBuka }
                .let { list ->
                    if (filterTerdekat) list.sortedBy { it.bank.jarakKm } else list.sortedBy { it.bank.nama }
                }
        }
}

class PetaViewModel(application: Application) : AndroidViewModel(application) {

    private val bankSampahRepository = (application as SampahKitaApp).container.bankSampahRepository

    private val _uiState = MutableStateFlow(PetaUiState())
    val uiState: StateFlow<PetaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val sekarang = LocalTime.now()
            val semua = bankSampahRepository.getBankSampah().map { BankSampahItem(it, it.isBukaPada(sekarang)) }
            _uiState.update { it.copy(semua = semua) }
        }
    }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }

    fun toggleTerdekat() = _uiState.update { it.copy(filterTerdekat = !it.filterTerdekat) }

    fun toggleBukaSekarang() = _uiState.update { it.copy(filterBukaSekarang = !it.filterBukaSekarang) }

    fun pilih(item: BankSampahItem?) = _uiState.update { it.copy(terpilih = item) }
}
