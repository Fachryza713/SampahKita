package com.sampahkita.app.ui.pencairan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sampahkita.app.data.model.StatusTransaksi
import com.sampahkita.app.data.model.Transaksi
import com.sampahkita.app.ui.common.PilihBankDropdown
import com.sampahkita.app.ui.common.formatRupiah
import com.sampahkita.app.ui.common.formatTanggal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PencairanScreen(
    onBack: () -> Unit,
    viewModel: PencairanViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.pesan.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pencairan Saldo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Saldo tersedia
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Saldo tersedia", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = formatRupiah(state.saldoTersedia),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (state.sedangDiajukan > 0) {
                            Text(
                                text = "Saldo ${formatRupiah(state.saldo)} · ${formatRupiah(state.sedangDiajukan)} sedang diajukan",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            // Nominal
            item {
                Text(
                    text = "Nominal pencairan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                val tampilError = state.tampilkanError && state.error != null
                OutlinedTextField(
                    value = state.inputNominal,
                    onValueChange = viewModel::onNominalChange,
                    prefix = { Text("Rp") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    isError = tampilError,
                    supportingText = {
                        Text(
                            when {
                                tampilError -> state.error.orEmpty()
                                state.nominal != null -> formatRupiah(state.nominal ?: 0)
                                else -> "Minimal ${formatRupiah(MIN_PENCAIRAN)}"
                            },
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NOMINAL_CEPAT.forEach { n ->
                        FilterChip(
                            selected = state.nominal == n,
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.pilihNominalCepat(n)
                            },
                            label = { Text(formatRupiah(n)) },
                        )
                    }
                    FilterChip(
                        selected = state.nominal != null && state.nominal == state.saldoTersedia,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.pilihSemuaSaldo()
                        },
                        label = { Text("Semua saldo") },
                    )
                }
            }

            // Bank
            item {
                PilihBankDropdown(
                    bankList = state.bankList,
                    terpilih = state.bankTerpilih,
                    onPilih = viewModel::pilihBank,
                    label = "Ambil uang di",
                )
            }

            // Catatan + tombol
            item {
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
                        text = "Uang diserahkan tunai oleh petugas setelah pengajuan divalidasi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.mintaKonfirmasi()
                    },
                    enabled = !state.isMengirim,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    if (state.isMengirim) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                    } else {
                        Icon(Icons.Outlined.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ajukan Pencairan")
                    }
                }
            }

            // Riwayat pengajuan
            item {
                Text(
                    text = "Pengajuan sebelumnya",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            if (state.pengajuan.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada pengajuan pencairan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.pengajuan, key = { it.id }) { PengajuanRow(it) }
            }
        }
    }

    if (state.konfirmasi) {
        AlertDialog(
            onDismissRequest = viewModel::batalKonfirmasi,
            icon = { Icon(Icons.Outlined.Payments, contentDescription = null) },
            title = { Text("Ajukan pencairan?") },
            text = {
                Text(
                    "Kamu akan mencairkan ${formatRupiah(state.nominal ?: 0)} dan mengambilnya tunai di " +
                        "${state.bankTerpilih?.nama}. Saldo berkurang setelah petugas menyetujui.",
                )
            },
            confirmButton = { TextButton(onClick = viewModel::ajukan) { Text("Ajukan") } },
            dismissButton = { TextButton(onClick = viewModel::batalKonfirmasi) { Text("Batal") } },
        )
    }
}

/** Label status khusus pencairan: Menunggu · Disetujui · Ditolak. */
private fun labelPencairan(status: StatusTransaksi): String = when (status) {
    StatusTransaksi.MENUNGGU_VALIDASI -> "Menunggu"
    StatusTransaksi.BERHASIL -> "Disetujui"
    StatusTransaksi.DITOLAK -> "Ditolak"
}

@Composable
private fun PengajuanRow(t: Transaksi) {
    val (latar, teks) = when (t.status) {
        StatusTransaksi.MENUNGGU_VALIDASI -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        StatusTransaksi.BERHASIL -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        StatusTransaksi.DITOLAK -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = formatRupiah(t.nominal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = t.keterangan,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatTanggal(t.tanggal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = labelPencairan(t.status),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = teks,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(latar)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
