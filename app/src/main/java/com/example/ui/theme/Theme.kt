package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MayaDarkColorScheme = darkColorScheme(
    primary = CyberRedAccent,
    onPrimary = CyberWhite,
    secondary = CyberOrange,
    onSecondary = CyberBlack,
    tertiary = CyberCyan,
    background = CyberBlack,
    onBackground = CyberWhite,
    surface = CyberDarkSurface,
    onSurface = CyberWhite,
    surfaceVariant = CyberCardBg,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberRedDim
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CyberBlack.toArgb()
                window.navigationBarColor = CyberBlack.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = MayaDarkColorScheme,
        typography = Typography,
        content = content
    )
}

