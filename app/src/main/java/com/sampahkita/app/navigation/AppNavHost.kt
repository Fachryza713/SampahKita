package com.sampahkita.app.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sampahkita.app.data.auth.AuthRepository
import com.sampahkita.app.data.model.Role
import com.sampahkita.app.data.preferences.ThemeMode
import com.sampahkita.app.ui.common.PlaceholderScreen
import com.sampahkita.app.ui.home.HomeScreen
import com.sampahkita.app.ui.peta.PetaScreen
import com.sampahkita.app.data.dummy.DummyData
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.sampahkita.app.ui.login.LoginScreen
import com.sampahkita.app.ui.splash.SplashScreen
import com.sampahkita.app.ui.theme.ThemeSettingSection
import com.sampahkita.app.ui.theme.ThemeToggleButton
import com.sampahkita.app.ui.theme.isDarkTheme
import com.sampahkita.app.ui.theme.toggledThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Route beranda sesuai peran. */
fun homeRouteFor(role: Role): String =
    if (role == Role.PETUGAS) Screen.HomePetugas.route else Screen.Home.route

/** Route yang belum dikerjakan; masing-masing menampilkan placeholder. */
private val placeholderScreens = listOf(
    Screen.Katalog,
    Screen.Tabungan,
    Screen.Pencairan,
    Screen.Scan,
    Screen.KelolaKatalog,
    Screen.ValidasiPencairan,
)

@Composable
fun AppNavHost(
    authRepository: AuthRepository,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val isDark = themeMode.isDarkTheme()
    val scope = rememberCoroutineScope()
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
            // TiketSetor: placeholder (fitur 07) yang sudah menerima bankSampahId opsional.
            composable(
                route = Screen.TiketSetor.routePattern,
                arguments = listOf(
                    navArgument(Screen.TiketSetor.ARG_BANK_SAMPAH_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { entry ->
                val bankSampahId = entry.arguments?.getString(Screen.TiketSetor.ARG_BANK_SAMPAH_ID)
                val bank = DummyData.bankSampah.firstOrNull { it.id == bankSampahId }
                PlaceholderScreen(judul = Screen.TiketSetor.judul) {
                    if (bank != null) {
                        Text(
                            text = "Bank tujuan: ${bank.nama}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
            // Beranda Petugas: placeholder + ikon tema di TopAppBar.
            composable(Screen.HomePetugas.route) {
                PlaceholderScreen(
                    judul = Screen.HomePetugas.judul,
                    topBarActions = {
                        ThemeToggleButton(isDark = isDark) {
                            onThemeModeChange(toggledThemeMode(isDark))
                        }
                    },
                )
            }
            // Profil: placeholder + bagian "Tampilan" + tombol Keluar.
            composable(Screen.Profil.route) {
                PlaceholderScreen(judul = Screen.Profil.judul) {
                    ThemeSettingSection(
                        selected = themeMode,
                        onSelect = onThemeModeChange,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(24.dp))
                    // TODO: dialog konfirmasi dibuat di fitur 10.
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                authRepository.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Keluar")
                    }
                }
            }
            placeholderScreens.forEach { screen ->
                composable(screen.route) { PlaceholderScreen(judul = screen.judul) }
            }
        }
    }
}
