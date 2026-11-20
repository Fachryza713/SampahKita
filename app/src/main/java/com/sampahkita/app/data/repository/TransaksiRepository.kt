package com.sampahkita.app.data.repository

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.Transaksi

interface TransaksiRepository {
    /** Riwayat transaksi user yang login, urut dari yang terbaru. */
    suspend fun getRiwayat(): List<Transaksi>
}

class FakeTransaksiRepository : TransaksiRepository {
    override suspend fun getRiwayat(): List<Transaksi> =
        DummyData.transaksi.sortedByDescending { it.tanggal }
}
