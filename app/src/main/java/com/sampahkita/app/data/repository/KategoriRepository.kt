package com.sampahkita.app.data.repository

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.KategoriSampah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToLong

interface KategoriRepository {
    /**
     * Daftar kategori sampah beserta harga hari ini.
     * @param bankSampahId jika diisi, harga mengikuti Bank Sampah tersebut.
     */
    suspend fun getKategori(bankSampahId: String? = null): List<KategoriSampah>

    /**
     * Simpan harga hari ini untuk satu Bank Sampah (kategoriId → harga per kg).
     * Dummy: disimpan sebagai harga dasar sehingga semua halaman ikut berubah.
     */
    suspend fun simpanHarga(bankSampahId: String, harga: Map<String, Long>)
}

class FakeKategoriRepository : KategoriRepository {

    /** Harga dasar per kategori (faktor bank = 1,0). */
    private val hargaDasar = MutableStateFlow(DummyData.kategori)

    override suspend fun getKategori(bankSampahId: String?): List<KategoriSampah> {
        val faktor = faktor(bankSampahId)
        return hargaDasar.value.map { it.copy(hargaPerKg = bulatkan50(it.hargaPerKg * faktor)) }
    }

    override suspend fun simpanHarga(bankSampahId: String, harga: Map<String, Long>) {
        val faktor = faktor(bankSampahId)
        hargaDasar.update { list ->
            list.map { k -> harga[k.id]?.let { k.copy(hargaPerKg = (it / faktor).roundToLong()) } ?: k }
        }
    }

    private fun faktor(bankSampahId: String?): Double =
        bankSampahId?.let { DummyData.faktorHargaBank[it] } ?: 1.0

    /** Bulatkan ke kelipatan Rp50 terdekat. */
    private fun bulatkan50(harga: Double): Long = (harga / 50).roundToLong() * 50
}
