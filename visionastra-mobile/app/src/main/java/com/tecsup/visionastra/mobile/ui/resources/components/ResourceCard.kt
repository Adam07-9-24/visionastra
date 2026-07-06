package com.tecsup.visionastra.mobile.ui.resources.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tecsup.visionastra.mobile.core.util.formatMb
import com.tecsup.visionastra.mobile.data.remote.dto.ResourceResponse
import com.tecsup.visionastra.mobile.ui.resources.ResourceType
import com.tecsup.visionastra.mobile.ui.resources.displayTitle
import com.tecsup.visionastra.mobile.ui.resources.formatResourceDate

@Composable
fun ResourceCard(
    resource: ResourceResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ResourceCardPalette()
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = resource.displayTitle(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                ResourceTypeChip(type = resource.tipo)
            }
            when (ResourceType.fromValue(resource.tipo)) {
                ResourceType.Image -> ImagePreview(resource, colors)
                ResourceType.Copy -> CopyPreview(resource, colors)
                ResourceType.Video -> VideoPreview(colors)
                ResourceType.Document -> DocumentPreview(colors)
            }
            ResourceMetaRow(resource = resource, colors = colors)
        }
    }
}

@Composable
private fun ImagePreview(resource: ResourceResponse, colors: ResourceCardPalette) {
    val context = LocalContext.current
    AsyncImage(
        model = "${NetworkConstants.BASE_URL}api/recursos/archivo/${resource.idRecurso}",
        imageLoader = remember { authenticatedImageLoader(context) },
        contentDescription = resource.displayTitle(),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(18.dp))
    )
}

@Composable
private fun CopyPreview(resource: ResourceResponse, colors: ResourceCardPalette) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        SoftIconBox(colors) { TextGlyph(colors.primary) }
        Text(
            text = resource.contenidoTexto.orEmpty().ifBlank { "Sin contenido" },
            modifier = Modifier.weight(1f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun VideoPreview(colors: ResourceCardPalette) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0F172A)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayGlyph(Color.White)
            Column {
                Text("Video generado", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("Toca para reproducir", color = Color(0xFFBAE6FD), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DocumentPreview(colors: ResourceCardPalette) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        SoftIconBox(colors) { DocumentGlyph(colors.warning) }
        Text(
            text = "Disponible en la version web",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun ResourceMetaRow(resource: ResourceResponse, colors: ResourceCardPalette) {
    val size = resource.pesoMb.formatMb().takeUnless { it.contains("disponible", ignoreCase = true) }
    val parts = buildList {
        add("Estado: ${resource.estado}")
        if (size != null) add(size)
        add("Subido: ${resource.fechaSubida.formatResourceDate()}")
    }
    Text(
        text = parts.joinToString(" · "),
        style = MaterialTheme.typography.bodySmall,
        color = colors.textSecondary
    )
}

@Composable
private fun SoftIconBox(colors: ResourceCardPalette, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = colors.soft,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

@Composable private fun TextGlyph(color: Color) { Canvas(Modifier.size(24.dp)) { drawLine(color, Offset(4.dp.toPx(), 7.dp.toPx()), Offset(20.dp.toPx(), 7.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(4.dp.toPx(), 12.dp.toPx()), Offset(20.dp.toPx(), 12.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(4.dp.toPx(), 17.dp.toPx()), Offset(14.dp.toPx(), 17.dp.toPx()), 2.dp.toPx()) } }
@Composable private fun DocumentGlyph(color: Color) { Canvas(Modifier.size(24.dp)) { val s = Stroke(1.9.dp.toPx(), cap = StrokeCap.Round); drawRoundRect(color, Offset(5.dp.toPx(), 3.dp.toPx()), androidx.compose.ui.geometry.Size(14.dp.toPx(), 18.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()), style = s); drawLine(color, Offset(8.dp.toPx(), 9.dp.toPx()), Offset(16.dp.toPx(), 9.dp.toPx()), 1.6.dp.toPx()); drawLine(color, Offset(8.dp.toPx(), 13.dp.toPx()), Offset(16.dp.toPx(), 13.dp.toPx()), 1.6.dp.toPx()) } }
@Composable private fun PlayGlyph(color: Color) { Canvas(Modifier.size(34.dp)) { drawCircle(color.copy(alpha = 0.18f), 16.dp.toPx(), Offset(size.width / 2f, size.height / 2f)); val path = androidx.compose.ui.graphics.Path().apply { moveTo(14.dp.toPx(), 10.dp.toPx()); lineTo(14.dp.toPx(), 24.dp.toPx()); lineTo(25.dp.toPx(), 17.dp.toPx()); close() }; drawPath(path, color) } }

private data class ResourceCardPalette(
    val surface: Color = Color.White,
    val soft: Color = Color(0xFFEFF6FF),
    val textPrimary: Color = Color(0xFF0F172A),
    val textSecondary: Color = Color(0xFF64748B),
    val primary: Color = Color(0xFF3B82F6),
    val border: Color = Color(0xFFDCE5F0),
    val warning: Color = Color(0xFFB45309)
)
