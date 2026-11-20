package com.sampahkita.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sampahkita.app.navigation.AppNavHost
import com.sampahkita.app.ui.theme.SampahKitaTheme
import com.sampahkita.app.ui.theme.ThemeViewModel
import com.sampahkita.app.ui.theme.isDarkTheme

class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = themeMode.isDarkTheme()

            // Ikon status bar & navigation bar mengikuti tema aplikasi, bukan tema sistem.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { darkTheme },
                )
                onDispose {}
            }

            SampahKitaTheme(darkTheme = darkTheme) {
                AppNavHost(
                    authRepository = (application as SampahKitaApp).container.authRepository,
                    themeMode = themeMode,
                    onThemeModeChange = themeViewModel::setThemeMode,
                )
            }
        }
    }

    private companion object {
        // Nilai bawaan androidx.activity untuk scrim navigation bar 3-tombol.
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
