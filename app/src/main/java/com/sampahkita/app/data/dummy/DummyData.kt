package com.sampahkita.app.data.dummy

import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.ItemSetor
import com.sampahkita.app.data.model.JenisTransaksi
import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.data.model.Role
import com.sampahkita.app.data.model.StatusTransaksi
import com.sampahkita.app.data.model.Transaksi
import com.sampahkita.app.data.model.User

/** Akun demo beserta password-nya (hanya untuk simulasi, tanpa backend). */
data class AkunDemo(val user: User, val password: String)

object DummyData {

    val nasabah = User(
        id = "u-001",
        nama = "Rina Lestari",
        username = "nasabah",
        email = "nasabah@sampahkita.id",
        role = Role.NASABAH,
        saldo = 45_500,
        totalKg = 12.4,
    )

    val petugas = User(
        id = "u-002",
        nama = "Budi Santoso",
        username = "petugas",
        email = "petugas@sampahkita.id",
        role = Role.PETUGAS,
        saldo = 0,
        totalKg = 0.0,
        bankSampahId = "bs-01",
    )

    val akun: List<AkunDemo> = listOf(
        AkunDemo(nasabah, "123456"),
        AkunDemo(petugas, "123456"),
    )

    val bankSampah: List<BankSampah> = listOf(
        BankSampah(
            id = "bs-01",
            nama = "Bank Sampah Lestari Pogung",
            alamat = "Jl. Pogung Baru No. 12, Sinduadi, Mlati, Sleman",
            jarakKm = 0.8,
            jamBuka = "08.00–15.00",
            kontak = "0812-2345-6701",
        ),
        BankSampah(
            id = "bs-02",
            nama = "Bank Sampah Hijau Karangmalang",
            alamat = "Jl. Karangmalang Blok C No. 5, Caturtunggal, Depok, Sleman",
            jarakKm = 1.6,
            jamBuka = "09.00–16.00",
            kontak = "0812-2345-6702",
        ),
        BankSampah(
            id = "bs-03",
            nama = "Bank Sampah Berseri Pugeran",
            alamat = "Jl. Pugeran Asri No. 21, Suryodiningratan, Mantrijeron, Yogyakarta",
            jarakKm = 3.4,
            jamBuka = "07.30–14.00",
            kontak = "0812-2345-6703",
        ),
        BankSampah(
            id = "bs-04",
            nama = "Bank Sampah Mandiri Seturan",
            alamat = "Jl. Seturan Indah No. 8, Caturtunggal, Depok, Sleman",
            jarakKm = 4.1,
            jamBuka = "10.00–17.00",
            kontak = "0812-2345-6704",
        ),
    )

    val kategori: List<KategoriSampah> = listOf(
        KategoriSampah("k-01", "Kardus", 2_500, "kardus"),
        KategoriSampah("k-02", "Botol PET", 4_000, "botol"),
        KategoriSampah("k-03", "Gelas Plastik", 3_500, "gelas"),
        KategoriSampah("k-04", "Kertas HVS", 2_000, "kertas"),
        KategoriSampah("k-05", "Kaleng Aluminium", 12_000, "kaleng"),
        KategoriSampah("k-06", "Minyak Jelantah", 5_000, "minyak"),
    )

    /** Faktor pengali harga per Bank Sampah (harga dasar = [kategori]). */
    val faktorHargaBank: Map<String, Double> = mapOf(
        "bs-01" to 1.00,
        "bs-02" to 1.05,
        "bs-03" to 0.95,
        "bs-04" to 1.10,
    )

    /** Riwayat transaksi nasabah demo, urut dari yang terbaru. */
    /**
     * Riwayat transaksi nasabah demo, urut dari yang terbaru.
     * Nominal setor = berat × harga dasar [kategori]; total setor − pencairan = saldo 45.500.
     */
    val transaksi: List<Transaksi> = listOf(
        Transaksi(
            "t-008", JenisTransaksi.SETOR, 9_500, "Setor Botol PET 1,5 kg & Kardus 1,4 kg", "2026-11-18 10:15",
            StatusTransaksi.BERHASIL, rincian = listOf(berat("k-02", 1.5), berat("k-01", 1.4)),
            petugas = petugas.nama, bankSampahId = "bs-01",
        ),
        Transaksi(
            "t-007", JenisTransaksi.PENCAIRAN, 20_000, "Pencairan tunai di Bank Sampah Lestari Pogung", "2026-11-12 13:40",
            StatusTransaksi.BERHASIL, petugas = petugas.nama, bankSampahId = "bs-01",
        ),
        Transaksi(
            "t-006", JenisTransaksi.SETOR, 14_400, "Setor Kaleng Aluminium 1,2 kg", "2026-11-05 09:05",
            StatusTransaksi.BERHASIL, rincian = listOf(berat("k-05", 1.2)),
            petugas = petugas.nama, bankSampahId = "bs-01",
        ),
        Transaksi(
            "t-005", JenisTransaksi.SETOR, 7_500, "Setor Minyak Jelantah 1,5 kg", "2026-10-27 15:20",
            StatusTransaksi.BERHASIL, rincian = listOf(berat("k-06", 1.5)),
            petugas = "Sri Wahyuni", bankSampahId = "bs-02",
        ),
        Transaksi(
            "t-004", JenisTransaksi.PENCAIRAN, 15_000, "Pencairan tunai di Bank Sampah Hijau Karangmalang", "2026-10-20 11:00",
            StatusTransaksi.BERHASIL, petugas = "Sri Wahyuni", bankSampahId = "bs-02",
        ),
        Transaksi(
            "t-003", JenisTransaksi.SETOR, 10_000, "Setor Kertas HVS 3,0 kg & Kardus 1,6 kg", "2026-10-14 08:45",
            StatusTransaksi.BERHASIL, rincian = listOf(berat("k-04", 3.0), berat("k-01", 1.6)),
            petugas = petugas.nama, bankSampahId = "bs-01",
        ),
        Transaksi(
            "t-002", JenisTransaksi.SETOR, 24_600, "Setor Gelas Plastik 2,0 kg & Botol PET 4,4 kg", "2026-10-02 16:10",
            StatusTransaksi.BERHASIL, rincian = listOf(berat("k-03", 2.0), berat("k-02", 4.4)),
            petugas = petugas.nama, bankSampahId = "bs-01",
        ),
        Transaksi(
            "t-001", JenisTransaksi.SETOR, 14_500, "Setor Kardus 5,8 kg", "2026-09-25 10:30",
            StatusTransaksi.BERHASIL, rincian = listOf(berat("k-01", 5.8)),
            petugas = "Sri Wahyuni", bankSampahId = "bs-02",
        ),
    )

    /** Item hasil timbangan (perkiraan = aktual). */
    private fun berat(kategoriId: String, kg: Double) = ItemSetor(kategoriId, kg, kg)
}
