package com.tecsup.visionastra.mobile.ui.ai.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.tecsup.visionastra.mobile.core.network.NetworkConstants
import com.tecsup.visionastra.mobile.core.network.authenticatedImageLoader
import com.tecsup.visionastra.mobile.data.remote.dto.ResourceResponse
import com.tecsup.visionastra.mobile.ui.resources.displayTitle

@Composable
fun ResourceSelectionCard(
    resource: ResourceResponse,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = ResourceSelectionColors()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) colors.selectedSurface else colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.border),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResourceVisual(resource = resource, colors = colors)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = resource.displayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (resource.tipo == "copy") "Idea" else "Imagen",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) colors.primary else colors.textSecondary
                )
                if (resource.tipo == "copy") {
                    Text(
                        text = resource.contenidoTexto.orEmpty().ifBlank { "Sin contenido" },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
            Checkbox(
                checked = selected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.primary,
                    uncheckedColor = colors.textSecondary
                )
            )
        }
    }
}

@Composable
private fun ResourceVisual(
    resource: ResourceResponse,
    colors: ResourceSelectionColors
) {
    if (resource.tipo == "imagen") {
        val context = LocalContext.current
        AsyncImage(
            model = "${NetworkConstants.BASE_URL}api/recursos/archivo/${resource.idRecurso}",
            imageLoader = remember { authenticatedImageLoader(context) },
            contentDescription = resource.displayTitle(),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    } else {
        Surface(
            modifier = Modifier.size(58.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.soft,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Box(contentAlignment = Alignment.Center) {
                TextGlyph(colors.primary)
            }
        }
    }
}

@Composable
private fun TextGlyph(color: Color) {
    Canvas(modifier = Modifier.size(26.dp)) {
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(5.dp.toPx(), 7.dp.toPx()), Offset(21.dp.toPx(), 7.dp.toPx()), 1.9.dp.toPx())
        drawLine(color, Offset(5.dp.toPx(), 13.dp.toPx()), Offset(21.dp.toPx(), 13.dp.toPx()), 1.9.dp.toPx())
        drawLine(color, Offset(5.dp.toPx(), 19.dp.toPx()), Offset(15.dp.toPx(), 19.dp.toPx()), 1.9.dp.toPx())
    }
}

private data class ResourceSelectionColors(
    val surface: Color = Color.White,
    val selectedSurface: Color = Color(0xFFEFF6FF),
    val soft: Color = Color(0xFFEFF6FF),
    val textPrimary: Color = Color(0xFF0F172A),
    val textSecondary: Color = Color(0xFF64748B),
    val primary: Color = Color(0xFF3B82F6),
    val border: Color = Color(0xFFDCE5F0)
)
