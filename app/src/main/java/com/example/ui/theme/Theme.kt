package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MiuiLightColorScheme = lightColorScheme(
    primary = MiuiBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = MiuiBlueDark,
    secondary = MiuiEmerald,
    onSecondary = Color.White,
    tertiary = MiuiAmber,
    onTertiary = Color.White,
    background = SurfaceWhite,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    outline = DividerLight,
)

@Composable
fun MiuiOsTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MiuiLightColorScheme,
        typography = Typography,
        content = content,
    )
}
