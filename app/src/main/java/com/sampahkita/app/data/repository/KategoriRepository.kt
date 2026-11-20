package com.sampahkita.app.data.repository

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.KategoriSampah
import kotlin.math.roundToLong

interface KategoriRepository {
    /**
     * Daftar kategori sampah beserta harga hari ini.
     * @param bankSampahId jika diisi, harga mengikuti Bank Sampah tersebut.
     */
    suspend fun getKategori(bankSampahId: String? = null): List<KategoriSampah>
}

class FakeKategoriRepository : KategoriRepository {
    override suspend fun getKategori(bankSampahId: String?): List<KategoriSampah> {
        val faktor = bankSampahId?.let { DummyData.faktorHargaBank[it] } ?: 1.0
        return DummyData.kategori.map { it.copy(hargaPerKg = bulatkan50(it.hargaPerKg * faktor)) }
    }

    /** Bulatkan ke kelipatan Rp50 terdekat. */
    private fun bulatkan50(harga: Double): Long = (harga / 50).roundToLong() * 50
}
