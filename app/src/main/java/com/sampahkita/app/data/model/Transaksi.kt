package com.sampahkita.app.data.model

enum class JenisTransaksi { SETOR, PENCAIRAN }

enum class StatusTransaksi(val label: String) {
    BERHASIL("Berhasil"),
    MENUNGGU_VALIDASI("Menunggu Validasi"),
    DITOLAK("Ditolak"),
}

data class Transaksi(
    val id: String,
    val jenis: JenisTransaksi,
    val nominal: Long,
    val keterangan: String,
    /** Format "yyyy-MM-dd HH:mm". */
    val tanggal: String,
    val status: StatusTransaksi,
    /** Rincian berat per kategori (hanya untuk SETOR; berat ada di [ItemSetor.beratAktualKg]). */
    val rincian: List<ItemSetor> = emptyList(),
    /** Nama petugas yang memproses, jika sudah diproses. */
    val petugas: String? = null,
    val bankSampahId: String? = null,
)
