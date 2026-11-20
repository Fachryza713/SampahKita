package com.sampahkita.app.data.repository

import com.sampahkita.app.data.model.ItemSetor
import com.sampahkita.app.data.model.StatusTiket
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class FakeTiketRepositoryTest {

    private val waktu = LocalDateTime.of(2026, 11, 20, 23, 10)
    private val repo = FakeTiketRepository(sekarang = { waktu })
    private val items = listOf(ItemSetor("k-01", 1.5))

    @Test
    fun kodeTiketDariTanggalDanNomorUrut() = runBlocking {
        assertEquals("SK-20261120-0012", repo.buatTiket("bs-01", items).kodeQr)
        assertEquals("SK-20261120-0013", repo.buatTiket("bs-01", items).kodeQr)
    }

    @Test
    fun tiketBaruMenjadiAktifDanBerlaku24Jam() = runBlocking {
        val tiket = repo.buatTiket("bs-02", items)
        val aktif = repo.tiketAktif.first()
        assertEquals(tiket.id, aktif?.id)
        assertEquals(StatusTiket.MENUNGGU, aktif?.status)
        assertEquals("2026-11-21 23:10", aktif?.berlakuSampai)
    }

    @Test
    fun tiketDibatalkanTidakLagiAktif() = runBlocking {
        val tiket = repo.buatTiket("bs-01", items)
        repo.batalkan(tiket.id)
        assertNull(repo.tiketAktif.first())
    }
}
