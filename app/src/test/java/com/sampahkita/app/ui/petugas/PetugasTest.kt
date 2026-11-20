package com.sampahkita.app.ui.petugas

import com.sampahkita.app.data.model.ItemSetor
import com.sampahkita.app.data.model.StatusTiket
import com.sampahkita.app.data.model.StatusTransaksi
import com.sampahkita.app.data.model.TiketSetor
import com.sampahkita.app.data.repository.FakeKategoriRepository
import com.sampahkita.app.data.repository.FakeTransaksiRepository
import com.sampahkita.app.data.repository.FakeUserRepository
import com.sampahkita.app.ui.petugas.katalog.validasiHarga
import com.sampahkita.app.ui.petugas.scan.parseBerat
import com.sampahkita.app.ui.petugas.scan.periksaTiket
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class PetugasTest {

    private val sekarang = LocalDateTime.of(2026, 11, 20, 23, 30)
    private val tiket = TiketSetor(
        id = "tk-12", kodeQr = "SK-20261120-0012", bankSampahId = "bs-01",
        items = listOf(ItemSetor("k-01", 1.5)), status = StatusTiket.MENUNGGU,
        dibuat = "2026-11-20 23:10", nasabahId = "u-001",
    )

    @Test
    fun periksaTiketValidDanSemuaKasusError() {
        assertNull(periksaTiket(tiket, tiket.kodeQr, "bs-01", null, sekarang))
        assertTrue(periksaTiket(null, "SK-X", "bs-01", null, sekarang)!!.contains("tidak ditemukan"))
        assertTrue(periksaTiket(tiket.copy(status = StatusTiket.SELESAI), "", "bs-01", null, sekarang)!!.contains("sudah dipakai"))
        assertTrue(periksaTiket(tiket.copy(status = StatusTiket.BATAL), "", "bs-01", null, sekarang)!!.contains("dibatalkan"))
        assertTrue(periksaTiket(tiket, "", "bs-01", null, sekarang.plusDays(2))!!.contains("kedaluwarsa"))
        assertTrue(periksaTiket(tiket, "", "bs-02", "Bank Sampah Lestari Pogung", sekarang)!!.contains("bukan unit Anda"))
    }

    @Test
    fun parseBeratTerimaKomaDanBatasi() {
        assertEquals(1.5, parseBerat("1,5")!!, 0.0)
        assertEquals(2.0, parseBerat("2.0")!!, 0.0)
        assertNull(parseBerat("0"))
        assertNull(parseBerat("100,5"))
        assertNull(parseBerat(""))
    }

    @Test
    fun validasiHargaKatalog() {
        assertNull(validasiHarga("2500"))
        assertNotNull(validasiHarga("0"))
        assertNotNull(validasiHarga(""))
        assertNotNull(validasiHarga("1000001"))
    }

    @Test
    fun setoranTercatatDiStatistikHarian() = runBlocking {
        val repo = FakeTransaksiRepository(sekarang = { sekarang })
        repo.catatSetoran("u-001", 7_750, listOf(ItemSetor("k-01", 1.5, 1.5), ItemSetor("k-02", 1.0, 1.0)), "bs-01", "Budi", "Setor")
        val stat = statHarian(repo.riwayat.first(), "bs-01", LocalDate.of(2026, 11, 20))
        assertEquals(1, stat.jumlahSetoran)
        assertEquals(2.5, stat.totalKg, 0.001)
        assertEquals(7_750L, stat.totalRupiah)
        // Unit lain tidak ikut terhitung
        assertEquals(0, statHarian(repo.riwayat.first(), "bs-02", LocalDate.of(2026, 11, 20)).jumlahSetoran)
    }

    @Test
    fun setujuiDanTolakHanyaUntukPengajuanMenunggu() = runBlocking {
        val repo = FakeTransaksiRepository(sekarang = { sekarang })
        val a = repo.ajukanPencairan("u-001", 10_000, "bs-01", "Pencairan")
        val b = repo.ajukanPencairan("u-001", 15_000, "bs-01", "Pencairan")

        assertEquals(StatusTransaksi.BERHASIL, repo.setujuiPencairan(a.id, "Budi")?.status)
        assertNull(repo.setujuiPencairan(a.id, "Budi")) // tidak bisa disetujui dua kali
        val ditolak = repo.tolakPencairan(b.id, "Budi", "Kas kosong")
        assertEquals(StatusTransaksi.DITOLAK, ditolak?.status)
        assertEquals("Kas kosong", ditolak?.alasanDitolak)

        val users = FakeUserRepository()
        assertEquals(0, pengajuanMenunggu(repo.riwayat.first(), users.semuaUser.first(), emptyList()).size)
    }

    @Test
    fun saldoNasabahBerubah() = runBlocking {
        val users = FakeUserRepository()
        users.ubahSaldo("u-001", 7_750, 2.5)
        users.ubahSaldo("u-001", -10_000)
        val u = users.getUser("u-001")!!
        assertEquals(43_250L, u.saldo)
        assertEquals(14.9, u.totalKg, 0.001)
    }

    @Test
    fun simpanHargaMengubahKatalog() = runBlocking {
        val repo = FakeKategoriRepository()
        repo.simpanHarga("bs-01", mapOf("k-01" to 3_000L))
        assertEquals(3_000L, repo.getKategori("bs-01").first { it.id == "k-01" }.hargaPerKg)
        assertEquals(3_000L, repo.getKategori().first { it.id == "k-01" }.hargaPerKg)
    }
}
