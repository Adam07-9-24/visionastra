package com.tecsup.visionastra.mobile.ui.ai.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GenerationResultSection(
    title: String,
    value: String?
) {
    if (value.isNullOrBlank()) return
    var expanded by remember { mutableStateOf(false) }
    val colors = ResultSectionPalette()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(if (expanded) "Ocultar" else "Ver", color = colors.primary, style = MaterialTheme.typography.labelLarge)
            }
            if (expanded) {
                Text(text = value, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            }
        }
    }
}

private data class ResultSectionPalette(
    val surface: Color = Color.White,
    val textPrimary: Color = Color(0xFF0F172A),
    val primary: Color = Color(0xFF3B82F6),
    val border: Color = Color(0xFFDCE5F0)
)
