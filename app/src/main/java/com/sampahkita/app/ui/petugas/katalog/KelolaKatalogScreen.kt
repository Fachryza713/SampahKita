package com.sampahkita.app.ui.petugas.katalog

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.ui.common.formatRupiah
import com.sampahkita.app.ui.common.formatTanggalPanjang
import com.sampahkita.app.ui.common.kategoriIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaKatalogScreen(viewModel: KelolaKatalogViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.pesan.collect { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kelola Katalog") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Box(Modifier.padding(16.dp)) {
                Button(
                    onClick = viewModel::simpan,
                    enabled = state.adaPerubahan && !state.isMenyimpan,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    if (state.isMenyimpan) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                    } else {
                        Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Simpan Harga Hari Ini")
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    text = state.unit?.nama ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Harga per kg · ${formatTanggalPanjang()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.adaPerubahan) {
                    Text(
                        text = "Ada perubahan yang belum disimpan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            items(state.kategori, key = { it.id }) { k ->
                HargaItem(
                    kategori = k,
                    hargaBaru = state.draft[k.id]?.takeIf { it != k.hargaPerKg },
                    onEdit = { viewModel.mulaiEdit(k) },
                )
            }
        }
    }

    state.sedangDiedit?.let { k ->
        val error = state.errorInput
        AlertDialog(
            onDismissRequest = viewModel::batalEdit,
            title = { Text("Ubah harga ${k.nama}") },
            text = {
                OutlinedTextField(
                    value = state.inputHarga,
                    onValueChange = viewModel::onInputHarga,
                    label = { Text("Harga per kg") },
                    prefix = { Text("Rp") },
                    suffix = { Text("/kg") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = {
                        Text(error ?: "Harga saat ini ${formatRupiah(k.hargaPerKg)}/kg")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::terapkanEdit, enabled = error == null) { Text("Terapkan") }
            },
            dismissButton = { TextButton(onClick = viewModel::batalEdit) { Text("Batal") } },
        )
    }
}

@Composable
private fun HargaItem(kategori: KategoriSampah, hargaBaru: Long?, onEdit: () -> Unit) {
    Card(
        onClick = onEdit,
        colors = CardDefaults.cardColors(
            containerColor = if (hargaBaru != null) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                Icon(
                    kategoriIcon(kategori.ikon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = kategori.nama,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                if (hargaBaru != null) {
                    Text(
                        text = formatRupiah(kategori.hargaPerKg),
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                Text(
                    text = "${formatRupiah(hargaBaru ?: kategori.hargaPerKg)}/kg",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (hargaBaru != null) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Outlined.Edit,
                contentDescription = "Ubah harga ${kategori.nama}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )
        }
    }
}
