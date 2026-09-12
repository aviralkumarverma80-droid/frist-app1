package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple

@Composable
fun AudioVisualizer(
    bands: FloatArray,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    barCount: Int = 16,
    isPlaying: Boolean = true
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(NeonMagenta, NeonPurple, NeonCyan)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val totalBars = barCount.coerceAtMost(bands.size)
            if (totalBars <= 0) return@Canvas

            val spacing = 4.dp.toPx()
            val totalSpacing = spacing * (totalBars - 1)
            val barWidth = ((canvasWidth - totalSpacing) / totalBars).coerceAtLeast(2.dp.toPx())

            for (i in 0 until totalBars) {
                val rawMag = if (isPlaying) bands[i] else 0.08f
                val clampedMag = rawMag.coerceIn(0.06f, 1.0f)
                val barHeight = canvasHeight * clampedMag

                val x = i * (barWidth + spacing)
                val y = canvasHeight - barHeight

                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
    }
}
