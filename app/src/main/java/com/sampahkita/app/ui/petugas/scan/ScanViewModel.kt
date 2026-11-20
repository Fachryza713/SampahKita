package com.sampahkita.app.ui.petugas.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.ItemSetor
import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.data.model.StatusTiket
import com.sampahkita.app.data.model.TiketSetor
import com.sampahkita.app.ui.common.formatAngkaKg
import com.sampahkita.app.ui.common.formatKg
import com.sampahkita.app.ui.common.formatTanggal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

const val MAKS_BERAT_KG = 100.0

private val FORMAT_WAKTU: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

/**
 * Periksa apakah tiket boleh diproses di unit ini.
 * @return null jika boleh; selain itu pesan error yang jelas.
 */
fun periksaTiket(
    tiket: TiketSetor?,
    kode: String,
    unitBankId: String?,
    namaBankTiket: String?,
    sekarang: LocalDateTime,
): String? = when {
    tiket == null -> "Tiket dengan kode $kode tidak ditemukan. Periksa kembali kodenya."
    tiket.status == StatusTiket.SELESAI -> "Tiket ${tiket.kodeQr} sudah dipakai — setoran sudah dikonfirmasi sebelumnya."
    tiket.status == StatusTiket.BATAL -> "Tiket ${tiket.kodeQr} sudah dibatalkan oleh nasabah."
    tiket.status == StatusTiket.KEDALUWARSA || sudahLewat(tiket.berlakuSampai, sekarang) ->
        "Tiket ${tiket.kodeQr} sudah kedaluwarsa (berlaku s.d. ${formatTanggal(tiket.berlakuSampai)})."
    unitBankId != null && tiket.bankSampahId != unitBankId ->
        "Tiket ini untuk ${namaBankTiket ?: "Bank Sampah lain"}, bukan unit Anda."
    else -> null
}

private fun sudahLewat(berlakuSampai: String, sekarang: LocalDateTime): Boolean =
    runCatching { sekarang.isAfter(LocalDateTime.parse(berlakuSampai, FORMAT_WAKTU)) }.getOrDefault(false)

/** "1,5" / "1.5" → 1.5; null jika kosong/tidak valid/di luar (0, 100]. */
fun parseBerat(teks: String): Double? =
    teks.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 && it <= MAKS_BERAT_KG }

sealed interface HasilScan {
    data object Kosong : HasilScan
    data class Gagal(val pesan: String) : HasilScan
    data class Ditemukan(
        val tiket: TiketSetor,
        val namaNasabah: String,
        /** Harga hari ini di unit (sesuai bank tiket). */
        val kategori: List<KategoriSampah>,
    ) : HasilScan
    data class Sukses(val kode: String, val namaNasabah: String, val totalKg: Double, val nominal: Long) : HasilScan
}

