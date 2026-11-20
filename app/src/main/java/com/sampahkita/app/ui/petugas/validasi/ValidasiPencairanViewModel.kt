package com.sampahkita.app.ui.petugas.validasi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.ui.common.formatRupiah
import com.sampahkita.app.ui.petugas.PengajuanItem
import com.sampahkita.app.ui.petugas.pengajuanMenunggu
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ValidasiUiState(
    val pengajuan: List<PengajuanItem> = emptyList(),
    /** Pengajuan yang sedang diisi alasan penolakannya. */
    val ditolak: PengajuanItem? = null,
    val alasan: String = "",
    /** id transaksi yang sedang diproses (tombol dinonaktifkan). */
    val diproses: String? = null,
)

class ValidasiPencairanViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SampahKitaApp).container
    private val banks = MutableStateFlow<List<BankSampah>>(emptyList())
    private val lokal = MutableStateFlow(ValidasiUiState())

    private val _pesan = Channel<String>(Channel.BUFFERED)
    val pesan: Flow<String> = _pesan.receiveAsFlow()

    init {
        viewModelScope.launch { banks.value = container.bankSampahRepository.getBankSampah() }
    }

    val uiState: StateFlow<ValidasiUiState> = combine(
        container.transaksiRepository.riwayat,
        container.userRepository.semuaUser,
        banks,
        lokal,
    ) { transaksi, users, bankList, l ->
        l.copy(pengajuan = pengajuanMenunggu(transaksi, users, bankList))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ValidasiUiState())

    fun setujui(item: PengajuanItem) {
        if (lokal.value.diproses != null) return
        lokal.update { it.copy(diproses = item.transaksi.id) }
        viewModelScope.launch {
            val t = item.transaksi
            val nasabah = container.userRepository.getUser(t.nasabahId)
            if (nasabah == null || nasabah.saldo < t.nominal) {
                _pesan.send("Saldo ${item.namaNasabah} tidak cukup untuk pencairan ${formatRupiah(t.nominal)}.")
            } else {
                val petugas = container.authRepository.currentUser.first()?.nama ?: "Petugas"
                val hasil = container.transaksiRepository.setujuiPencairan(t.id, petugas)
                if (hasil != null) {
                    // Saldo nasabah baru berkurang saat pencairan disetujui.
                    container.userRepository.ubahSaldo(t.nasabahId, -t.nominal)
                    _pesan.send("Pencairan ${formatRupiah(t.nominal)} untuk ${item.namaNasabah} disetujui.")
                }
            }
            lokal.update { it.copy(diproses = null) }
        }
    }

    fun mulaiTolak(item: PengajuanItem) = lokal.update { it.copy(ditolak = item, alasan = "") }

    fun onAlasanChange(teks: String) = lokal.update { it.copy(alasan = teks.take(120)) }

    fun batalTolak() = lokal.update { it.copy(ditolak = null, alasan = "") }

    fun konfirmasiTolak() {
        val l = lokal.value
        val item = l.ditolak ?: return
        val alasan = l.alasan.trim()
        if (alasan.isEmpty()) return
        lokal.update { it.copy(ditolak = null, alasan = "", diproses = item.transaksi.id) }
        viewModelScope.launch {
            val petugas = container.authRepository.currentUser.first()?.nama ?: "Petugas"
            container.transaksiRepository.tolakPencairan(item.transaksi.id, petugas, alasan)
            _pesan.send("Pengajuan ${item.namaNasabah} ditolak.")
            lokal.update { it.copy(diproses = null) }
        }
    }
}
