package com.sampahkita.app.ui.pencairan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.JenisTransaksi
import com.sampahkita.app.data.model.StatusTransaksi
import com.sampahkita.app.data.model.Transaksi
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

/** Nominal minimal satu kali pencairan. Ubah di sini saja. */
const val MIN_PENCAIRAN: Long = 10_000

/** Pilihan cepat nominal (selain "Semua saldo"). */
val NOMINAL_CEPAT = listOf(10_000L, 25_000L, 50_000L)

/** null jika valid; selain itu pesan error untuk ditampilkan. */
fun validasiNominal(nominal: Long?, saldoTersedia: Long): String? = when {
    nominal == null || nominal <= 0 -> "Masukkan nominal pencairan"
    nominal < MIN_PENCAIRAN -> "Minimal pencairan ${formatRupiah(MIN_PENCAIRAN)}"
    nominal > saldoTersedia -> "Melebihi saldo tersedia (${formatRupiah(saldoTersedia)})"
    else -> null
}

data class PencairanUiState(
    val saldo: Long = 0,
    /** Riwayat pencairan (semua status), terbaru dulu. */
    val pengajuan: List<Transaksi> = emptyList(),
    val bankList: List<BankSampah> = emptyList(),
    val bankTerpilih: BankSampah? = null,
    /** Hanya digit, tanpa pemisah ribuan. */
    val inputNominal: String = "",
    /** Tampilkan error validasi (setelah user mengetik atau menekan Ajukan). */
    val tampilkanError: Boolean = false,
    val konfirmasi: Boolean = false,
    val isMengirim: Boolean = false,
) {
    val nominal: Long? get() = inputNominal.toLongOrNull()

    /** Jumlah yang sedang diajukan dan belum divalidasi. */
    val sedangDiajukan: Long
        get() = pengajuan.filter { it.status == StatusTransaksi.MENUNGGU_VALIDASI }.sumOf { it.nominal }

    /** Saldo dikurangi pengajuan yang masih menunggu, agar tidak bisa mengajukan melebihi saldo. */
    val saldoTersedia: Long get() = (saldo - sedangDiajukan).coerceAtLeast(0)

    val error: String? get() = validasiNominal(nominal, saldoTersedia)

    val bisaAjukan: Boolean get() = error == null && bankTerpilih != null && !isMengirim
}

class PencairanViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SampahKitaApp).container
    private val transaksiRepository = container.transaksiRepository

    private val _uiState = MutableStateFlow(PencairanUiState())
    val uiState: StateFlow<PencairanUiState> = _uiState.asStateFlow()

    private val _pesan = Channel<String>(Channel.BUFFERED)
    /** Pesan sekali tampil untuk Snackbar. */
    val pesan: Flow<String> = _pesan.receiveAsFlow()

    init {
        viewModelScope.launch {
            val user = container.authRepository.currentUser.first()
            val bankList = container.bankSampahRepository.getBankSampah()
            _uiState.update {
                it.copy(saldo = user?.saldo ?: 0, bankList = bankList, bankTerpilih = bankList.firstOrNull())
            }
        }
        viewModelScope.launch {
            val nasabahId = container.authRepository.currentUser.first()?.id.orEmpty()
            transaksiRepository.riwayatNasabah(nasabahId).collect { list ->
                _uiState.update { s -> s.copy(pengajuan = list.filter { it.jenis == JenisTransaksi.PENCAIRAN }) }
            }
        }
    }

    fun onNominalChange(teks: String) = _uiState.update {
        it.copy(inputNominal = teks.filter(Char::isDigit).trimStart('0').take(9), tampilkanError = true)
    }

    fun pilihNominalCepat(nominal: Long) = _uiState.update {
        it.copy(inputNominal = nominal.toString(), tampilkanError = true)
    }

    fun pilihSemuaSaldo() = _uiState.update {
        it.copy(inputNominal = it.saldoTersedia.takeIf { s -> s > 0 }?.toString().orEmpty(), tampilkanError = true)
    }

    fun pilihBank(bank: BankSampah) = _uiState.update { it.copy(bankTerpilih = bank) }

    /** Tombol "Ajukan Pencairan": validasi dulu, lalu tampilkan dialog konfirmasi. */
    fun mintaKonfirmasi() = _uiState.update {
        if (it.bisaAjukan) it.copy(konfirmasi = true) else it.copy(tampilkanError = true)
    }

    fun batalKonfirmasi() = _uiState.update { it.copy(konfirmasi = false) }

    fun ajukan() {
        val s = _uiState.value
        val nominal = s.nominal ?: return
        val bank = s.bankTerpilih ?: return
        if (!s.bisaAjukan) return
        _uiState.update { it.copy(konfirmasi = false, isMengirim = true) }
        viewModelScope.launch {
            transaksiRepository.ajukanPencairan(
                nasabahId = container.authRepository.currentUser.first()?.id.orEmpty(),
                nominal = nominal,
                bankSampahId = bank.id,
                keterangan = "Pencairan tunai di ${bank.nama}",
            )
            _uiState.update { it.copy(isMengirim = false, inputNominal = "", tampilkanError = false) }
            _pesan.send("Pengajuan ${formatRupiah(nominal)} terkirim. Menunggu validasi petugas.")
        }
    }
}
