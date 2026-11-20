package com.sampahkita.app.ui.katalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.ui.common.formatAngkaKg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

const val LANGKAH_KG = 0.5
const val MAKS_KG = 100.0

data class KatalogUiState(
    val bankList: List<BankSampah> = emptyList(),
    val bankTerpilih: BankSampah? = null,
    /** Kategori dengan harga sesuai [bankTerpilih]. */
    val kategori: List<KategoriSampah> = emptyList(),
    /** kategoriId → perkiraan berat (kg), selalu 0..[MAKS_KG]. */
    val berat: Map<String, Double> = emptyMap(),
    /** kategoriId → teks di kolom input (boleh sedang diketik, mis. "1,"). */
    val inputTeks: Map<String, String> = emptyMap(),
    /** kategoriId yang inputnya sempat melebihi [MAKS_KG]. */
    val melebihiMaks: Set<String> = emptySet(),
) {
    /** Total estimasi, dihitung ulang setiap state berubah. */
    val totalEstimasi: Long
        get() = kategori.sumOf { k -> (berat[k.id] ?: 0.0) * k.hargaPerKg }.roundToLong()

    val itemTerisi: Map<String, Double>
        get() = berat.filterValues { it > 0.0 }
}

class KatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SampahKitaApp).container

    private val _uiState = MutableStateFlow(KatalogUiState())
    val uiState: StateFlow<KatalogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val bankList = container.bankSampahRepository.getBankSampah()
            _uiState.update { it.copy(bankList = bankList) }
            bankList.firstOrNull()?.let { pilihBank(it) }
        }
    }

    fun pilihBank(bank: BankSampah) {
        _uiState.update { it.copy(bankTerpilih = bank) }
        viewModelScope.launch {
            val kategori = container.kategoriRepository.getKategori(bank.id)
            _uiState.update { it.copy(kategori = kategori) }
        }
    }

    fun tambah(kategoriId: String) = ubahBerat(kategoriId) { it + LANGKAH_KG }

    fun kurangi(kategoriId: String) = ubahBerat(kategoriId) { it - LANGKAH_KG }

    /** Input diketik: terima angka dengan koma/titik desimal. */
    fun onInputBerat(kategoriId: String, teks: String) {
        val bersih = teks.filter { it.isDigit() || it == ',' || it == '.' }.take(5)
        val angka = bersih.replace(',', '.').toDoubleOrNull() ?: 0.0
        val melebihi = angka > MAKS_KG
        val berat = angka.coerceIn(0.0, MAKS_KG)
        _uiState.update { s ->
            s.copy(
                berat = s.berat + (kategoriId to berat),
                inputTeks = s.inputTeks + (kategoriId to if (melebihi) formatAngkaKg(MAKS_KG) else bersih),
                melebihiMaks = if (melebihi) s.melebihiMaks + kategoriId else s.melebihiMaks - kategoriId,
            )
        }
    }

    fun reset() = _uiState.update { it.copy(berat = emptyMap(), inputTeks = emptyMap(), melebihiMaks = emptySet()) }

    private fun ubahBerat(kategoriId: String, ubah: (Double) -> Double) {
        _uiState.update { s ->
            val baru = ubah(s.berat[kategoriId] ?: 0.0).coerceIn(0.0, MAKS_KG)
            s.copy(
                berat = s.berat + (kategoriId to baru),
                inputTeks = s.inputTeks + (kategoriId to if (baru == 0.0) "" else formatAngkaKg(baru)),
                melebihiMaks = s.melebihiMaks - kategoriId,
            )
        }
    }
}
