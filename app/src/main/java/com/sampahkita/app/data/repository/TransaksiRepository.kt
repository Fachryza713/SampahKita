package com.sampahkita.app.data.repository

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.JenisTransaksi
import com.sampahkita.app.data.model.StatusTransaksi
import com.sampahkita.app.data.model.Transaksi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface TransaksiRepository {
    /** Riwayat transaksi user yang login, urut dari yang terbaru; ikut berubah saat ada transaksi baru. */
    val riwayat: Flow<List<Transaksi>>

    suspend fun getRiwayat(): List<Transaksi> = riwayat.first()

    /** Buat pengajuan pencairan baru berstatus MENUNGGU_VALIDASI. */
    suspend fun ajukanPencairan(nominal: Long, bankSampahId: String, keterangan: String): Transaksi
}

/** Transaksi disimpan di memori, diawali data dari [DummyData]. */
class FakeTransaksiRepository(
    private val sekarang: () -> LocalDateTime = { LocalDateTime.now() },
) : TransaksiRepository {

    private val semua = MutableStateFlow(DummyData.transaksi)
    private var nomorUrut = DummyData.transaksi.size

    override val riwayat: Flow<List<Transaksi>> = semua.map { list -> list.sortedByDescending { it.tanggal } }

    override suspend fun ajukanPencairan(nominal: Long, bankSampahId: String, keterangan: String): Transaksi {
        nomorUrut += 1
        val transaksi = Transaksi(
            id = "t-" + nomorUrut.toString().padStart(3, '0'),
            jenis = JenisTransaksi.PENCAIRAN,
            nominal = nominal,
            keterangan = keterangan,
            tanggal = sekarang().format(FORMAT_WAKTU),
            status = StatusTransaksi.MENUNGGU_VALIDASI,
            bankSampahId = bankSampahId,
        )
        semua.update { it + transaksi }
        return transaksi
    }

    private companion object {
        val FORMAT_WAKTU: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
