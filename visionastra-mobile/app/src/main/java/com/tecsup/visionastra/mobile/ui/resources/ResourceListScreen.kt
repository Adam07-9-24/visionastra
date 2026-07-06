package com.tecsup.visionastra.mobile.ui.resources

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBarVariant
import com.tecsup.visionastra.mobile.ui.resources.components.ResourceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceListScreen(
    state: ResourceListUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onTypeSelected: (String?) -> Unit,
    onUploadImageClick: () -> Unit,
    onCreateCopyClick: () -> Unit,
    onResourceClick: (Int, String) -> Unit,
    onSnackbarShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = resourcePalette
    var showCreateSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onSnackbarShown()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = "Recursos",
                subtitle = "Imágenes, ideas y videos de tu campaña",
                onBackClick = onBackClick,
                variant = VisionAstraTopAppBarVariant.Module
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = colors.primary,
                contentColor = Color.White,
                icon = { AddGlyph(Color.White) },
                text = { Text("Agregar recurso", fontWeight = FontWeight.SemiBold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CampaignHeader(name = state.campaignName ?: "Campana", colors = colors)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(resourceTypeFilters, key = { it.label }) { filter ->
                    val selected = state.selectedType == filter.value
                    FilterChip(
                        selected = selected,
                        onClick = { onTypeSelected(filter.value) },
                        label = { Text(filter.label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.surface,
                            labelColor = colors.textSecondary,
                            selectedContainerColor = colors.soft,
                            selectedLabelColor = colors.primary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = colors.border,
                            selectedBorderColor = colors.primary
                        )
                    )
                }
            }
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
                state.errorMessage != null -> ResourceMessageState(
                    title = "No se pudieron cargar los recursos",
                    message = state.errorMessage,
                    actionText = "Reintentar",
                    onActionClick = onRetryClick,
                    colors = colors
                )
                state.resources.isEmpty() -> ResourceMessageState(
                    title = "Esta campana todavia no tiene recursos",
                    message = "Agrega una imagen o idea para orientar la generacion de contenido.",
                    actionText = "Agregar recurso",
                    onActionClick = { showCreateSheet = true },
                    colors = colors
                )
                state.filteredResources.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay recursos para este filtro", color = colors.textSecondary)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredResources, key = { it.idRecurso }) { resource ->
                        ResourceCard(
                            resource = resource,
                            onClick = { onResourceClick(resource.idRecurso, resource.tipo) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            containerColor = colors.surface
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 4.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Text("Agregar recurso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Text(
                    "Selecciona el tipo de contenido que deseas agregar a la campana.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                SheetOption(
                    title = "Subir imagen",
                    description = "Selecciona una imagen desde tu dispositivo.",
                    colors = colors,
                    icon = { ImageGlyph(colors.primary) },
                    onClick = {
                        showCreateSheet = false
                        onUploadImageClick()
                    }
                )
                SheetOption(
                    title = "Crear idea",
                    description = "Agrega un texto, mensaje o concepto para orientar la creacion de contenido.",
                    colors = colors,
                    icon = { TextGlyph(colors.primary) },
                    onClick = {
                        showCreateSheet = false
                        onCreateCopyClick()
                    }
                )
                Box(modifier = Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun CampaignHeader(name: String, colors: ResourcePalette) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(22.dp),
        color = colors.soft,
        border = BorderStroke(1.dp, colors.sky.copy(alpha = 0.35f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SoftIcon(colors) { CampaignGlyph(colors.primary) }
            Column {
                Text("Campana", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }
        }
    }
}

@Composable
private fun SheetOption(title: String, description: String, colors: ResourcePalette, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SoftIcon(colors, icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                Text(description, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            ArrowGlyph(colors.sky)
        }
    }
}

@Composable
private fun ResourceMessageState(title: String, message: String, actionText: String, onActionClick: () -> Unit, colors: ResourcePalette) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SoftIcon(colors) { ImageGlyph(colors.primary) }
        Text(title, modifier = Modifier.padding(top = 18.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary, textAlign = TextAlign.Center)
        Text(message, modifier = Modifier.padding(top = 8.dp, bottom = 22.dp), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, textAlign = TextAlign.Center)
        Button(onClick = onActionClick, shape = RoundedCornerShape(16.dp)) { Text(actionText) }
    }
}

@Composable private fun SoftIcon(colors: ResourcePalette, content: @Composable () -> Unit) { Surface(Modifier.size(44.dp), shape = RoundedCornerShape(14.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) { Box(contentAlignment = Alignment.Center) { content() } } }
@Composable private fun BackGlyph(color: Color) { Canvas(Modifier.size(24.dp)) { drawLine(color, Offset(19.dp.toPx(), 12.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 7.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 17.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()) } }
@Composable private fun AddGlyph(color: Color) { Canvas(Modifier.size(20.dp)) { drawLine(color, Offset(10.dp.toPx(), 4.dp.toPx()), Offset(10.dp.toPx(), 16.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(4.dp.toPx(), 10.dp.toPx()), Offset(16.dp.toPx(), 10.dp.toPx()), 2.2.dp.toPx()) } }
@Composable private fun ArrowGlyph(color: Color) { Canvas(Modifier.size(18.dp)) { drawLine(color, Offset(4.dp.toPx(), 9.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(10.dp.toPx(), 5.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(10.dp.toPx(), 13.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx()) } }
@Composable private fun CampaignGlyph(color: Color) { Canvas(Modifier.size(22.dp)) { val s = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round); drawRoundRect(color, Offset(3.dp.toPx(), 5.dp.toPx()), Size(16.dp.toPx(), 12.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()), style = s); drawLine(color, Offset(7.dp.toPx(), 10.dp.toPx()), Offset(15.dp.toPx(), 10.dp.toPx()), 1.6.dp.toPx()) } }
@Composable private fun ImageGlyph(color: Color) { Canvas(Modifier.size(22.dp)) { val s = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round); drawRoundRect(color, Offset(3.dp.toPx(), 5.dp.toPx()), Size(16.dp.toPx(), 12.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()), style = s); drawCircle(color.copy(alpha = 0.22f), 2.dp.toPx(), Offset(8.dp.toPx(), 9.dp.toPx()), style = s); drawLine(color, Offset(6.dp.toPx(), 16.dp.toPx()), Offset(11.dp.toPx(), 12.dp.toPx()), 1.5.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 12.dp.toPx()), Offset(17.dp.toPx(), 16.dp.toPx()), 1.5.dp.toPx()) } }
@Composable private fun TextGlyph(color: Color) { Canvas(Modifier.size(22.dp)) { drawLine(color, Offset(4.dp.toPx(), 7.dp.toPx()), Offset(18.dp.toPx(), 7.dp.toPx()), 1.9.dp.toPx()); drawLine(color, Offset(4.dp.toPx(), 12.dp.toPx()), Offset(18.dp.toPx(), 12.dp.toPx()), 1.9.dp.toPx()); drawLine(color, Offset(4.dp.toPx(), 17.dp.toPx()), Offset(13.dp.toPx(), 17.dp.toPx()), 1.9.dp.toPx()) } }

@Immutable
private data class ResourcePalette(
    val background: Color = Color(0xFFF8FAFC),
    val surface: Color = Color.White,
    val soft: Color = Color(0xFFEFF6FF),
    val textPrimary: Color = Color(0xFF0F172A),
    val textSecondary: Color = Color(0xFF64748B),
    val primary: Color = Color(0xFF3B82F6),
    val sky: Color = Color(0xFF0EA5E9),
    val border: Color = Color(0xFFDCE5F0)
)

private val resourcePalette = ResourcePalette()
