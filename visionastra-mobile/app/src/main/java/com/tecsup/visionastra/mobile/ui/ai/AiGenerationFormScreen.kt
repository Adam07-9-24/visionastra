package com.tecsup.visionastra.mobile.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.data.remote.dto.CampaignResponse
import com.tecsup.visionastra.mobile.ui.ai.components.ResourceSelectionCard
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGenerationFormScreen(
    state: AiGenerationFormUiState,
    onBackClick: () -> Unit,
    onCampaignSelected: (Int) -> Unit,
    onResourceToggle: (Int) -> Unit,
    onPromptChange: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    val colors = aiFormPalette
    Scaffold(
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = "Nueva generación IA",
                subtitle = "Convierte tu idea en contenido",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                state.errorMessage?.let { ErrorBanner(message = it, colors = colors) }
            }
            item {
                CampaignSelectionCard(
                    selectedCampaign = state.selectedCampaign,
                    campaigns = state.campaigns,
                    isCreating = state.isCreating,
                    colors = colors,
                    onCampaignSelected = onCampaignSelected
                )
            }
            item {
                PromptSection(
                    prompt = state.prompt,
                    isCreating = state.isCreating,
                    colors = colors,
                    onPromptChange = onPromptChange
                )
            }
            item {
                SectionHeader(
                    title = "Recursos para la generacion",
                    description = "Selecciona imagenes o ideas de esta campana para orientar a la IA.",
                    colors = colors
                )
            }
            if (state.inputResources.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Text(
                            text = "Esta campana no tiene imagenes o ideas activas para seleccionar.",
                            modifier = Modifier.padding(16.dp),
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(state.inputResources, key = { it.idRecurso }) { resource ->
                    ResourceSelectionCard(
                        resource = resource,
                        selected = state.selectedResourceIds.contains(resource.idRecurso),
                        onClick = { onResourceToggle(resource.idRecurso) }
                    )
                }
            }
            item {
                NextStepInfo(colors = colors)
            }
            item {
                Button(
                    onClick = onCreateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !state.isCreating,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.sky)
                ) {
                    if (state.isCreating) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text("Creando generacion...")
                        }
                    } else {
                        SparkGlyph(Color.White)
                        Text(
                            text = "Crear generacion",
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignSelectionCard(
    selectedCampaign: CampaignResponse?,
    campaigns: List<CampaignResponse>,
    isCreating: Boolean,
    colors: AiFormPalette,
    onCampaignSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.soft,
        border = BorderStroke(1.dp, colors.sky.copy(alpha = 0.35f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CampaignGlyph(colors.primary)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Campana seleccionada", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                    Text(
                        text = selectedCampaign?.nombre ?: "Selecciona una campana activa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, colors.sky.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "Activa",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (campaigns.size > 1) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cambiar campana", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                    campaigns.forEach { campaign ->
                        OutlinedButton(
                            onClick = { onCampaignSelected(campaign.idCampana) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isCreating,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (campaign.idCampana == selectedCampaign?.idCampana) colors.primary else colors.border)
                        ) {
                            Text(
                                text = campaign.nombre,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptSection(
    prompt: String,
    isCreating: Boolean,
    colors: AiFormPalette,
    onPromptChange: (String) -> Unit
) {
    val count = prompt.trim().length
    val valid = count >= 10
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader(
                title = "Describe el video que deseas crear",
                description = "Explica la escena, el estilo, el mensaje y la emocion que quieres transmitir.",
                colors = colors
            )
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Idea para el video") },
                placeholder = {
                    Text("Ejemplo: crea un video dinamico y alegre donde el producto sea el protagonista, con un estilo moderno y juvenil...")
                },
                minLines = 5,
                supportingText = {
                    Text(
                        text = "$count caracteres - minimo 10",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = if (valid) colors.textSecondary else colors.error
                    )
                },
                enabled = !isCreating,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor = colors.primary,
                    cursorColor = colors.primary,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )
            Text(
                text = "VisionAstra convertira tu idea en un prompt optimizado para generar el video.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    description: String,
    colors: AiFormPalette
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
    }
}

@Composable
private fun NextStepInfo(colors: AiFormPalette) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.soft,
        border = BorderStroke(1.dp, colors.sky.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoGlyph(colors.primary)
            Text(
                text = "Despues de crear la generacion podras preparar el prompt, revisarlo y decidir cuando generar el video.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    colors: AiFormPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.errorSoft,
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.24f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = colors.error
        )
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
private fun CampaignGlyph(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(color, Offset(3.dp.toPx(), 5.dp.toPx()), Size(16.dp.toPx(), 12.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()), style = stroke)
        drawLine(color, Offset(7.dp.toPx(), 10.dp.toPx()), Offset(15.dp.toPx(), 10.dp.toPx()), 1.6.dp.toPx())
        drawLine(color, Offset(7.dp.toPx(), 14.dp.toPx()), Offset(13.dp.toPx(), 14.dp.toPx()), 1.6.dp.toPx())
    }
}

@Composable
private fun SparkGlyph(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        drawLine(color, Offset(9.dp.toPx(), 2.dp.toPx()), Offset(9.dp.toPx(), 6.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(9.dp.toPx(), 12.dp.toPx()), Offset(9.dp.toPx(), 16.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(2.dp.toPx(), 9.dp.toPx()), Offset(6.dp.toPx(), 9.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(12.dp.toPx(), 9.dp.toPx()), Offset(16.dp.toPx(), 9.dp.toPx()), 2.dp.toPx())
    }
}

@Composable
private fun InfoGlyph(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        drawCircle(color.copy(alpha = 0.16f), 9.dp.toPx(), Offset(11.dp.toPx(), 11.dp.toPx()))
        drawCircle(color, 1.3.dp.toPx(), Offset(11.dp.toPx(), 7.dp.toPx()))
        drawLine(color, Offset(11.dp.toPx(), 10.dp.toPx()), Offset(11.dp.toPx(), 15.dp.toPx()), 1.8.dp.toPx())
    }
}

@Immutable
private data class AiFormPalette(
    val background: Color = Color(0xFFF8FAFC),
    val surface: Color = Color.White,
    val soft: Color = Color(0xFFEFF6FF),
    val textPrimary: Color = Color(0xFF0F172A),
    val textSecondary: Color = Color(0xFF64748B),
    val primary: Color = Color(0xFF2563EB),
    val sky: Color = Color(0xFF0EA5E9),
    val border: Color = Color(0xFFD7E3F0),
    val error: Color = Color(0xFFB42318),
    val errorSoft: Color = Color(0xFFFFF1F2)
)

private val aiFormPalette = AiFormPalette()
