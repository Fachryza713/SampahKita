package com.sampahkita.app.ui.tabungan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.JenisTransaksi
import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.data.model.Transaksi
import com.sampahkita.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

enum class FilterTabungan(val label: String) {
    SEMUA("Semua"),
    SETOR("Setor"),
    PENCAIRAN("Pencairan"),
}

/** Satu kelompok bulan, mis. "2026-11" berisi transaksi November 2026. */
data class KelompokBulan(val bulan: YearMonth, val transaksi: List<Transaksi>)

data class TabunganUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val semua: List<Transaksi> = emptyList(),
    val filter: FilterTabungan = FilterTabungan.SEMUA,
    val terpilih: Transaksi? = null,
    val kategori: List<KategoriSampah> = emptyList(),
    val bankList: List<BankSampah> = emptyList(),
    val bulanIni: YearMonth = YearMonth.now(),
) {
    val kelompok: List<KelompokBulan>
        get() = kelompokkanPerBulan(
            semua.filter {
                when (filter) {
                    FilterTabungan.SEMUA -> true
                    FilterTabungan.SETOR -> it.jenis == JenisTransaksi.SETOR
                    FilterTabungan.PENCAIRAN -> it.jenis == JenisTransaksi.PENCAIRAN
                }
            },
        )

    val setoranBulanIni: Int
        get() = jumlahSetoranBulan(semua, bulanIni)

    fun kategoriById(id: String) = kategori.firstOrNull { it.id == id }

    fun bankById(id: String?) = bankList.firstOrNull { it.id == id }
}

/** Urutkan terbaru dulu, lalu kelompokkan per bulan. Tanggal berformat "yyyy-MM-dd HH:mm". */
fun kelompokkanPerBulan(transaksi: List<Transaksi>): List<KelompokBulan> =
    transaksi
        .sortedByDescending { it.tanggal }
        .groupBy { YearMonth.parse(it.tanggal.take(7)) }
        .map { (bulan, list) -> KelompokBulan(bulan, list) }

fun jumlahSetoranBulan(transaksi: List<Transaksi>, bulan: YearMonth): Int =
    transaksi.count { it.jenis == JenisTransaksi.SETOR && it.tanggal.startsWith(bulan.toString()) }

class TabunganViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SampahKitaApp).container

    private val _uiState = MutableStateFlow(TabunganUiState())
    val uiState: StateFlow<TabunganUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = container.authRepository.currentUser.first()
            val kategori = container.kategoriRepository.getKategori()
            val bankList = container.bankSampahRepository.getBankSampah()
            _uiState.update { it.copy(user = user, kategori = kategori, bankList = bankList) }
        }
        // Ikuti perubahan riwayat (mis. pengajuan pencairan baru dari fitur 09).
        viewModelScope.launch {
            val nasabahId = container.authRepository.currentUser.first()?.id.orEmpty()
            container.transaksiRepository.riwayatNasabah(nasabahId).collect { transaksi ->
                _uiState.update { it.copy(isLoading = false, semua = transaksi) }
            }
        }
    }

    fun pilihFilter(filter: FilterTabungan) = _uiState.update { it.copy(filter = filter) }

    fun pilihTransaksi(transaksi: Transaksi?) = _uiState.update { it.copy(terpilih = transaksi) }
}
