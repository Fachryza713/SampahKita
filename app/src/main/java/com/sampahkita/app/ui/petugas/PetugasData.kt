package com.sampahkita.app.ui.petugas

import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.JenisTransaksi
import com.sampahkita.app.data.model.StatusTransaksi
import com.sampahkita.app.data.model.Transaksi
import com.sampahkita.app.data.model.User
import java.time.LocalDate

/** Pengajuan pencairan yang menunggu validasi, siap ditampilkan. */
data class PengajuanItem(
    val transaksi: Transaksi,
    val namaNasabah: String,
    val namaBank: String,
)

/** Statistik setoran di satu unit pada satu hari. */
data class StatHarian(
    val jumlahSetoran: Int = 0,
    val totalKg: Double = 0.0,
    val totalRupiah: Long = 0,
)

fun pengajuanMenunggu(
    transaksi: List<Transaksi>,
    users: List<User>,
    banks: List<BankSampah>,
): List<PengajuanItem> =
    transaksi
        .filter { it.jenis == JenisTransaksi.PENCAIRAN && it.status == StatusTransaksi.MENUNGGU_VALIDASI }
        .sortedBy { it.tanggal } // yang paling lama menunggu di atas
        .map { t ->
            PengajuanItem(
                transaksi = t,
                namaNasabah = users.firstOrNull { it.id == t.nasabahId }?.nama ?: "Nasabah",
                namaBank = banks.firstOrNull { it.id == t.bankSampahId }?.nama ?: "-",
            )
        }

fun statHarian(transaksi: List<Transaksi>, bankSampahId: String?, tanggal: LocalDate): StatHarian {
    val setoran = transaksi.filter {
        it.jenis == JenisTransaksi.SETOR &&
            it.status == StatusTransaksi.BERHASIL &&
            it.bankSampahId == bankSampahId &&
            it.tanggal.startsWith(tanggal.toString())
    }
    return StatHarian(
        jumlahSetoran = setoran.size,
        totalKg = setoran.sumOf { t -> t.rincian.sumOf { it.beratAktualKg ?: it.perkiraanKg } },
        totalRupiah = setoran.sumOf { it.nominal },
    )
}
