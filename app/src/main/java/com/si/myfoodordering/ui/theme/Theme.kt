package com.si.myfoodordering.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = White,
    primaryContainer = PrimaryOrange.copy(alpha = 0.22f),
    onPrimaryContainer = TextWhite,
    secondary = SecondaryOrange,
    onSecondary = White,
    tertiary = SuccessGreen,
    onTertiary = White,
    background = DeepBlack,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = TextGray,
    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF3A3A3C),
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = White,
    primaryContainer = PrimaryOrange.copy(alpha = 0.12f),
    onPrimaryContainer = DarkGrey,
    secondary = SecondaryOrange,
    onSecondary = White,
    tertiary = SuccessGreen,
    onTertiary = White,
    background = Color(0xFFF8F8F8),
    onBackground = DarkGrey,
    surface = White,
    onSurface = DarkGrey,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFE8E8EC),
)

@Composable
fun MyFoodOrderingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
