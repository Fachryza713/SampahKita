package com.sampahkita.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sampahkita.app.data.preferences.ThemeMode

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = LightInversePrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightPrimary,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    surfaceBright = LightSurfaceBright,
    surfaceDim = LightSurfaceDim,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
)

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkPrimary,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceBright = DarkSurfaceBright,
    surfaceDim = DarkSurfaceDim,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
)

private val SampahKitaShapes = Shapes(
    medium = RoundedCornerShape(16.dp), // dipakai Card
)

/** true jika mode gelap harus dipakai untuk [ThemeMode] ini. */
@Composable
fun ThemeMode.isDarkTheme(): Boolean = when (this) {
    ThemeMode.TERANG -> false
    ThemeMode.GELAP -> true
    ThemeMode.SISTEM -> isSystemInDarkTheme()
}

@Composable
fun SampahKitaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val target = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = animateColorScheme(target),
        typography = Typography,
        shapes = SampahKitaShapes,
        content = content,
    )
}

private const val DURASI_TRANSISI_MS = 400

@Composable
private fun Color.animated(): Color =
    animateColorAsState(this, tween(DURASI_TRANSISI_MS), label = "warnaTema").value

/** Transisi halus antar skema warna saat tema berganti. */
@Composable
private fun animateColorScheme(target: ColorScheme): ColorScheme = target.copy(
    primary = target.primary.animated(),
    onPrimary = target.onPrimary.animated(),
    primaryContainer = target.primaryContainer.animated(),
    onPrimaryContainer = target.onPrimaryContainer.animated(),
    inversePrimary = target.inversePrimary.animated(),
    secondary = target.secondary.animated(),
    onSecondary = target.onSecondary.animated(),
    secondaryContainer = target.secondaryContainer.animated(),
    onSecondaryContainer = target.onSecondaryContainer.animated(),
    tertiary = target.tertiary.animated(),
    onTertiary = target.onTertiary.animated(),
    tertiaryContainer = target.tertiaryContainer.animated(),
    onTertiaryContainer = target.onTertiaryContainer.animated(),
    background = target.background.animated(),
    onBackground = target.onBackground.animated(),
    surface = target.surface.animated(),
    onSurface = target.onSurface.animated(),
    surfaceVariant = target.surfaceVariant.animated(),
    onSurfaceVariant = target.onSurfaceVariant.animated(),
    surfaceTint = target.surfaceTint.animated(),
    inverseSurface = target.inverseSurface.animated(),
    inverseOnSurface = target.inverseOnSurface.animated(),
    error = target.error.animated(),
    onError = target.onError.animated(),
    errorContainer = target.errorContainer.animated(),
    onErrorContainer = target.onErrorContainer.animated(),
    outline = target.outline.animated(),
    outlineVariant = target.outlineVariant.animated(),
    surfaceBright = target.surfaceBright.animated(),
    surfaceDim = target.surfaceDim.animated(),
    surfaceContainerLowest = target.surfaceContainerLowest.animated(),
    surfaceContainerLow = target.surfaceContainerLow.animated(),
    surfaceContainer = target.surfaceContainer.animated(),
    surfaceContainerHigh = target.surfaceContainerHigh.animated(),
    surfaceContainerHighest = target.surfaceContainerHighest.animated(),
)
