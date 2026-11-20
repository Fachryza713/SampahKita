package com.sampahkita.app.ui.tabungan

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.JenisTransaksi
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class TabunganTest {

    @Test
    fun transaksiDikelompokkanPerBulanTerbaruDulu() {
        val kelompok = kelompokkanPerBulan(DummyData.transaksi)
        assertEquals(
            listOf(YearMonth.of(2026, 11), YearMonth.of(2026, 10), YearMonth.of(2026, 9)),
            kelompok.map { it.bulan },
        )
        assertEquals(listOf(3, 4, 1), kelompok.map { it.transaksi.size })
    }

    @Test
    fun jumlahSetoranBulanIniHanyaMenghitungSetor() {
        assertEquals(2, jumlahSetoranBulan(DummyData.transaksi, YearMonth.of(2026, 11)))
        assertEquals(0, jumlahSetoranBulan(DummyData.transaksi, YearMonth.of(2026, 12)))
    }

    @Test
    fun filterSetorDanPencairan() {
        val state = TabunganUiState(semua = DummyData.transaksi, filter = FilterTabungan.PENCAIRAN)
        val hasil = state.kelompok.flatMap { it.transaksi }
        assertEquals(2, hasil.size)
        assertEquals(true, hasil.all { it.jenis == JenisTransaksi.PENCAIRAN })
    }

    @Test
    fun dataDummyKonsistenDenganSaldo() {
        val setor = DummyData.transaksi.filter { it.jenis == JenisTransaksi.SETOR }.sumOf { it.nominal }
        val cair = DummyData.transaksi.filter { it.jenis == JenisTransaksi.PENCAIRAN }.sumOf { it.nominal }
        assertEquals(DummyData.nasabah.saldo, setor - cair)
    }
}
