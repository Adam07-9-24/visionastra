package com.tecsup.visionastra.mobile.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.data.remote.dto.AiGenerationResponse
import com.tecsup.visionastra.mobile.ui.ai.components.GenerationStatusChip
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBarVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGenerationListScreen(
    state: AiGenerationListUiState,
    onBackClick: () -> Unit,
    onNewClick: () -> Unit,
    onGenerationClick: (Int) -> Unit,
    onRetryClick: () -> Unit
) {
    val colors = aiListPalette
    Scaffold(
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = "Generaciones IA",
                subtitle = "Contenido creado con inteligencia artificial",
                onBackClick = onBackClick,
                variant = VisionAstraTopAppBarVariant.Module
            )
        },
        floatingActionButton = {
            if (state.generations.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onNewClick,
                    containerColor = colors.sky,
                    contentColor = Color.White,
                    icon = { AddGlyph(Color.White) },
                    text = { Text("Nueva generacion", fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }

            state.errorMessage != null -> AiMessageState(
                title = "No se pudieron cargar las generaciones",
                message = state.errorMessage,
                actionText = "Reintentar",
                onActionClick = onRetryClick,
                colors = colors,
                isEmpty = false,
                modifier = Modifier.padding(innerPadding)
            )

            state.generations.isEmpty() -> AiMessageState(
                title = "Todavia no tienes generaciones IA",
                message = "Selecciona una campana activa, combina imagenes e ideas, y crea contenido con inteligencia artificial.",
                actionText = "Crear primera generacion",
                onActionClick = onNewClick,
                colors = colors,
                isEmpty = true,
                modifier = Modifier.padding(innerPadding)
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AiHeaderCard(colors = colors)
                }
                items(state.generations, key = { it.idGeneracion }) { generation ->
                    AiGenerationCard(
                        generation = generation,
                        colors = colors,
                        onClick = { onGenerationClick(generation.idGeneracion) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AiGenerationCard(
    generation: AiGenerationResponse,
    colors: AiListPalette,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = colors.soft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AiGlyph(colors.sky)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = generation.nombreCampana ?: "Campana ${generation.idCampana}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    GenerationStatusChip(generation.estado)
                }
                Text(
                    text = "Salida: ${generation.tipoSalida}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Text(
                    text = "Fecha: ${generation.fechaCreacion.formatAiDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Text(
                    text = generation.prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (generation.estado == "error" && !generation.mensajeError.isNullOrBlank()) {
                    Text(
                        text = generation.mensajeError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AiHeaderCard(colors: AiListPalette) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.soft,
        border = BorderStroke(1.dp, colors.sky.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AiGlyph(colors.primary)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "Contenido creado con inteligencia artificial",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Convierte campanas, imagenes e ideas en videos listos para publicar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun AiMessageState(
    title: String,
    message: String,
    actionText: String,
    onActionClick: () -> Unit,
    colors: AiListPalette,
    isEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(if (isEmpty) 24.dp else 20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, if (isEmpty) colors.sky.copy(alpha = 0.4f) else colors.error.copy(alpha = 0.28f)),
            shadowElevation = if (isEmpty) 5.dp else 2.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isEmpty) colors.skySoft else colors.errorSoft,
                    border = BorderStroke(1.dp, if (isEmpty) colors.sky.copy(alpha = 0.28f) else colors.error.copy(alpha = 0.24f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isEmpty) AiGlyph(colors.sky) else ErrorGlyph(colors.error)
                    }
                }
                Text(
                    text = title,
                    modifier = Modifier.padding(top = 18.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 8.dp, bottom = 18.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                if (isEmpty) {
                    AiStepsRow(colors)
                    Spacer(modifier = Modifier.height(20.dp))
                }
                Button(
                    onClick = onActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isEmpty) colors.sky else colors.primary)
                ) {
                    if (isEmpty) AiGlyph(Color.White)
                    Text(
                        text = actionText,
                        modifier = Modifier.padding(start = if (isEmpty) 8.dp else 0.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AiStepsRow(colors: AiListPalette) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepPill("Elige campana", colors, Modifier.weight(1f))
        StepDivider(colors)
        StepPill("Prepara el prompt", colors, Modifier.weight(1f))
        StepDivider(colors)
        StepPill("Genera el video", colors, Modifier.weight(1f))
    }
}

@Composable
private fun StepPill(text: String, colors: AiListPalette, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.soft,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StepDivider(colors: AiListPalette) {
    Text(">", color = colors.sky, fontWeight = FontWeight.Bold)
}

@Composable
private fun AiGlyph(color: Color) {
    Canvas(modifier = Modifier.size(26.dp)) {
        drawLine(color, Offset(13.dp.toPx(), 3.dp.toPx()), Offset(13.dp.toPx(), 9.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(13.dp.toPx(), 17.dp.toPx()), Offset(13.dp.toPx(), 23.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(3.dp.toPx(), 13.dp.toPx()), Offset(9.dp.toPx(), 13.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(17.dp.toPx(), 13.dp.toPx()), Offset(23.dp.toPx(), 13.dp.toPx()), 2.dp.toPx())
        drawCircle(color.copy(alpha = 0.22f), 6.dp.toPx(), Offset(13.dp.toPx(), 13.dp.toPx()), style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun BackGlyph(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawLine(color, Offset(19.dp.toPx(), 12.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx())
        drawLine(color, Offset(11.dp.toPx(), 7.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx())
        drawLine(color, Offset(11.dp.toPx(), 17.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx())
    }
}

@Composable
private fun AddGlyph(color: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        drawLine(color, Offset(10.dp.toPx(), 4.dp.toPx()), Offset(10.dp.toPx(), 16.dp.toPx()), 2.2.dp.toPx())
        drawLine(color, Offset(4.dp.toPx(), 10.dp.toPx()), Offset(16.dp.toPx(), 10.dp.toPx()), 2.2.dp.toPx())
    }
}

@Composable
private fun ErrorGlyph(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color.copy(alpha = 0.12f), 11.dp.toPx(), Offset(size.width / 2f, size.height / 2f))
        drawLine(color, Offset(9.dp.toPx(), 9.dp.toPx()), Offset(19.dp.toPx(), 19.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(19.dp.toPx(), 9.dp.toPx()), Offset(9.dp.toPx(), 19.dp.toPx()), 2.dp.toPx())
    }
}

@Immutable
private data class AiListPalette(
    val background: Color = Color(0xFFF8FAFC),
    val surface: Color = Color.White,
    val soft: Color = Color(0xFFEFF6FF),
    val textPrimary: Color = Color(0xFF0F172A),
    val textSecondary: Color = Color(0xFF64748B),
    val primary: Color = Color(0xFF2563EB),
    val sky: Color = Color(0xFF0EA5E9),
    val skySoft: Color = Color(0xFFE0F2FE),
    val border: Color = Color(0xFFD7E3F0),
    val error: Color = Color(0xFFB42318),
    val errorSoft: Color = Color(0xFFFFF1F2)
)

private val aiListPalette = AiListPalette()
