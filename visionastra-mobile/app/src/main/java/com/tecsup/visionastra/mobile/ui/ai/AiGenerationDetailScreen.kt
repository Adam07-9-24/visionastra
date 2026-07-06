package com.tecsup.visionastra.mobile.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.data.remote.dto.AiGenerationResponse
import com.tecsup.visionastra.mobile.ui.ai.components.GenerationResultSection
import com.tecsup.visionastra.mobile.ui.ai.components.GenerationStatusChip
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGenerationDetailScreen(
    state: AiGenerationDetailUiState,
    onBackClick: () -> Unit,
    onCreatePromptClick: () -> Unit,
    onGenerateVideoRequest: () -> Unit,
    onGenerateVideoConfirm: () -> Unit,
    onGenerateVideoCancel: () -> Unit,
    onPlayVideoClick: (Int) -> Unit
) {
    val colors = aiDetailPalette
    Scaffold(
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = "Detalle de generación",
                subtitle = "Revisa cada etapa del proceso",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        val generation = state.generation
        when {
            state.isLoading && generation == null -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            generation == null -> Column(Modifier.fillMaxSize().padding(innerPadding).padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text(state.errorMessage ?: "No se pudo cargar la generacion", color = colors.error)
            }
            else -> {
                val showProgress = state.isPreparingPrompt || state.shouldShowVideoInProgress
                val canCreatePrompt = !state.hasPreparedPrompt && !state.isPreparingPrompt && !state.isGeneratingVideo && generation.estado != "completado"
                val canGenerateVideo = state.hasPreparedPrompt &&
                    !state.isPreparingPrompt &&
                    !state.isGeneratingVideo &&
                    !state.shouldShowVideoInProgress &&
                    generation.estado !in setOf("completado", "error")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GenerationHeader(generation, colors)
                    StageIndicator(
                        promptDone = state.hasPreparedPrompt,
                        promptActive = state.isPreparingPrompt || (!state.hasPreparedPrompt && generation.estado == "procesando"),
                        videoActive = state.shouldShowVideoInProgress,
                        videoDone = state.hasCompletedVideo,
                        colors = colors
                    )
                    if (showProgress) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = colors.primary)
                    Text(detailMessage(state), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    state.errorMessage?.let { ErrorBox(it, colors) }
                    if (generation.estado == "error" && !generation.mensajeError.isNullOrBlank()) ErrorBox(generation.mensajeError, colors)
                    InfoCard("Idea para el video", generation.prompt, colors)
                    InfoCard("Recursos seleccionados", generation.recursosEntrada.joinToString("\n") { "${it.titulo ?: it.nombreArchivo ?: it.idRecurso} (${it.tipo.displayResourceType()})" }, colors)
                    GenerationResultSection("Resumen de contexto", generation.resumenContexto)
                    GenerationResultSection("Guion generado", generation.guionGenerado)
                    GenerationResultSection("Prompt final en espanol", generation.promptFinalEspanol)
                    GenerationResultSection("Prompt tecnico en ingles", generation.promptFinal)
                    if (!state.hasPreparedPrompt && generation.estado != "error") {
                        Button(onClick = onCreatePromptClick, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = canCreatePrompt, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.sky)) {
                            if (!state.isPreparingPrompt) SparkGlyph(Color.White)
                            Text(if (state.isPreparingPrompt) "Creando prompt..." else "Crear prompt", modifier = Modifier.padding(start = if (state.isPreparingPrompt) 0.dp else 8.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (state.hasPreparedPrompt && generation.estado !in setOf("completado", "error")) {
                        Button(onClick = onGenerateVideoRequest, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = canGenerateVideo, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
                            if (!state.isGeneratingVideo) SparkGlyph(Color.White)
                            Text(if (state.isGeneratingVideo) "Generando video..." else "Generar video", modifier = Modifier.padding(start = if (state.isGeneratingVideo) 0.dp else 8.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (generation.estado == "completado" && generation.idRecursoResultado != null) {
                        OutlinedButton(onClick = { onPlayVideoClick(generation.idRecursoResultado) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, colors.sky)) {
                            PlayGlyph(colors.sky)
                            Text("Reproducir video", modifier = Modifier.padding(start = 8.dp), color = colors.sky, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
    if (state.showGenerateVideoConfirmation) {
        AlertDialog(
            onDismissRequest = onGenerateVideoCancel,
            title = { Text("Generar video") },
            text = { Text("¿Deseas generar el video con el prompt preparado? Esta acción puede consumir cuota del servicio de IA.") },
            confirmButton = { Button(onClick = onGenerateVideoConfirm, colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) { Text("Generar video") } },
            dismissButton = { TextButton(onClick = onGenerateVideoCancel) { Text("Cancelar") } }
        )
    }
}

@Composable private fun GenerationHeader(g: AiGenerationResponse, colors: AiDetailPalette) { Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = colors.soft, border = BorderStroke(1.dp, colors.sky.copy(alpha = .35f))) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) { Text(g.nombreCampana ?: "Campana ${g.idCampana}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary); GenerationStatusChip(g.estado) }; Text("${statusTitle(g.estado)} · ${g.tipoSalida} · ${g.fechaCreacion.formatAiDate()}", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium) } } }
@Composable private fun StageIndicator(promptDone: Boolean, promptActive: Boolean, videoActive: Boolean, videoDone: Boolean, colors: AiDetailPalette) { Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) { Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { StageItem("Creada", done = true, active = false, colors); StageItem("Prompt", done = promptDone, active = promptActive, colors); StageItem("Video", done = videoDone, active = videoActive, colors) } } }
@Composable private fun StageItem(label: String, done: Boolean, active: Boolean, colors: AiDetailPalette) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Canvas(Modifier.size(18.dp)) { val color = when { done -> colors.primary; active -> colors.sky; else -> colors.border }; drawCircle(color.copy(alpha = if (active && !done) .18f else 1f), 8.dp.toPx(), Offset(size.width / 2f, size.height / 2f)); if (active && !done) drawCircle(color, 4.dp.toPx(), Offset(size.width / 2f, size.height / 2f)) }; Text(label, color = when { done -> colors.primary; active -> colors.sky; else -> colors.textSecondary }, style = MaterialTheme.typography.labelMedium, fontWeight = if (active || done) FontWeight.SemiBold else FontWeight.Normal) } }
@Composable private fun InfoCard(title: String, value: String, colors: AiDetailPalette) { Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, fontWeight = FontWeight.Bold, color = colors.textPrimary); Text(value.ifBlank { "Sin informacion" }, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium) } } }
@Composable private fun ErrorBox(message: String, colors: AiDetailPalette) { Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.errorSoft, border = BorderStroke(1.dp, colors.error.copy(alpha = .25f))) { Text(message, modifier = Modifier.padding(14.dp), color = colors.error) } }
@Composable private fun BackGlyph(color: Color) { Canvas(Modifier.size(24.dp)) { drawLine(color, Offset(19.dp.toPx(), 12.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 7.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 17.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()) } }
@Composable private fun SparkGlyph(color: Color) { Canvas(Modifier.size(18.dp)) { drawLine(color, Offset(9.dp.toPx(), 2.dp.toPx()), Offset(9.dp.toPx(), 6.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(9.dp.toPx(), 12.dp.toPx()), Offset(9.dp.toPx(), 16.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(2.dp.toPx(), 9.dp.toPx()), Offset(6.dp.toPx(), 9.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(12.dp.toPx(), 9.dp.toPx()), Offset(16.dp.toPx(), 9.dp.toPx()), 2.dp.toPx()) } }
@Composable private fun PlayGlyph(color: Color) { Canvas(Modifier.size(18.dp)) { val path = androidx.compose.ui.graphics.Path().apply { moveTo(6.dp.toPx(), 4.dp.toPx()); lineTo(14.dp.toPx(), 9.dp.toPx()); lineTo(6.dp.toPx(), 14.dp.toPx()); close() }; drawPath(path, color) } }
private fun statusTitle(status: String): String = when (status) { "pendiente" -> "Generacion creada"; "procesando" -> "Procesando contenido"; "completado" -> "Video completado"; "error" -> "No se pudo completar"; else -> status }
private fun String.displayResourceType(): String = when (this) { "copy" -> "Idea"; "imagen" -> "Imagen"; "video" -> "Video"; "documento" -> "Documento"; else -> this }
private fun detailMessage(state: AiGenerationDetailUiState): String { val generation = state.generation ?: return ""; return when { state.isPreparingPrompt -> "VisionAstra esta creando el resumen, guion y prompt."; state.shouldShowVideoInProgress -> "Estamos generando tu video"; generation.estado == "completado" -> "Tu video esta listo"; state.hasPreparedPrompt -> "Prompt preparado. Revisalo antes de generar el video."; generation.estado == "procesando" -> "Procesando la generacion"; generation.estado == "error" -> "No se pudo completar la generacion"; else -> "Generacion creada. Prepara el prompt para continuar." } }
private data class AiDetailPalette(val background: Color = Color(0xFFF8FAFC), val surface: Color = Color.White, val soft: Color = Color(0xFFEFF6FF), val textPrimary: Color = Color(0xFF0F172A), val textSecondary: Color = Color(0xFF64748B), val primary: Color = Color(0xFF2563EB), val sky: Color = Color(0xFF0EA5E9), val border: Color = Color(0xFFD7E3F0), val error: Color = Color(0xFFB42318), val errorSoft: Color = Color(0xFFFFF1F2))
private val aiDetailPalette = AiDetailPalette()
