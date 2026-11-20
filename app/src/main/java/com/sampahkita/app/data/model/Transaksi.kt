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
)
