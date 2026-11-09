package com.ariellka.masterflow.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Switch to scrolling and icon-only navigation at 150% text size.
const val LARGE_FONT_SCALE = 1.5f

object FlowSpace {
    val tiny = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val page = 16.dp
    val large = 24.dp
    val section = 28.dp
    val touch = 48.dp
    val button = 56.dp
    val avatar = 40.dp
    val illustration = 104.dp
    val contentWidth = 600.dp
    val scheduleViewport = 160.dp
}

object FlowCorners {
    val card = RoundedCornerShape(20.dp)
    val button = RoundedCornerShape(16.dp)
    val navigation = RoundedCornerShape(26.dp)
    val pill = RoundedCornerShape(50)
}

val FlowShapes = Shapes(
    small = FlowCorners.button,
    medium = FlowCorners.card,
    large = FlowCorners.navigation
)
