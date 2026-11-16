package com.ariellka.masterflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = FlowBlue, onPrimary = FlowInk,
    primaryContainer = FlowBlue, onPrimaryContainer = FlowInk,
    secondary = FlowAccent, onSecondary = Color.White,
    secondaryContainer = FlowMint, onSecondaryContainer = FlowMintInk,
    background = FlowBackground, onBackground = FlowInk,
    surface = FlowBackground, onSurface = FlowInk,
    surfaceVariant = FlowCard, onSurfaceVariant = FlowMuted,
    outline = Color(0xFF817789), outlineVariant = Color(0xFFCAC1CE),
    error = Color(0xFF9D3448), errorContainer = Color(0xFFF9DFE4),
    onErrorContainer = Color(0xFF782638)
)

private val DarkColors = darkColorScheme(
    primary = FlowBlue, onPrimary = FlowInk,
    primaryContainer = Color(0xFF344D68), onPrimaryContainer = FlowNightInk,
    secondary = Color(0xFFB0CDED), onSecondary = FlowInk,
    secondaryContainer = Color(0xFF284A3F), onSecondaryContainer = Color(0xFFCEEDDF),
    background = FlowNight, onBackground = FlowNightInk,
    surface = FlowNight, onSurface = FlowNightInk,
    surfaceVariant = FlowNightCard, onSurfaceVariant = FlowNightMuted,
    outline = Color(0xFF9C91A8), outlineVariant = Color(0xFF544B60),
    error = Color(0xFFFFB2C0), errorContainer = Color(0xFF572735),
    onErrorContainer = Color(0xFFFFDAE2)
)

@Composable
fun MasterFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = FlowShapes,
        content = content
    )
}
