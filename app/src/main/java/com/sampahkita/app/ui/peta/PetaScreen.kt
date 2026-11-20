package com.sampahkita.app.ui.peta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sampahkita.app.ui.common.formatJarak
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetaScreen(
    onSetorKeSini: (bankSampahId: String) -> Unit,
    viewModel: PetaViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    /** Tutup sheet dengan animasi, lalu jalankan [lalu]. */
    fun tutupSheet(lalu: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            viewModel.pilih(null)
            lalu()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Peta digambar sampai ke belakang status bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val tinggiPeta = maxHeight * 0.4f
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    PetaPlaceholder(Modifier.fillMaxWidth().height(tinggiPeta))
                }
                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text("Cari Bank Sampah") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Hapus pencarian")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        FilterChipSederhana("Terdekat", state.filterTerdekat, viewModel::toggleTerdekat)
                        FilterChipSederhana("Buka Sekarang", state.filterBukaSekarang, viewModel::toggleBukaSekarang)
                    }
                }
                val hasil = state.hasil
                if (hasil.isEmpty() && state.semua.isNotEmpty()) {
                    item { HasilKosong() }
                } else {
                    items(hasil, key = { it.bank.id }) { item ->
                        BankSampahCard(
                            item = item,
                            onClick = { viewModel.pilih(item) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }

    state.terpilih?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.pilih(null) },
            sheetState = sheetState,
        ) {
            DetailBankSampah(
                item = item,
                onSetorKeSini = { tutupSheet { onSetorKeSini(item.bank.id) } },
                onPetunjukArah = {
                    tutupSheet { scope.launch { snackbarHostState.showSnackbar("Segera hadir") } }
                },
            )
        }
    }
}

@Composable
private fun PetaPlaceholder(modifier: Modifier = Modifier) {
    // TODO: ganti dengan Google Maps Compose
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.secondaryContainer,
                ),
            ),
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.statusBarsPadding(),
        ) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Peta interaktif segera hadir",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipSederhana(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else {
            null
        },
    )
}

@Composable
private fun BankSampahCard(item: BankSampahItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bank = item.bank
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(16.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = bank.nama,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    BadgeBuka(item.isBuka)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = bank.alamat,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoKecil(Icons.Outlined.NearMe, formatJarak(bank.jarakKm))
                    Spacer(Modifier.width(16.dp))
                    InfoKecil(Icons.Outlined.AccessTime, bank.jamBuka)
                }
            }
        }
    }
}

@Composable
private fun BadgeBuka(isBuka: Boolean) {
    val latar = if (isBuka) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val teks = if (isBuka) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    Text(
        text = if (isBuka) "Buka" else "Tutup",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = teks,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(latar)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Composable
private fun InfoKecil(icon: ImageVector, teks: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = teks,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HasilKosong() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Bank Sampah tidak ditemukan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DetailBankSampah(
    item: BankSampahItem,
    onSetorKeSini: () -> Unit,
    onPetunjukArah: () -> Unit,
) {
    val bank = item.bank
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = bank.nama,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            BadgeBuka(item.isBuka)
        }
        Spacer(Modifier.height(16.dp))
        BarisDetail(Icons.Outlined.Place, "Alamat", bank.alamat)
        BarisDetail(Icons.Outlined.NearMe, "Jarak", formatJarak(bank.jarakKm))
        BarisDetail(Icons.Outlined.AccessTime, "Jam operasional", "${bank.jamBuka} WIB")
        BarisDetail(Icons.Outlined.Phone, "Kontak", bank.kontak)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSetorKeSini, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Setor ke sini")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onPetunjukArah, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Petunjuk Arah")
        }
    }
}

@Composable
private fun BarisDetail(icon: ImageVector, label: String, nilai: String) {
    Row(Modifier.padding(vertical = 6.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = nilai,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
