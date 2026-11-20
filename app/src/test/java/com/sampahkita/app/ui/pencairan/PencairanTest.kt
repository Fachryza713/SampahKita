package com.sampahkita.app.ui.pencairan

import com.sampahkita.app.data.model.JenisTransaksi
import com.sampahkita.app.data.model.StatusTransaksi
import com.sampahkita.app.data.repository.FakeTransaksiRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class PencairanTest {

    @Test
    fun validasiNominal() {
        assertNotNull(validasiNominal(null, 45_500))
        assertNotNull(validasiNominal(0, 45_500))
        assertNotNull(validasiNominal(MIN_PENCAIRAN - 1, 45_500)) // di bawah minimal
        assertNotNull(validasiNominal(45_501, 45_500)) // melebihi saldo
        assertNull(validasiNominal(MIN_PENCAIRAN, 45_500))
        assertNull(validasiNominal(45_500, 45_500))
    }

    @Test
    fun saldoTersediaDikurangiPengajuanMenunggu() = runBlocking {
        val repo = FakeTransaksiRepository(sekarang = { LocalDateTime.of(2026, 11, 20, 23, 30) })
        repo.ajukanPencairan(25_000, "bs-01", "Pencairan tunai")
        val pencairan = repo.riwayat.first().filter { it.jenis == JenisTransaksi.PENCAIRAN }
        val state = PencairanUiState(saldo = 45_500, pengajuan = pencairan)

        assertEquals(StatusTransaksi.MENUNGGU_VALIDASI, pencairan.first().status) // terbaru di atas
        assertEquals(25_000L, state.sedangDiajukan)
        assertEquals(20_500L, state.saldoTersedia)
    }
}
