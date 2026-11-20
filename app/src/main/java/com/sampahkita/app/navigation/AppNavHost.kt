package com.sampahkita.app.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sampahkita.app.data.auth.AuthRepository
import com.sampahkita.app.data.model.Role
import com.sampahkita.app.data.preferences.ThemeMode
import com.sampahkita.app.ui.home.HomeScreen
import com.sampahkita.app.ui.peta.PetaScreen
import com.sampahkita.app.ui.katalog.KatalogScreen
import com.sampahkita.app.ui.tiket.TiketSetorScreen
import com.sampahkita.app.ui.tabungan.TabunganScreen
import com.sampahkita.app.ui.pencairan.PencairanScreen
import com.sampahkita.app.ui.profil.ProfilScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.sampahkita.app.ui.login.LoginScreen
import com.sampahkita.app.ui.splash.SplashScreen
import com.sampahkita.app.ui.petugas.home.HomePetugasScreen
import com.sampahkita.app.ui.petugas.katalog.KelolaKatalogScreen
import com.sampahkita.app.ui.petugas.scan.ScanScreen
import com.sampahkita.app.ui.petugas.validasi.ValidasiPencairanScreen
import com.sampahkita.app.ui.theme.isDarkTheme
import com.sampahkita.app.ui.theme.toggledThemeMode
import kotlinx.coroutines.flow.first

/** Route beranda sesuai peran. */
fun homeRouteFor(role: Role): String =
    if (role == Role.PETUGAS) Screen.HomePetugas.route else Screen.Home.route


@Composable
fun AppNavHost(
    authRepository: AuthRepository,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val isDark = themeMode.isDarkTheme()
    val currentUser by remember(authRepository) { authRepository.currentUser }
        .collectAsStateWithLifecycle(initialValue = null)
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Buang bagian argumen opsional, mis. "tiket_setor?bankSampahId={...}" → "tiket_setor".
    val currentRoute = backStackEntry?.destination?.route?.substringBefore('?')

    val role = currentUser?.role
    val navItems = role?.let { navItemsFor(it) }.orEmpty()
    val showBottomBar = navItems.any { it.screen.route == currentRoute }

    Scaffold(
        // Inset diurus oleh tiap layar; Scaffold ini hanya memberi ruang untuk bottom bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar && role != null) {
                BottomNavBar(
                    items = navItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(homeRouteFor(role)) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSelesai = {
                        // Sudah login? langsung ke beranda sesuai peran : Login.
                        val user = authRepository.currentUser.first()
                        val tujuan = user?.let { homeRouteFor(it.role) } ?: Screen.Login.route
                        navController.navigate(tujuan) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginBerhasil = { user ->
                        navController.navigate(homeRouteFor(user.role)) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    isDark = isDark,
                    onToggleTheme = { onThemeModeChange(toggledThemeMode(isDark)) },
                    onNavigate = { tujuan ->
                        // Tujuan yang ada di bottom nav dibuka sebagai tab; sisanya ditumpuk biasa.
                        if (nasabahNavItems.any { it.screen == tujuan }) {
                            navController.navigate(tujuan.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate(tujuan.route)
                        }
                    },
                )
            }
            composable(Screen.Peta.route) {
                PetaScreen(
                    onSetorKeSini = { bankSampahId ->
                        navController.navigate(Screen.TiketSetor.createRoute(bankSampahId)) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Screen.Katalog.route) {
                KatalogScreen(
                    onBack = { navController.popBackStack() },
                    onBuatTiket = { bankSampahId, items ->
                        navController.navigate(Screen.TiketSetor.createRoute(bankSampahId, items)) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            // TiketSetor: placeholder (fitur 07) yang sudah menerima bankSampahId & items opsional.
            composable(
                route = Screen.TiketSetor.routePattern,
                arguments = listOf(
                    navArgument(Screen.TiketSetor.ARG_BANK_SAMPAH_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(Screen.TiketSetor.ARG_ITEMS) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                // Argumen dibaca TiketSetorViewModel lewat SavedStateHandle.
                TiketSetorScreen(
                    onKembaliKeBeranda = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Screen.Tabungan.route) {
                TabunganScreen(
                    onSetorSampah = {
                        navController.navigate(Screen.TiketSetor.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Screen.Pencairan.route) {
                PencairanScreen(onBack = { navController.popBackStack() })
            }
            // ---------- Petugas ----------
            composable(Screen.HomePetugas.route) {
                HomePetugasScreen(
                    isDark = isDark,
                    onToggleTheme = { onThemeModeChange(toggledThemeMode(isDark)) },
                    onScan = { navController.navigateTab(Screen.Scan.route, Screen.HomePetugas.route) },
                    onLihatSemuaPencairan = {
                        navController.navigateTab(Screen.ValidasiPencairan.route, Screen.HomePetugas.route)
                    },
                )
            }
            composable(Screen.Scan.route) { ScanScreen() }
            composable(Screen.KelolaKatalog.route) { KelolaKatalogScreen() }
            composable(Screen.ValidasiPencairan.route) { ValidasiPencairanScreen() }
            composable(Screen.Profil.route) {
                ProfilScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onLogoutSelesai = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

/** Buka route sebagai tab bottom nav (state tab lain tetap tersimpan). */
private fun NavHostController.navigateTab(route: String, homeRoute: String) {
    navigate(route) {
        popUpTo(homeRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
