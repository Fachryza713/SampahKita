package com.sampahkita.app.ui.tiket

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.TiketSetor
import com.sampahkita.app.ui.common.formatJarak
import com.sampahkita.app.ui.common.formatKg
import com.sampahkita.app.ui.common.formatRupiah
import com.sampahkita.app.ui.common.formatTanggal
import com.sampahkita.app.ui.common.kategoriIcon
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiketSetorScreen(
    onKembaliKeBeranda: () -> Unit,
    viewModel: TiketSetorViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tiket = state.tiketAktif

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (tiket == null) "Setor Sampah" else "Drop-Off Pass") })
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = tiket,
            contentKey = { it?.id },
            modifier = Modifier.padding(innerPadding),
            label = "langkahTiket",
        ) { tiketSaatIni ->
            if (tiketSaatIni == null) {
                FormTiket(
                    state = state,
                    onPilihBank = viewModel::pilihBank,
                    onToggleKategori = viewModel::toggleKategori,
                    onTambah = viewModel::tambah,
                    onKurangi = viewModel::kurangi,
                    onBuatTiket = viewModel::buatTiket,
                )
            } else {
                DropOffPass(
                    tiket = tiketSaatIni,
                    state = state,
                    onBatalkan = viewModel::batalkanTiket,
                    onKembaliKeBeranda = onKembaliKeBeranda,
                )
            }
        }
    }
}

// ================= Langkah 1 — Form tiket =================

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FormTiket(
    state: TiketSetorUiState,
    onPilihBank: (BankSampah) -> Unit,
    onToggleKategori: (String) -> Unit,
    onTambah: (String) -> Unit,
    onKurangi: (String) -> Unit,
    onBuatTiket: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
    ) {
        LabelLangkah("Langkah 1 dari 2 · Isi tiket setor")

        JudulBagian("Bank Sampah tujuan")
        PilihBankDropdown(state.bankList, state.bankTerpilih, onPilihBank)

        JudulBagian("Kategori sampah")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            state.kategori.forEach { k ->
                val terpilih = k.id in state.dipilih
                FilterChip(
                    selected = terpilih,
                    onClick = { onToggleKategori(k.id) },
                    label = { Text(k.nama) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (terpilih) Icons.Filled.Check else kategoriIcon(k.ikon),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }

        if (state.dipilih.isNotEmpty()) {
            JudulBagian("Perkiraan berat")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    state.kategori.filter { it.id in state.dipilih }.forEach { k ->
                        val berat = state.dipilih.getValue(k.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(k.nama, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = "${formatRupiah(k.hargaPerKg)}/kg",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            FilledTonalIconButton(
                                onClick = { onKurangi(k.id) },
                                enabled = berat > TiketSetorViewModel.MIN_KG,
                                modifier = Modifier.size(36.dp),
                            ) { Icon(Icons.Outlined.Remove, contentDescription = "Kurangi ${k.nama}") }
                            Text(
                                text = formatKg(berat),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(72.dp),
                            )
                            FilledTonalIconButton(
                                onClick = { onTambah(k.id) },
                                enabled = berat < TiketSetorViewModel.MAX_KG,
                                modifier = Modifier.size(36.dp),
                            ) { Icon(Icons.Outlined.Add, contentDescription = "Tambah ${k.nama}") }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        // Ringkasan estimasi
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Ringkasan", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "${state.dipilih.size} kategori · ${formatKg(state.dipilih.values.sum())}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Estimasi nilai: ${formatRupiah(state.estimasi)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Nilai akhir mengikuti hasil timbangan petugas.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onBuatTiket,
            enabled = state.bisaBuatTiket,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            if (state.isMembuat) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
            } else {
                Icon(Icons.Outlined.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Buat Tiket")
            }
        }
        if (state.dipilih.isEmpty()) {
            Text(
                text = "Pilih minimal satu kategori sampah.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun PilihBankDropdown(
    bankList: List<BankSampah>,
    terpilih: BankSampah?,
    onPilih: (BankSampah) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedCard(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = terpilih?.nama ?: "Pilih Bank Sampah",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Pilih Bank Sampah")
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            bankList.forEach { bank ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(bank.nama)
                            Text(
                                text = formatJarak(bank.jarakKm),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    onClick = {
                        onPilih(bank)
                        expanded = false
                    },
                )
            }
        }
    }
}

// ================= Langkah 2 — Drop-Off Pass =================

@Composable
private fun DropOffPass(
    tiket: TiketSetor,
    state: TiketSetorUiState,
    onBatalkan: () -> Unit,
    onKembaliKeBeranda: () -> Unit,
) {
    var konfirmasiBatal by rememberSaveable { mutableStateOf(false) }
    val bank = state.bankById(tiket.bankSampahId)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LabelLangkah("Langkah 2 dari 2 · Tiket siap dipindai")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = bank?.nama ?: tiket.bankSampahId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    BadgeMenunggu()
                }
                Spacer(Modifier.height(16.dp))

                QrPlaceholder()

                Spacer(Modifier.height(12.dp))
                Text(
                    text = tiket.kodeQr,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Berlaku sampai ${formatTanggal(tiket.berlakuSampai)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                // Rincian kategori
                Text(
                    text = "Rincian setoran",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
                var total = 0L
                tiket.items.forEach { item ->
                    val k = state.kategoriById(item.kategoriId)
                    val nilai = ((k?.hargaPerKg ?: 0L) * item.perkiraanKg).roundToLong()
                    total += nilai
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = k?.nama ?: item.kategoriId,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = formatKg(item.perkiraanKg),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = formatRupiah(nilai),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(96.dp),
                        )
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        text = "Estimasi nilai",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatRupiah(total),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        // Instruksi
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(12.dp),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Tunjukkan tiket ini ke petugas Bank Sampah untuk dipindai.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onKembaliKeBeranda, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Kembali ke Beranda")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { konfirmasiBatal = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Batalkan Tiket") }
    }

    if (konfirmasiBatal) {
        AlertDialog(
            onDismissRequest = { konfirmasiBatal = false },
            title = { Text("Batalkan tiket?") },
            text = { Text("Tiket ${tiket.kodeQr} akan dibatalkan dan tidak bisa dipindai lagi.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        konfirmasiBatal = false
                        onBatalkan()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Ya, batalkan") }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiBatal = false }) { Text("Tidak") }
            },
        )
    }
}

@Composable
private fun QrPlaceholder() {
    // TODO: generate QR dari kodeQr (ZXing)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.QrCode2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(96.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "QR akan tampil di sini",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun BadgeMenunggu() {
    Text(
        text = "Menunggu",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun LabelLangkah(teks: String) {
    Text(
        text = teks,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    )
}

@Composable
private fun JudulBagian(teks: String) {
    Text(
        text = teks,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}
