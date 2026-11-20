package com.sampahkita.app.data.repository

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.KategoriSampah

interface KategoriRepository {
    /** Daftar kategori sampah beserta harga hari ini. */
    suspend fun getKategori(): List<KategoriSampah>
}

class FakeKategoriRepository : KategoriRepository {
    override suspend fun getKategori(): List<KategoriSampah> = DummyData.kategori
}
