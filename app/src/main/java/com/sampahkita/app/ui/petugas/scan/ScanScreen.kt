package com.sampahkita.app.ui.petugas.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sampahkita.app.ui.common.formatKg
import com.sampahkita.app.ui.common.formatRupiah
import com.sampahkita.app.ui.theme.DarkColorScheme
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(viewModel: ScanViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Scaffold(topBar = { TopAppBar(title = { Text("Scan & Timbang") }) }) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (val hasil = state.hasil) {
                is HasilScan.Sukses -> LayarSukses(hasil, onBerikutnya = viewModel::reset)
                else -> {
                    KameraPlaceholder()
                    // Input kode manual
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = state.kodeInput,
                            onValueChange = viewModel::onKodeChange,
                            label = { Text("Masukkan kode tiket manual") },
                            placeholder = { Text("SK-20261120-0012") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Search,
                            ),
                            keyboardActions = KeyboardActions(onSearch = {
                                focusManager.clearFocus()
                                viewModel.cari()
                            }),
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.cari()
                            },
                            enabled = !state.isMencari,
                            modifier = Modifier.height(56.dp).padding(top = 6.dp),
                        ) {
                            if (state.isMencari) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                            } else {
                                Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cari")
                            }
                        }
                    }
                    when (hasil) {
                        is HasilScan.Gagal -> PesanError(hasil.pesan)
                        is HasilScan.Ditemukan -> FormTimbangan(
                            hasil = hasil,
                            state = state,
                            onBeratChange = viewModel::onBeratChange,
                            onKonfirmasi = {
                                focusManager.clearFocus()
                                viewModel.konfirmasiSetoran()
                            },
                        )
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun KameraPlaceholder() {
    // TODO: CameraX + ML Kit Barcode
    // Area kamera selalu gelap: pakai skema warna gelap di bagian ini saja.
    MaterialTheme(colorScheme = DarkColorScheme) {
        val warnaBingkai = MaterialTheme.colorScheme.primary
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        ) {
            // Bingkai scan berupa 4 sudut
            Canvas(Modifier.size(170.dp)) {
                val p = size.width
                val l = p * 0.22f
                val w = 6.dp.toPx()
                listOf(
                    Offset(0f, 0f) to listOf(Offset(l, 0f), Offset(0f, l)),
                    Offset(p, 0f) to listOf(Offset(p - l, 0f), Offset(p, l)),
                    Offset(0f, p) to listOf(Offset(l, p), Offset(0f, p - l)),
                    Offset(p, p) to listOf(Offset(p - l, p), Offset(p, p - l)),
                ).forEach { (sudut, ujung) ->
                    ujung.forEach { drawLine(warnaBingkai, sudut, it, strokeWidth = w, cap = StrokeCap.Round) }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Kamera scanner segera hadir",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun PesanError(pesan: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp),
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
        Spacer(Modifier.width(8.dp))
        Text(pesan, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Composable
private fun FormTimbangan(
    hasil: HasilScan.Ditemukan,
    state: ScanUiState,
    onBeratChange: (String, String) -> Unit,
    onKonfirmasi: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = hasil.namaNasabah,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = hasil.tiket.kodeQr,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                text = "Masukkan berat aktual hasil timbangan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            hasil.tiket.items.forEach { item ->
                val k = hasil.kategori.firstOrNull { it.id == item.kategoriId }
                val teks = state.beratInput[item.kategoriId].orEmpty()
                val valid = parseBerat(teks)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(k?.nama ?: item.kategoriId, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = "Perkiraan ${formatKg(item.perkiraanKg)} · ${formatRupiah(k?.hargaPerKg ?: 0)}/kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "= ${formatRupiah(((valid ?: 0.0) * (k?.hargaPerKg ?: 0)).roundToLong())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    OutlinedTextField(
                        value = teks,
                        onValueChange = { onBeratChange(item.kategoriId, it) },
                        suffix = { Text("kg") },
                        singleLine = true,
                        isError = valid == null,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(120.dp),
                    )
                }
            }
            if (state.beratValid == null) {
                Text(
                    text = "Berat harus lebih dari 0 dan maksimal ${MAKS_BERAT_KG.toInt()} kg.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Row {
                Text(
                    text = "Total dibayarkan",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatRupiah(state.total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onKonfirmasi,
                enabled = state.bisaKonfirmasi,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                if (state.isMenyimpan) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                } else {
                    Text("Konfirmasi Setoran")
                }
            }
        }
    }
}

@Composable
private fun LayarSukses(hasil: HasilScan.Sukses, onBerikutnya: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(72.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Setoran berhasil!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = hasil.kode,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BarisRingkas("Nasabah", hasil.namaNasabah)
                BarisRingkas("Total berat", formatKg(hasil.totalKg))
                BarisRingkas("Saldo nasabah bertambah", formatRupiah(hasil.nominal), tebal = true)
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBerikutnya, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Scan tiket berikutnya")
        }
    }
}

@Composable
private fun BarisRingkas(label: String, nilai: String, tebal: Boolean = false) {
    Row {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            text = nilai,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (tebal) FontWeight.Bold else FontWeight.Medium,
            color = if (tebal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}
