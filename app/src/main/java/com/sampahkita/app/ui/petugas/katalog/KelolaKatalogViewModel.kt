package com.sampahkita.app.ui.petugas.katalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.ui.common.formatRupiah
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val HARGA_MAKS: Long = 1_000_000

/** null jika valid; selain itu pesan error. */
fun validasiHarga(teks: String): String? {
    val harga = teks.toLongOrNull()
    return when {
        harga == null || harga <= 0 -> "Harga harus lebih dari Rp0"
        harga > HARGA_MAKS -> "Harga maksimal ${formatRupiah(HARGA_MAKS)}/kg"
        else -> null
    }
}

data class KelolaKatalogUiState(
    val unit: BankSampah? = null,
    /** Harga tersimpan saat ini. */
    val kategori: List<KategoriSampah> = emptyList(),
    /** Perubahan yang belum disimpan: kategoriId → harga baru. */
    val draft: Map<String, Long> = emptyMap(),
    /** Kategori yang sedang diedit di dialog. */
    val sedangDiedit: KategoriSampah? = null,
    val inputHarga: String = "",
    val isMenyimpan: Boolean = false,
) {
    fun hargaTampil(k: KategoriSampah): Long = draft[k.id] ?: k.hargaPerKg

    /** Hanya hitung perubahan yang benar-benar berbeda dari harga tersimpan. */
    val adaPerubahan: Boolean
        get() = kategori.any { k -> draft[k.id]?.let { it != k.hargaPerKg } == true }

    val errorInput: String? get() = validasiHarga(inputHarga)
}

class KelolaKatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SampahKitaApp).container

    private val _uiState = MutableStateFlow(KelolaKatalogUiState())
    val uiState: StateFlow<KelolaKatalogUiState> = _uiState.asStateFlow()

    private val _pesan = Channel<String>(Channel.BUFFERED)
    val pesan: Flow<String> = _pesan.receiveAsFlow()

    init {
        viewModelScope.launch {
            val petugas = container.authRepository.currentUser.first()
            val unit = container.bankSampahRepository.getBankSampah().firstOrNull { it.id == petugas?.bankSampahId }
            val kategori = container.kategoriRepository.getKategori(unit?.id)
            _uiState.update { it.copy(unit = unit, kategori = kategori) }
        }
    }

    fun mulaiEdit(k: KategoriSampah) = _uiState.update {
        it.copy(sedangDiedit = k, inputHarga = it.hargaTampil(k).toString())
    }

    fun onInputHarga(teks: String) = _uiState.update {
        it.copy(inputHarga = teks.filter(Char::isDigit).trimStart('0').take(7))
    }

    fun batalEdit() = _uiState.update { it.copy(sedangDiedit = null, inputHarga = "") }

    fun terapkanEdit() = _uiState.update { s ->
        val k = s.sedangDiedit ?: return@update s
        if (s.errorInput != null) return@update s
        s.copy(draft = s.draft + (k.id to s.inputHarga.toLong()), sedangDiedit = null, inputHarga = "")
    }

    fun simpan() {
        val s = _uiState.value
        val unit = s.unit ?: return
        if (!s.adaPerubahan || s.isMenyimpan) return
        _uiState.update { it.copy(isMenyimpan = true) }
        viewModelScope.launch {
            container.kategoriRepository.simpanHarga(unit.id, s.draft)
            val kategori = container.kategoriRepository.getKategori(unit.id)
            _uiState.update { it.copy(kategori = kategori, draft = emptyMap(), isMenyimpan = false) }
            _pesan.send("Harga hari ini disimpan")
        }
    }
}
