package com.sampahkita.app.ui.tiket

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.ItemSetor
import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.data.model.TiketSetor
import com.sampahkita.app.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

private const val LANGKAH_KG = 0.5
private const val MAKS_KG = 100.0
private const val BERAT_AWAL_KG = 1.0

data class TiketSetorUiState(
    val isLoading: Boolean = true,
    val bankList: List<BankSampah> = emptyList(),
    val bankTerpilih: BankSampah? = null,
    /** Kategori dengan harga sesuai [bankTerpilih]. */
    val kategori: List<KategoriSampah> = emptyList(),
    /** Kategori yang dipilih → perkiraan berat (kg). */
    val dipilih: Map<String, Double> = emptyMap(),
    val isMembuat: Boolean = false,
    /** Jika ada, tampilkan langkah 2 (Drop-Off Pass). */
    val tiketAktif: TiketSetor? = null,
) {
    val estimasi: Long
        get() = kategori.sumOf { k -> (dipilih[k.id] ?: 0.0) * k.hargaPerKg }.roundToLong()

    val bisaBuatTiket: Boolean
        get() = bankTerpilih != null && dipilih.isNotEmpty() && !isMembuat

    fun bankById(id: String): BankSampah? = bankList.firstOrNull { it.id == id }

    fun kategoriById(id: String): KategoriSampah? = kategori.firstOrNull { it.id == id }
}

class TiketSetorViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val container = (application as SampahKitaApp).container
    private val tiketRepository = container.tiketRepository

    private val _uiState = MutableStateFlow(TiketSetorUiState())
    val uiState: StateFlow<TiketSetorUiState> = _uiState.asStateFlow()

    init {
        // Argumen opsional dari Peta (bankSampahId) atau Katalog (bankSampahId + items).
        val bankIdAwal: String? = savedStateHandle[Screen.TiketSetor.ARG_BANK_SAMPAH_ID]
        val itemsAwal = Screen.TiketSetor.decodeItems(savedStateHandle[Screen.TiketSetor.ARG_ITEMS])
            .mapValues { it.value.coerceIn(LANGKAH_KG, MAKS_KG) }

        viewModelScope.launch {
            val bankList = container.bankSampahRepository.getBankSampah()
            val bank = bankList.firstOrNull { it.id == bankIdAwal } ?: bankList.firstOrNull()
            _uiState.update { it.copy(bankList = bankList, dipilih = itemsAwal, isLoading = false) }
            bank?.let { pilihBank(it) }
        }
        viewModelScope.launch {
            tiketRepository.tiketAktif.collect { tiket ->
                _uiState.update { it.copy(tiketAktif = tiket) }
                // Tampilkan harga sesuai bank pada tiket.
                tiket?.let { t -> _uiState.value.bankById(t.bankSampahId)?.let { pilihBank(it) } }
            }
        }
    }

    fun pilihBank(bank: BankSampah) {
        _uiState.update { it.copy(bankTerpilih = bank) }
        viewModelScope.launch {
            val kategori = container.kategoriRepository.getKategori(bank.id)
            _uiState.update { it.copy(kategori = kategori) }
        }
    }

    /** Chip multi-pilih: pilih dengan berat awal 1 kg, atau batalkan pilihan. */
    fun toggleKategori(kategoriId: String) = _uiState.update { s ->
        val baru = if (kategoriId in s.dipilih) s.dipilih - kategoriId else s.dipilih + (kategoriId to BERAT_AWAL_KG)
        s.copy(dipilih = baru)
    }

    fun tambah(kategoriId: String) = ubahBerat(kategoriId, +LANGKAH_KG)

    fun kurangi(kategoriId: String) = ubahBerat(kategoriId, -LANGKAH_KG)

    fun buatTiket() {
        val s = _uiState.value
        val bank = s.bankTerpilih ?: return
        if (!s.bisaBuatTiket) return
        _uiState.update { it.copy(isMembuat = true) }
        viewModelScope.launch {
            // Urutan item mengikuti urutan katalog.
            val items = s.kategori.mapNotNull { k ->
                s.dipilih[k.id]?.let { ItemSetor(kategoriId = k.id, perkiraanKg = it) }
            }
            tiketRepository.buatTiket(bank.id, items)
            _uiState.update { it.copy(isMembuat = false, dipilih = emptyMap()) }
        }
    }

    fun batalkanTiket() {
        val tiket = _uiState.value.tiketAktif ?: return
        viewModelScope.launch { tiketRepository.batalkan(tiket.id) }
    }

    private fun ubahBerat(kategoriId: String, delta: Double) = _uiState.update { s ->
        val sekarang = s.dipilih[kategoriId] ?: return@update s
        s.copy(dipilih = s.dipilih + (kategoriId to (sekarang + delta).coerceIn(LANGKAH_KG, MAKS_KG)))
    }

    companion object {
        const val MIN_KG = LANGKAH_KG
        const val MAX_KG = MAKS_KG
    }
}
