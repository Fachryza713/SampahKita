package com.sampahkita.app.data.repository

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.BankSampah

interface BankSampahRepository {
    /** Semua Bank Sampah, urut dari yang terdekat. */
    suspend fun getBankSampah(): List<BankSampah>
}

class FakeBankSampahRepository : BankSampahRepository {
    override suspend fun getBankSampah(): List<BankSampah> =
        DummyData.bankSampah.sortedBy { it.jarakKm }
}
