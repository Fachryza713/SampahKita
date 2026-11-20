package com.sampahkita.app.ui.tabungan

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sampahkita.app.data.model.JenisTransaksi
import com.sampahkita.app.data.model.StatusTransaksi
import com.sampahkita.app.data.model.Transaksi
import com.sampahkita.app.ui.common.LocaleIndonesia
import com.sampahkita.app.ui.common.SkeletonBox
import com.sampahkita.app.ui.common.formatKg
import com.sampahkita.app.ui.common.formatRupiah
import com.sampahkita.app.ui.common.formatTanggal
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

private val formatBulan = DateTimeFormatter.ofPattern("MMMM yyyy", LocaleIndonesia)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabunganScreen(
    onSetorSampah: () -> Unit,
    viewModel: TabunganViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Buku Tabungan") }) }) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Ringkasan(state, Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            PrimaryTabRow(selectedTabIndex = state.filter.ordinal) {
                FilterTabungan.entries.forEach { f ->
                    Tab(
                        selected = state.filter == f,
                        onClick = { viewModel.pilihFilter(f) },
                        text = { Text(f.label) },
                    )
                }
            }
            val kelompok = state.kelompok
            when {
                state.isLoading -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) { SkeletonBox(Modifier.fillMaxWidth().height(64.dp)) }
                }
                kelompok.isEmpty() -> EmptyState(onSetorSampah)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    kelompok.forEach { grup ->
                        item(key = grup.bulan.toString()) { HeaderBulan(grup.bulan) }
                        items(grup.transaksi, key = { it.id }) { t ->
                            TransaksiRow(t, onClick = { viewModel.pilihTransaksi(t) })
                        }
                    }
                }
            }
        }
    }

    state.terpilih?.let { t ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.pilihTransaksi(null) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            DetailTransaksi(t, state)
        }
    }
}

// ---------- Ringkasan ----------

@Composable
private fun Ringkasan(state: TabunganUiState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Saldo aktif", style = MaterialTheme.typography.labelLarge)
            Text(
                text = formatRupiah(state.user?.saldo ?: 0),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Row {
                StatKecil("Total daur ulang", formatKg(state.user?.totalKg ?: 0.0), Modifier.weight(1f))
                StatKecil("Setoran bulan ini", "${state.setoranBulanIni} kali", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatKecil(label: String, nilai: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(nilai, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ---------- Daftar ----------

@Composable
private fun HeaderBulan(bulan: YearMonth) {
    Text(
        text = bulan.format(formatBulan),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun IkonJenis(jenis: JenisTransaksi, ukuran: Int = 40) {
    val isSetor = jenis == JenisTransaksi.SETOR
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(ukuran.dp)
            .clip(CircleShape)
            .background(if (isSetor) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer),
    ) {
        Icon(
            imageVector = if (isSetor) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
            contentDescription = if (isSetor) "Setor" else "Pencairan",
            tint = if (isSetor) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size((ukuran / 2).dp),
        )
    }
}

@Composable
private fun warnaNominal(jenis: JenisTransaksi): Color =
    if (jenis == JenisTransaksi.SETOR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

private fun teksNominal(t: Transaksi): String =
    (if (t.jenis == JenisTransaksi.SETOR) "+" else "−") + formatRupiah(t.nominal)

@Composable
private fun TransaksiRow(t: Transaksi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IkonJenis(t.jenis)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = t.keterangan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatTanggal(t.tanggal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                BadgeStatus(t.status)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = teksNominal(t),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = warnaNominal(t.jenis),
            )
        }
    }
}

@Composable
private fun BadgeStatus(status: StatusTransaksi) {
    val (latar, teks) = when (status) {
        StatusTransaksi.BERHASIL -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        StatusTransaksi.MENUNGGU_VALIDASI -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatusTransaksi.DITOLAK -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    Text(
        text = status.label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = teks,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(latar)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun EmptyState(onSetorSampah: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(48.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Belum ada transaksi. Yuk setor sampah pertamamu!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSetorSampah) {
            Icon(Icons.Outlined.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Setor Sampah")
        }
    }
}

// ---------- Detail ----------

@Composable
private fun DetailTransaksi(t: Transaksi, state: TabunganUiState) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IkonJenis(t.jenis, ukuran = 56)
        Spacer(Modifier.height(8.dp))
        Text(
            text = teksNominal(t),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = warnaNominal(t.jenis),
        )
        Text(
            text = t.keterangan,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        BadgeStatus(t.status)

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        BarisInfo(Icons.Outlined.AccessTime, "Waktu", formatTanggal(t.tanggal))
        BarisInfo(Icons.Outlined.Storefront, "Bank Sampah", state.bankById(t.bankSampahId)?.nama ?: "-")
        BarisInfo(Icons.Outlined.Badge, "Petugas", t.petugas ?: "Belum diproses")
        BarisInfo(Icons.Outlined.Tag, "ID transaksi", t.id.uppercase())

        if (t.jenis == JenisTransaksi.SETOR && t.rincian.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Rincian per kategori",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    t.rincian.forEach { item ->
                        val k = state.kategoriById(item.kategoriId)
                        val kg = item.beratAktualKg ?: item.perkiraanKg
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text(
                                text = k?.nama ?: item.kategoriId,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = formatKg(kg),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = formatRupiah(((k?.hargaPerKg ?: 0L) * kg).roundToLong()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(96.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarisInfo(icon: ImageVector, label: String, nilai: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = nilai,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
