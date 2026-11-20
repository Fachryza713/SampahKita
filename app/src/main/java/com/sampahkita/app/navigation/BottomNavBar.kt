package com.sampahkita.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.sampahkita.app.data.model.Role

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val nasabahNavItems = listOf(
    BottomNavItem(Screen.Home, "Beranda", Icons.Filled.Home),
    BottomNavItem(Screen.Peta, "Peta", Icons.Filled.Map),
    BottomNavItem(Screen.TiketSetor, "Setor", Icons.Filled.QrCode),
    BottomNavItem(Screen.Tabungan, "Tabungan", Icons.Filled.AccountBalanceWallet),
    BottomNavItem(Screen.Profil, "Profil", Icons.Filled.Person),
)

val petugasNavItems = listOf(
    BottomNavItem(Screen.HomePetugas, "Beranda", Icons.Filled.Home),
    BottomNavItem(Screen.Scan, "Scan", Icons.Filled.QrCodeScanner),
    BottomNavItem(Screen.KelolaKatalog, "Katalog", Icons.Filled.Sell),
    BottomNavItem(Screen.ValidasiPencairan, "Pencairan", Icons.Filled.Payments),
    BottomNavItem(Screen.Profil, "Profil", Icons.Filled.Person),
)

fun navItemsFor(role: Role): List<BottomNavItem> =
    if (role == Role.PETUGAS) petugasNavItems else nasabahNavItems

@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = { onItemClick(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
