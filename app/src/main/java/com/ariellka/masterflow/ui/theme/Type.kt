package com.ariellka.masterflow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun flowText(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) =
    TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = weight,
        fontSize = size.sp, lineHeight = height.sp, letterSpacing = 0.sp)

val Typography = Typography(
    headlineLarge = flowText(32, 38, FontWeight.SemiBold),
    headlineMedium = flowText(28, 34, FontWeight.SemiBold),
    headlineSmall = flowText(26, 32),
    titleLarge = flowText(22, 28, FontWeight.Medium),
    titleMedium = flowText(18, 24, FontWeight.Medium),
    titleSmall = flowText(16, 22, FontWeight.Medium),
    bodyLarge = flowText(16, 24),
    bodyMedium = flowText(14, 20),
    bodySmall = flowText(12, 18),
    labelLarge = flowText(14, 20, FontWeight.Medium),
    labelMedium = flowText(12, 16, FontWeight.Medium),
    labelSmall = flowText(11, 16, FontWeight.Medium)
)
