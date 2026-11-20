package com.sampahkita.app.ui.katalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.ui.common.formatJarak
import com.sampahkita.app.ui.common.formatRupiah
import com.sampahkita.app.ui.common.formatTanggalPanjang
import com.sampahkita.app.ui.common.kategoriIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KatalogScreen(
    onBack: () -> Unit,
    onBuatTiket: (bankSampahId: String?, items: Map<String, Double>) -> Unit,
    viewModel: KatalogViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Harga") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Harga Sampah Hari Ini",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formatTanggalPanjang(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                PilihBankDropdown(
                    bankList = state.bankList,
                    terpilih = state.bankTerpilih,
                    onPilih = viewModel::pilihBank,
                )
            }
            items(state.kategori, key = { it.id }) { KategoriHargaItem(it) }
            item {
                SimulasiCard(
                    state = state,
                    onTambah = viewModel::tambah,
                    onKurangi = viewModel::kurangi,
                    onInput = viewModel::onInputBerat,
                    onReset = viewModel::reset,
                    onBuatTiket = { onBuatTiket(state.bankTerpilih?.id, state.itemTerisi) },
                )
            }
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
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Bank Sampah",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = terpilih?.nama ?: "Pilih Bank Sampah",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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

@Composable
private fun KategoriIkon(kategori: KategoriSampah, ukuran: Int = 40) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(ukuran.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        Icon(
            imageVector = kategoriIcon(kategori.ikon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size((ukuran * 0.55f).dp),
        )
    }
}

@Composable
private fun KategoriHargaItem(kategori: KategoriSampah) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            KategoriIkon(kategori)
            Spacer(Modifier.width(12.dp))
            Text(
                text = kategori.nama,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${formatRupiah(kategori.hargaPerKg)}/kg",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SimulasiCard(
    state: KatalogUiState,
    onTambah: (String) -> Unit,
    onKurangi: (String) -> Unit,
    onInput: (String, String) -> Unit,
    onReset: () -> Unit,
    onBuatTiket: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Simulasi Pendapatan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
                // Selalu ada (hanya nonaktif) agar tata letak tidak bergeser.
                TextButton(onClick = onReset, enabled = state.itemTerisi.isNotEmpty()) { Text("Reset") }
            }
            Text(
                text = "Masukkan perkiraan berat per kategori (langkah 0,5 kg, maks 100 kg).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.height(8.dp))

            state.kategori.forEach { kategori ->
                BeratRow(
                    kategori = kategori,
                    berat = state.berat[kategori.id] ?: 0.0,
                    teks = state.inputTeks[kategori.id].orEmpty(),
                    melebihiMaks = kategori.id in state.melebihiMaks,
                    onTambah = { onTambah(kategori.id) },
                    onKurangi = { onKurangi(kategori.id) },
                    onInput = { onInput(kategori.id, it) },
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f),
            )
            Text(
                text = "Estimasi pendapatan: ${formatRupiah(state.totalEstimasi)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Nilai akhir mengikuti hasil timbangan petugas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onBuatTiket,
                enabled = state.itemTerisi.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Buat Tiket Setor")
            }
        }
    }
}

@Composable
private fun BeratRow(
    kategori: KategoriSampah,
    berat: Double,
    teks: String,
    melebihiMaks: Boolean,
    onTambah: () -> Unit,
    onKurangi: () -> Unit,
    onInput: (String) -> Unit,
) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KategoriIkon(kategori, ukuran = 32)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = kategori.nama,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatRupiah((berat * kategori.hargaPerKg).toLong()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                )
            }
            FilledTonalIconButton(onClick = onKurangi, enabled = berat > 0.0, colors = stepperColors(), modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Remove, contentDescription = "Kurangi ${kategori.nama}")
            }
            OutlinedTextField(
                value = teks,
                onValueChange = onInput,
                placeholder = {
                    Text("0", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                },
                singleLine = true,
                isError = melebihiMaks,
                textStyle = TextStyle(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .width(80.dp)
                    .padding(horizontal = 4.dp),
            )
            FilledTonalIconButton(onClick = onTambah, enabled = berat < MAKS_KG, colors = stepperColors(), modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = "Tambah ${kategori.nama}")
            }
            Text(
                text = "kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        if (melebihiMaks) {
            Text(
                text = "Maksimal 100 kg per kategori",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 40.dp),
            )
        }
    }
}

/** Tombol stepper kontras di atas kartu secondaryContainer. */
@Composable
private fun stepperColors() = IconButtonDefaults.filledTonalIconButtonColors(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
)
