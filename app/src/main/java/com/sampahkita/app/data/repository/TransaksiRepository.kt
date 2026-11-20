package com.sampahkita.app.data.repository

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.ItemSetor
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
    /** Semua transaksi, urut dari yang terbaru; ikut berubah saat ada transaksi baru. */
    val riwayat: Flow<List<Transaksi>>

    suspend fun getRiwayat(): List<Transaksi> = riwayat.first()

    /** Riwayat milik satu nasabah saja, urut dari yang terbaru. */
    fun riwayatNasabah(nasabahId: String): Flow<List<Transaksi>> =
        riwayat.map { list -> list.filter { it.nasabahId == nasabahId } }

    /** Buat pengajuan pencairan baru berstatus MENUNGGU_VALIDASI. */
    suspend fun ajukanPencairan(
        nasabahId: String,
        nominal: Long,
        bankSampahId: String,
        keterangan: String,
    ): Transaksi

    /** Catat setoran yang sudah ditimbang petugas (status BERHASIL). */
    suspend fun catatSetoran(
        nasabahId: String,
        nominal: Long,
        rincian: List<ItemSetor>,
        bankSampahId: String,
        petugas: String,
        keterangan: String,
    ): Transaksi

    suspend fun setujuiPencairan(transaksiId: String, petugas: String): Transaksi?

    suspend fun tolakPencairan(transaksiId: String, petugas: String, alasan: String): Transaksi?
}

/** Transaksi disimpan di memori, diawali data dari [DummyData]. */
class FakeTransaksiRepository(
    private val sekarang: () -> LocalDateTime = { LocalDateTime.now() },
) : TransaksiRepository {

    private val semua = MutableStateFlow(DummyData.transaksi.map { it.copy(nasabahId = DummyData.nasabah.id) })
    private var nomorUrut = DummyData.transaksi.size

    override val riwayat: Flow<List<Transaksi>> = semua.map { list -> list.sortedByDescending { it.tanggal } }

    override suspend fun ajukanPencairan(
        nasabahId: String,
        nominal: Long,
        bankSampahId: String,
        keterangan: String,
    ): Transaksi = tambah(
        Transaksi(
            id = idBaru(),
            jenis = JenisTransaksi.PENCAIRAN,
            nominal = nominal,
            keterangan = keterangan,
            tanggal = waktuSekarang(),
            status = StatusTransaksi.MENUNGGU_VALIDASI,
            bankSampahId = bankSampahId,
            nasabahId = nasabahId,
        ),
    )

    override suspend fun catatSetoran(
        nasabahId: String,
        nominal: Long,
        rincian: List<ItemSetor>,
        bankSampahId: String,
        petugas: String,
        keterangan: String,
    ): Transaksi = tambah(
        Transaksi(
            id = idBaru(),
            jenis = JenisTransaksi.SETOR,
            nominal = nominal,
            keterangan = keterangan,
            tanggal = waktuSekarang(),
            status = StatusTransaksi.BERHASIL,
            rincian = rincian,
            petugas = petugas,
            bankSampahId = bankSampahId,
            nasabahId = nasabahId,
        ),
    )

    override suspend fun setujuiPencairan(transaksiId: String, petugas: String): Transaksi? =
        ubahPengajuan(transaksiId) { it.copy(status = StatusTransaksi.BERHASIL, petugas = petugas) }

    override suspend fun tolakPencairan(transaksiId: String, petugas: String, alasan: String): Transaksi? =
        ubahPengajuan(transaksiId) {
            it.copy(status = StatusTransaksi.DITOLAK, petugas = petugas, alasanDitolak = alasan)
        }

    /** Hanya pengajuan yang masih MENUNGGU_VALIDASI yang boleh diubah. */
    private fun ubahPengajuan(id: String, ubah: (Transaksi) -> Transaksi): Transaksi? {
        var hasil: Transaksi? = null
        semua.update { list ->
            list.map { t ->
                if (t.id == id && t.jenis == JenisTransaksi.PENCAIRAN && t.status == StatusTransaksi.MENUNGGU_VALIDASI) {
                    ubah(t).also { hasil = it }
                } else {
                    t
                }
            }
        }
        return hasil
    }

    private fun tambah(transaksi: Transaksi): Transaksi {
        semua.update { it + transaksi }
        return transaksi
    }

    private fun idBaru(): String {
        nomorUrut += 1
        return "t-" + nomorUrut.toString().padStart(3, '0')
    }

    private fun waktuSekarang(): String = sekarang().format(FORMAT_WAKTU)

    private companion object {
        val FORMAT_WAKTU: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
