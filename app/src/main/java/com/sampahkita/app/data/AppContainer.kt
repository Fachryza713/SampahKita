package com.sampahkita.app.data

import android.content.Context
import com.sampahkita.app.data.auth.AuthRepository
import com.sampahkita.app.data.auth.FakeAuthRepository
import com.sampahkita.app.data.preferences.SessionPreferences
import com.sampahkita.app.data.repository.BankSampahRepository
import com.sampahkita.app.data.repository.FakeBankSampahRepository
import com.sampahkita.app.data.repository.FakeKategoriRepository
import com.sampahkita.app.data.repository.FakeTiketRepository
import com.sampahkita.app.data.repository.FakeTransaksiRepository
import com.sampahkita.app.data.repository.TiketRepository
import com.sampahkita.app.data.repository.KategoriRepository
import com.sampahkita.app.data.repository.TransaksiRepository

/**
 * Tempat membuat repository satu kali untuk seluruh aplikasi.
 * Ganti implementasi Fake... di sini saat backend asli siap.
 */
class AppContainer(context: Context) {
    val authRepository: AuthRepository = FakeAuthRepository(SessionPreferences(context))
    val kategoriRepository: KategoriRepository = FakeKategoriRepository()
    val bankSampahRepository: BankSampahRepository = FakeBankSampahRepository()
    val transaksiRepository: TransaksiRepository = FakeTransaksiRepository()
    val tiketRepository: TiketRepository = FakeTiketRepository()
}
