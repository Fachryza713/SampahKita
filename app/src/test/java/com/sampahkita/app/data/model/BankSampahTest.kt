package com.sampahkita.app.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class BankSampahTest {

    private val bank = BankSampah("bs", "Uji", "Alamat", 1.0, "08.00–15.00", "0812")

    @Test
    fun bukaDalamJamOperasional() {
        assertTrue(bank.isBukaPada(LocalTime.of(8, 0)))
        assertTrue(bank.isBukaPada(LocalTime.of(10, 55)))
        assertTrue(bank.isBukaPada(LocalTime.of(14, 59)))
    }

    @Test
    fun tutupDiLuarJamOperasional() {
        assertFalse(bank.isBukaPada(LocalTime.of(7, 59)))
        assertFalse(bank.isBukaPada(LocalTime.of(15, 0)))
        assertFalse(bank.isBukaPada(LocalTime.of(22, 56)))
    }

    @Test
    fun formatJamTidakValidDianggapTutup() {
        assertFalse(bank.copy(jamBuka = "setiap hari").isBukaPada(LocalTime.NOON))
    }
}
