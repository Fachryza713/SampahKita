package com.sampahkita.app.data.repository

import com.sampahkita.app.data.model.ItemSetor
import com.sampahkita.app.data.model.StatusTiket
import com.sampahkita.app.data.model.TiketSetor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface TiketRepository {
    /** Tiket berstatus MENUNGGU yang terakhir dibuat, atau null. */
    val tiketAktif: Flow<TiketSetor?>

    suspend fun buatTiket(bankSampahId: String, items: List<ItemSetor>): TiketSetor

    suspend fun batalkan(tiketId: String)
}

/** Menyimpan tiket di memori (hilang saat aplikasi ditutup). */
class FakeTiketRepository(
    private val sekarang: () -> LocalDateTime = { LocalDateTime.now() },
) : TiketRepository {

    private val semuaTiket = MutableStateFlow<List<TiketSetor>>(emptyList())

    /** Nomor urut kode tiket; mulai 11 agar tiket pertama = ...-0012 seperti contoh. */
    private var nomorUrut = 11

    override val tiketAktif: Flow<TiketSetor?> = semuaTiket.map { list ->
        list.lastOrNull { it.status == StatusTiket.MENUNGGU }
    }

    override suspend fun buatTiket(bankSampahId: String, items: List<ItemSetor>): TiketSetor {
        val waktu = sekarang()
        nomorUrut += 1
        val kode = "SK-${waktu.format(FORMAT_KODE)}-${nomorUrut.toString().padStart(4, '0')}"
        val tiket = TiketSetor(
            id = "tk-$nomorUrut",
            kodeQr = kode,
            bankSampahId = bankSampahId,
            items = items,
            status = StatusTiket.MENUNGGU,
            dibuat = waktu.format(FORMAT_WAKTU),
        )
        semuaTiket.update { it + tiket }
        return tiket
    }

    override suspend fun batalkan(tiketId: String) {
        semuaTiket.update { list ->
            list.map { if (it.id == tiketId) it.copy(status = StatusTiket.BATAL) else it }
        }
    }

    private companion object {
        val FORMAT_KODE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val FORMAT_WAKTU: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
