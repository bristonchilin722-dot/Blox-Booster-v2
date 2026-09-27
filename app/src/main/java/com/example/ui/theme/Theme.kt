package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BloxDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color.Black,
    primaryContainer = NeonPurpleDark,
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = CyberCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00363A),
    onSecondaryContainer = Color(0xFFE0F7FA),
    tertiary = CyberPink,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = Color(0xFFEF4444),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Blox Booster is a dedicated dark gaming optimizer suite
    MaterialTheme(
        colorScheme = BloxDarkColorScheme,
        typography = Typography,
        content = content
    )
}
