package com.sampahkita.app.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sampahkita.app.data.model.Role
import com.sampahkita.app.data.preferences.ThemeMode
import com.sampahkita.app.ui.theme.ThemeSettingSection
import com.sampahkita.app.ui.theme.ThemeToggleButton
import com.sampahkita.app.ui.theme.isDarkTheme
import com.sampahkita.app.ui.theme.toggledThemeMode
import com.sampahkita.app.data.repository.FakeSessionRepository
import com.sampahkita.app.ui.common.PlaceholderScreen
import com.sampahkita.app.ui.login.LoginScreen
import com.sampahkita.app.ui.splash.SplashScreen

/** Route beranda sesuai peran. */
fun homeRouteFor(role: Role): String =
    if (role == Role.PETUGAS) Screen.HomePetugas.route else Screen.Home.route

/** Route yang belum dikerjakan; masing-masing menampilkan placeholder. */
private val placeholderScreens = listOf(
    Screen.Peta,
    Screen.Katalog,
    Screen.TiketSetor,
    Screen.Tabungan,
    Screen.Pencairan,
    Screen.Scan,
    Screen.KelolaKatalog,
    Screen.ValidasiPencairan,
)

@Composable
fun AppNavHost(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val isDark = themeMode.isDarkTheme()
    val currentUser by FakeSessionRepository.currentUser.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

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
                        val user = FakeSessionRepository.currentUser.value
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
                        FakeSessionRepository.setUser(user)
                        navController.navigate(homeRouteFor(user.role)) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                )
            }
            // Beranda Nasabah & Petugas: placeholder + ikon tema di TopAppBar.
            listOf(Screen.Home, Screen.HomePetugas).forEach { screen ->
                composable(screen.route) {
                    PlaceholderScreen(
                        judul = screen.judul,
                        topBarActions = {
                            ThemeToggleButton(isDark = isDark) {
                                onThemeModeChange(toggledThemeMode(isDark))
                            }
                        },
                    )
                }
            }
            // Profil: placeholder + bagian "Tampilan".
            composable(Screen.Profil.route) {
                PlaceholderScreen(judul = Screen.Profil.judul) {
                    ThemeSettingSection(
                        selected = themeMode,
                        onSelect = onThemeModeChange,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            placeholderScreens.forEach { screen ->
                composable(screen.route) { PlaceholderScreen(judul = screen.judul) }
            }
        }
    }
}
