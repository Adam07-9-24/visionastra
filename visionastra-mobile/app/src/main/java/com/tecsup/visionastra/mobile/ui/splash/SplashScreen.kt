package com.tecsup.visionastra.mobile.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val background = Color(0xFFF8FAFC)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val primary = Color(0xFF3B82F6)
    Surface(
        modifier = modifier.fillMaxSize(),
        color = background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = primary,
                border = BorderStroke(1.dp, Color(0xFFDCE5F0))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(42.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val path = Path().apply {
                            moveTo(cx, 2.dp.toPx())
                            lineTo(cx + 8.dp.toPx(), cy - 8.dp.toPx())
                            lineTo(size.width - 2.dp.toPx(), cy)
                            lineTo(cx + 8.dp.toPx(), cy + 8.dp.toPx())
                            lineTo(cx, size.height - 2.dp.toPx())
                            lineTo(cx - 8.dp.toPx(), cy + 8.dp.toPx())
                            lineTo(2.dp.toPx(), cy)
                            lineTo(cx - 8.dp.toPx(), cy - 8.dp.toPx())
                            close()
                        }
                        drawPath(path, Color.White)
                        drawCircle(Color.White.copy(alpha = 0.75f), 3.dp.toPx(), Offset(cx, cy))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "VisionAstra",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Text(
                text = "Preparando tu espacio creativo",
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(22.dp))
            CircularProgressIndicator(color = primary)
        }
    }
}
