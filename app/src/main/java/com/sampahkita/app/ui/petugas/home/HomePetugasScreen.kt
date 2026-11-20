package com.sampahkita.app.ui.petugas.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sampahkita.app.ui.common.formatKg
import com.sampahkita.app.ui.common.formatRupiah
import com.sampahkita.app.ui.common.formatTanggal
import com.sampahkita.app.ui.petugas.PengajuanItem
import com.sampahkita.app.ui.theme.ThemeToggleButton

private const val MAKS_PENGAJUAN_DI_BERANDA = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePetugasScreen(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onScan: () -> Unit,
    onLihatSemuaPencairan: () -> Unit,
    viewModel: HomePetugasViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Halo, ${state.petugas?.nama?.substringBefore(' ') ?: "Petugas"} 👋",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = state.unit?.nama ?: "Unit Bank Sampah",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                actions = { ThemeToggleButton(isDark = isDark, onToggle = onToggleTheme) },
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
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Statistik hari ini", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(12.dp))
                        Row {
                            Stat("Setoran", "${state.stat.jumlahSetoran}", Modifier.weight(1f))
                            Stat("Total berat", formatKg(state.stat.totalKg), Modifier.weight(1f))
                            Stat("Dibayarkan", formatRupiah(state.stat.totalRupiah), Modifier.weight(1.3f))
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = onScan,
                    contentPadding = PaddingValues(vertical = 20.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Scan QR Nasabah", style = MaterialTheme.typography.titleMedium)
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Pencairan menunggu validasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onLihatSemuaPencairan) { Text("Lihat semua") }
                }
            }
            if (state.pengajuan.isEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Tidak ada pengajuan yang menunggu.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(state.pengajuan.take(MAKS_PENGAJUAN_DI_BERANDA), key = { it.transaksi.id }) {
                    PengajuanRingkas(it)
                }
                if (state.pengajuan.size > MAKS_PENGAJUAN_DI_BERANDA) {
                    item {
                        Text(
                            text = "+${state.pengajuan.size - MAKS_PENGAJUAN_DI_BERANDA} pengajuan lainnya",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, nilai: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = nilai,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PengajuanRingkas(item: PengajuanItem) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.namaNasabah,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatTanggal(item.transaksi.tanggal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatRupiah(item.transaksi.nominal),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
