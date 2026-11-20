package com.sampahkita.app.ui.common

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val LocaleIndonesia: Locale = Locale.forLanguageTag("id-ID")

private val angkaFormat: NumberFormat = NumberFormat.getIntegerInstance(LocaleIndonesia)
private val tanggalInput = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val tanggalOutput = DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", LocaleIndonesia)

/** 45500 → "Rp45.500". */
fun formatRupiah(nominal: Long): String = "Rp" + angkaFormat.format(nominal)

/** 12.4 → "12,4 kg". */
fun formatKg(kg: Double): String = String.format(LocaleIndonesia, "%.1f kg", kg)

/** 0.8 → "0,8 km". */
fun formatJarak(km: Double): String = formatKg(km).removeSuffix("kg") + "km"

/** "2026-11-18 10:15" → "18 Nov 2026 · 10:15". */
fun formatTanggal(tanggal: String): String =
    runCatching { LocalDateTime.parse(tanggal, tanggalInput).format(tanggalOutput) }
        .getOrDefault(tanggal)
