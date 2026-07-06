package com.tecsup.visionastra.mobile.ui.ai.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.ui.ai.GenerationStatus

@Composable
fun GenerationStatusChip(status: String) {
    val generationStatus = GenerationStatus.fromValue(status)
    val colors = generationStatusColors(generationStatus)
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = colors.container,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = generationStatus.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = colors.content,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class StatusColors(
    val container: Color,
    val content: Color,
    val border: Color
)

private fun generationStatusColors(status: GenerationStatus): StatusColors =
    when (status) {
        GenerationStatus.Pending -> StatusColors(
            container = Color(0xFFF8FAFC),
            content = Color(0xFF475569),
            border = Color(0xFFDCE5F0)
        )
        GenerationStatus.Processing -> StatusColors(
            container = Color(0xFFEFF6FF),
            content = Color(0xFF2563EB),
            border = Color(0xFFBFDBFE)
        )
        GenerationStatus.Completed -> StatusColors(
            container = Color(0xFFF0FDF4),
            content = Color(0xFF15803D),
            border = Color(0xFFBBF7D0)
        )
        GenerationStatus.Error -> StatusColors(
            container = Color(0xFFFFF1F2),
            content = Color(0xFFB42318),
            border = Color(0xFFFECACA)
        )
    }
