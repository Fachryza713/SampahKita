package com.sampahkita.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.QrCode2
import com.sampahkita.app.data.model.TiketSetor
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.JenisTransaksi
import com.sampahkita.app.data.model.KategoriSampah
import com.sampahkita.app.data.model.Transaksi
import com.sampahkita.app.data.model.User
import com.sampahkita.app.navigation.Screen
import com.sampahkita.app.ui.common.SkeletonBox
import com.sampahkita.app.ui.common.formatJarak
import com.sampahkita.app.ui.common.formatKg
import com.sampahkita.app.ui.common.formatRupiah
import com.sampahkita.app.ui.common.formatTanggal
import com.sampahkita.app.ui.common.kategoriIcon
import com.sampahkita.app.ui.theme.ThemeToggleButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onNavigate: (Screen) -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.user?.let { "Halo, ${it.namaDepan()} 👋" } ?: "Halo 👋",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Yuk setor sampah hari ini",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    ThemeToggleButton(isDark = isDark, onToggle = onToggleTheme)
                    // Notifikasi belum berfungsi.
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifikasi")
                    }
                },
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.isLoading) {
                HomeSkeleton()
            } else {
                HomeContent(state = state, onNavigate = onNavigate)
            }
        }
    }
}

private fun User.namaDepan(): String = nama.substringBefore(' ')

@Composable
private fun HomeContent(state: HomeUiState, onNavigate: (Screen) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            SaldoCard(
                saldo = state.user?.saldo ?: 0,
                totalKg = state.user?.totalKg ?: 0.0,
                onCairkan = { onNavigate(Screen.Pencairan) },
                onRiwayat = { onNavigate(Screen.Tabungan) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        state.tiketAktif?.let { tiket ->
            item(key = "tiketAktif") {
                TiketAktifCard(
                    tiket = tiket,
                    namaBank = state.namaBank(tiket.bankSampahId),
                    onClick = { onNavigate(Screen.TiketSetor) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        item {
            AksiCepat(onNavigate = onNavigate, modifier = Modifier.padding(horizontal = 16.dp))
        }
        item {
            SectionHeader(judul = "Harga Hari Ini", onLihatSemua = { onNavigate(Screen.Katalog) })
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.kategori, key = { it.id }) { KategoriCard(it) }
            }
        }
        state.bankTerdekat?.let { bank ->
            item {
                SectionHeader(judul = "Bank Sampah Terdekat")
                BankTerdekatCard(
                    bank = bank,
                    onLihatPeta = { onNavigate(Screen.Peta) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        item {
            SectionHeader(judul = "Aktivitas Terakhir", onLihatSemua = { onNavigate(Screen.Tabungan) })
        }
        if (state.transaksiTerakhir.isEmpty()) {
            item {
                Text(
                    text = "Belum ada aktivitas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        } else {
            items(state.transaksiTerakhir, key = { it.id }) { transaksi ->
                TransaksiItem(transaksi, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

// ---------- Kartu Saldo ----------

@Composable
private fun SaldoCard(
    saldo: Long,
    totalKg: Double,
    onCairkan: () -> Unit,
    onRiwayat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Saldo Tabungan", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                // Badge aksen kuning khas saldo
                Text(
                    text = "Eco-Passbook",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatRupiah(saldo),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Recycling, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Total daur ulang ${formatKg(totalKg)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onCairkan,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f),
                ) { Text("Cairkan") }
                OutlinedButton(
                    onClick = onRiwayat,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    modifier = Modifier.weight(1f),
                ) { Text("Riwayat") }
            }
        }
    }
}

// ---------- Tiket aktif ----------

private fun HomeUiState.namaBank(id: String): String =
    semuaBank.firstOrNull { it.id == id }?.nama ?: "Bank Sampah"

@Composable
private fun TiketAktifCard(
    tiket: TiketSetor,
    namaBank: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.QrCode2, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Tiket setor aktif", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = tiket.kodeQr,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = namaBank,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Berlaku s.d. ${formatTanggal(tiket.berlakuSampai)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Lihat tiket")
        }
    }
}

// ---------- Aksi Cepat ----------

private data class AksiItem(val label: String, val icon: ImageVector, val tujuan: Screen)

private val aksiCepat = listOf(
    AksiItem("Setor Sampah", Icons.Outlined.QrCode, Screen.TiketSetor),
    AksiItem("Peta Bank Sampah", Icons.Outlined.Map, Screen.Peta),
    AksiItem("Katalog Harga", Icons.Outlined.Sell, Screen.Katalog),
    AksiItem("Riwayat", Icons.AutoMirrored.Outlined.ReceiptLong, Screen.Tabungan),
)

@Composable
private fun AksiCepat(onNavigate: (Screen) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = "Aksi Cepat",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            aksiCepat.forEach { aksi ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigate(aksi.tujuan) }
                        .padding(vertical = 4.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Icon(
                            imageVector = aksi.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = aksi.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        minLines = 2,
                    )
                }
            }
        }
    }
}

// ---------- Harga Hari Ini ----------

@Composable
private fun SectionHeader(judul: String, onLihatSemua: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp)
            .height(40.dp),
    ) {
        Text(
            text = judul,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (onLihatSemua != null) {
            TextButton(onClick = onLihatSemua) { Text("Lihat semua") }
        }
    }
}

@Composable
private fun KategoriCard(kategori: KategoriSampah) {
    Card(
        modifier = Modifier.width(128.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                Icon(
                    imageVector = kategoriIcon(kategori.ikon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = kategori.nama,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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

// ---------- Bank Sampah Terdekat ----------

@Composable
private fun BankTerdekatCard(bank: BankSampah, onLihatPeta: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
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
                Text(
                    text = bank.nama,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                InfoKecil(Icons.Outlined.NearMe, formatJarak(bank.jarakKm))
                InfoKecil(Icons.Outlined.AccessTime, "Buka ${bank.jamBuka}")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onLihatPeta,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) { Text("Lihat Peta") }
        }
    }
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

// ---------- Aktivitas Terakhir ----------

@Composable
private fun TransaksiItem(transaksi: Transaksi, modifier: Modifier = Modifier) {
    val isSetor = transaksi.jenis == JenisTransaksi.SETOR
    // Setor = saldo naik (hijau/primary), pencairan = saldo turun (merah/error).
    val warna = if (isSetor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val latarIkon = if (isSetor) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val warnaIkon = if (isSetor) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(latarIkon),
            ) {
                Icon(
                    imageVector = if (isSetor) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                    contentDescription = if (isSetor) "Setor" else "Pencairan",
                    tint = warnaIkon,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = transaksi.keterangan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatTanggal(transaksi.tanggal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = (if (isSetor) "+" else "−") + formatRupiah(transaksi.nominal),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = warna,
            )
        }
    }
}

// ---------- Skeleton ----------

@Composable
private fun HomeSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        userScrollEnabled = false,
    ) {
        item { SkeletonBox(Modifier.fillMaxWidth().height(180.dp)) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { SkeletonBox(Modifier.weight(1f).height(80.dp)) }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) { SkeletonBox(Modifier.weight(1f).height(120.dp)) }
            }
        }
        item { SkeletonBox(Modifier.fillMaxWidth().height(96.dp)) }
        items(3) { SkeletonBox(Modifier.fillMaxWidth().height(64.dp)) }
    }
}
