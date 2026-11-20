package com.sampahkita.app.ui.profil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sampahkita.app.data.model.Role
import com.sampahkita.app.data.model.User
import com.sampahkita.app.data.preferences.ThemeMode
import com.sampahkita.app.ui.theme.ThemeSettingSection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLogoutSelesai: () -> Unit,
    viewModel: ProfilViewModel = viewModel(),
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var dialogTentang by rememberSaveable { mutableStateOf(false) }
    var dialogKeluar by rememberSaveable { mutableStateOf(false) }

    fun segeraHadir() {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Segera hadir")
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            HeaderProfil(user)

            JudulBagian("Akun")
            KartuBagian {
                BarisMenu(Icons.Outlined.Edit, "Edit Profil", onClick = ::segeraHadir)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                BarisMenu(Icons.Outlined.Lock, "Ubah Password", onClick = ::segeraHadir)
            }

            Spacer(Modifier.height(20.dp))
            KartuBagian {
                // Judul "Tampilan" sudah ada di dalam ThemeSettingSection (fitur 02).
                ThemeSettingSection(
                    selected = themeMode,
                    onSelect = onThemeModeChange,
                    modifier = Modifier.padding(16.dp),
                )
            }

            JudulBagian("Tentang")
            KartuBagian {
                BarisMenu(Icons.Outlined.Info, "Tentang SampahKita", onClick = { dialogTentang = true })
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                BarisMenu(
                    icon = Icons.Outlined.PhoneAndroid,
                    judul = "Versi aplikasi",
                    nilai = viewModel.versiAplikasi,
                )
            }

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { dialogKeluar = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Keluar")
            }
        }
    }

    if (dialogTentang) {
        AlertDialog(
            onDismissRequest = { dialogTentang = false },
            icon = { Icon(Icons.Filled.Eco, contentDescription = null) },
            title = { Text("Tentang SampahKita") },
            text = {
                Column {
                    Text(
                        "SampahKita adalah aplikasi bank sampah digital. Nasabah dapat mencari Bank Sampah " +
                            "terdekat, mengecek harga sampah harian, membuat tiket setor, dan memantau tabungan. " +
                            "Petugas memindai tiket, menimbang sampah, dan memvalidasi pencairan saldo.",
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Anggota kelompok:", fontWeight = FontWeight.SemiBold)
                    ANGGOTA_KELOMPOK.forEach { Text("• $it") }
                }
            },
            confirmButton = { TextButton(onClick = { dialogTentang = false }) { Text("Tutup") } },
        )
    }

    if (dialogKeluar) {
        AlertDialog(
            onDismissRequest = { dialogKeluar = false },
            title = { Text("Keluar dari akun?") },
            text = { Text("Kamu perlu masuk lagi untuk menggunakan SampahKita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogKeluar = false
                        viewModel.logout(onLogoutSelesai)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Keluar") }
            },
            dismissButton = { TextButton(onClick = { dialogKeluar = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun HeaderProfil(user: User?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Text(
                text = user?.let { inisial(it.nama) }.orEmpty(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = user?.nama.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = user?.email.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (user != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (user.role == Role.PETUGAS) "Petugas" else "Nasabah",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun JudulBagian(teks: String) {
    Text(
        text = teks,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 4.dp),
    )
}

@Composable
private fun KartuBagian(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) { content() }
}

@Composable
private fun BarisMenu(
    icon: ImageVector,
    judul: String,
    nilai: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ListItem(
        headlineContent = { Text(judul) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = {
            when {
                nilai != null -> Text(nilai, color = MaterialTheme.colorScheme.onSurfaceVariant)
                onClick != null -> Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
            }
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier,
    )
}

