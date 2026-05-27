package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = JiminePrimaryDark,
    onPrimary = JimineOnPrimaryDark,
    primaryContainer = JiminePrimaryContainerDark,
    onPrimaryContainer = JimineOnPrimaryContainerDark,
    secondary = JimineSecondaryDark,
    background = JimineBackgroundDark,
    surface = JimineSurfaceDark,
    onBackground = JimineOnBackgroundDark,
    onSurface = JimineOnSurfaceDark,
    surfaceVariant = JimineSurfaceVariantDark,
    onSurfaceVariant = JimineOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = JiminePrimaryLight,
    onPrimary = JimineOnPrimaryLight,
    primaryContainer = JiminePrimaryContainerLight,
    onPrimaryContainer = JimineOnPrimaryContainerLight,
    secondary = JimineSecondaryLight,
    background = JimineBackgroundLight,
    surface = JimineSurfaceLight,
    onBackground = JimineOnBackgroundLight,
    onSurface = JimineOnSurfaceLight,
    surfaceVariant = JimineSurfaceVariantLight,
    onSurfaceVariant = JimineOnSurfaceVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to force our custom celestial branding!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
