package com.sampahkita.app.ui.katalog

import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Test

class KatalogUiStateTest {

    private val kategori = listOf(
        KategoriSampah("k-01", "Kardus", 2_500, "kardus"),
        KategoriSampah("k-02", "Botol PET", 4_000, "botol"),
    )

    @Test
    fun totalEstimasiDihitungDariBeratKaliHarga() {
        val state = KatalogUiState(kategori = kategori, berat = mapOf("k-01" to 1.5, "k-02" to 2.25))
        assertEquals(12_750L, state.totalEstimasi) // 3.750 + 9.000
    }

    @Test
    fun itemTerisiMengabaikanBeratNol() {
        val state = KatalogUiState(kategori = kategori, berat = mapOf("k-01" to 0.0, "k-02" to 1.0))
        assertEquals(mapOf("k-02" to 1.0), state.itemTerisi)
    }

    @Test
    fun encodeDecodeItemsBolakBalik() {
        val items = mapOf("k-01" to 1.5, "k-05" to 100.0)
        assertEquals(items, Screen.TiketSetor.decodeItems(Screen.TiketSetor.encodeItems(items)))
        assertEquals(emptyMap<String, Double>(), Screen.TiketSetor.decodeItems(null))
    }
}