data class ScanUiState(
    val kodeInput: String = "",
    val isMencari: Boolean = false,
    val hasil: HasilScan = HasilScan.Kosong,
    /** kategoriId → teks berat aktual yang diketik petugas. */
    val beratInput: Map<String, String> = emptyMap(),
    val isMenyimpan: Boolean = false,
) {
    private val ditemukan get() = hasil as? HasilScan.Ditemukan

    /** kategoriId → berat aktual valid (null jika ada input tidak valid). */
    val beratValid: Map<String, Double>?
        get() {
            val t = ditemukan?.tiket ?: return null
            val hasil = t.items.associate { it.kategoriId to parseBerat(beratInput[it.kategoriId].orEmpty()) }
            return if (hasil.values.all { it != null }) hasil.mapValues { it.value!! } else null
        }

    val total: Long
        get() {
            val d = ditemukan ?: return 0
            return d.tiket.items.sumOf { item ->
                val kg = parseBerat(beratInput[item.kategoriId].orEmpty()) ?: 0.0
                val harga = d.kategori.firstOrNull { it.id == item.kategoriId }?.hargaPerKg ?: 0
                kg * harga
            }.roundToLong()
        }

    val bisaKonfirmasi: Boolean get() = beratValid != null && !isMenyimpan
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SampahKitaApp).container

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onKodeChange(teks: String) = _uiState.update { it.copy(kodeInput = teks.uppercase().take(24)) }

    fun cari() {
        val kode = _uiState.value.kodeInput.trim()
        if (kode.isEmpty()) {
            _uiState.update { it.copy(hasil = HasilScan.Gagal("Masukkan kode tiket terlebih dahulu.")) }
            return
        }
        _uiState.update { it.copy(isMencari = true) }
        viewModelScope.launch {
            val petugas = container.authRepository.currentUser.first()
            val tiket = container.tiketRepository.cariByKode(kode)
            val banks = container.bankSampahRepository.getBankSampah()
            val error = periksaTiket(
                tiket = tiket,
                kode = kode,
                unitBankId = petugas?.bankSampahId,
                namaBankTiket = banks.firstOrNull { it.id == tiket?.bankSampahId }?.nama,
                sekarang = LocalDateTime.now(),
            )
            if (tiket == null || error != null) {
                _uiState.update { it.copy(isMencari = false, hasil = HasilScan.Gagal(error.orEmpty())) }
                return@launch
            }
            val nasabah = container.userRepository.getUser(tiket.nasabahId)
            val kategori = container.kategoriRepository.getKategori(tiket.bankSampahId)
            _uiState.update {
                it.copy(
                    isMencari = false,
                    hasil = HasilScan.Ditemukan(tiket, nasabah?.nama ?: "Nasabah", kategori),
                    // Isi awal dengan perkiraan berat; petugas mengganti dengan hasil timbangan.
                    beratInput = tiket.items.associate { i -> i.kategoriId to formatAngkaKg(i.perkiraanKg) },
                )
            }
        }
    }

    fun onBeratChange(kategoriId: String, teks: String) = _uiState.update {
        val bersih = teks.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(5)
        it.copy(beratInput = it.beratInput + (kategoriId to bersih))
    }

    fun konfirmasiSetoran() {
        val s = _uiState.value
        val d = s.hasil as? HasilScan.Ditemukan ?: return
        val berat = s.beratValid ?: return
        if (s.isMenyimpan) return
        _uiState.update { it.copy(isMenyimpan = true) }
        viewModelScope.launch {
            // Cek ulang: tiket mungkin sudah dipakai/dibatalkan sejak dicari.
            val terbaru = container.tiketRepository.cariByKode(d.tiket.kodeQr)
            if (terbaru?.status != StatusTiket.MENUNGGU) {
                _uiState.update {
                    it.copy(isMenyimpan = false, hasil = HasilScan.Gagal("Status tiket berubah. Silakan cari ulang."))
                }
                return@launch
            }
            val petugas = container.authRepository.currentUser.first()
            val nominal = s.total
            val totalKg = berat.values.sum()
            val rincian = d.tiket.items.map { ItemSetor(it.kategoriId, it.perkiraanKg, berat[it.kategoriId]) }
            val keterangan = "Setor " + rincian.joinToString(" & ") { item ->
                val nama = d.kategori.firstOrNull { it.id == item.kategoriId }?.nama ?: item.kategoriId
                "$nama ${formatKg(item.beratAktualKg ?: item.perkiraanKg)}"
            }

            container.tiketRepository.selesaikan(d.tiket.id, berat)
            container.transaksiRepository.catatSetoran(
                nasabahId = d.tiket.nasabahId,
                nominal = nominal,
                rincian = rincian,
                bankSampahId = d.tiket.bankSampahId,
                petugas = petugas?.nama ?: "Petugas",
                keterangan = keterangan,
            )
            container.userRepository.ubahSaldo(d.tiket.nasabahId, nominal, totalKg)

            _uiState.update {
                it.copy(
                    isMenyimpan = false,
                    hasil = HasilScan.Sukses(d.tiket.kodeQr, d.namaNasabah, totalKg, nominal),
                )
            }
        }
    }

    /** Kembali ke kondisi awal untuk memindai tiket berikutnya. */
    fun reset() = _uiState.update { ScanUiState() }
}
