package com.tecsup.visionastra.mobile.ui.resources

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.tecsup.visionastra.mobile.core.network.authenticatedImageLoader
import com.tecsup.visionastra.mobile.core.util.formatMb
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar
import com.tecsup.visionastra.mobile.ui.resources.components.ResourceTypeChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceDetailScreen(
    state: ResourceDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onVideoClick: (Int) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onDeleteConfirm: () -> Unit,
    onSnackbarShown: () -> Unit
) {
    val colors = detailPalette
    var showTitleDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var titleDraft by remember(state.resource?.titulo) { mutableStateOf(state.resource?.titulo.orEmpty()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onSnackbarShown()
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = "Detalle del recurso",
                subtitle = "Consulta y administra este contenido",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            state.resource == null -> Column(Modifier.fillMaxSize().padding(innerPadding).padding(24.dp), verticalArrangement = Arrangement.Center) {
                Text(state.errorMessage ?: "No se pudo cargar el recurso", color = colors.error)
                Button(onClick = onRetryClick, modifier = Modifier.padding(top = 16.dp)) { Text("Reintentar") }
            }
            else -> {
                val resource = state.resource
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ResourcePreview(resourceType = ResourceType.fromValue(resource.tipo), title = resource.displayTitle(), fileUrl = state.fileUrl, onVideoClick = { onVideoClick(resource.idRecurso) }, colors = colors)
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border), shadowElevation = 2.dp) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                                Text(resource.displayTitle(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                ResourceTypeChip(type = resource.tipo)
                            }
                            if (ResourceType.fromValue(resource.tipo) == ResourceType.Copy) {
                                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.soft) {
                                    Text(resource.contenidoTexto.orEmpty(), modifier = Modifier.padding(14.dp), color = colors.textPrimary)
                                }
                            }
                            state.errorMessage?.let { Text(it, color = colors.error) }
                            MetaRow("Archivo", resource.nombreArchivo, colors)
                            HorizontalDivider(color = colors.border)
                            MetaRow("Formato", resource.formato ?: "No disponible", colors)
                            HorizontalDivider(color = colors.border)
                            MetaRow("Tamano", resource.pesoMb.formatMb().takeUnless { it.contains("disponible", true) } ?: "No disponible", colors)
                            HorizontalDivider(color = colors.border)
                            MetaRow("Estado", resource.estado, colors)
                            HorizontalDivider(color = colors.border)
                            MetaRow("Fecha de subida", resource.fechaSubida.formatResourceDate(), colors)
                        }
                    }
                    Button(onClick = { showTitleDialog = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
                        EditGlyph(Color.White)
                        Text("Editar titulo", modifier = Modifier.padding(start = 8.dp))
                    }
                    if (ResourceType.fromValue(resource.tipo) == ResourceType.Video) {
                        OutlinedButton(onClick = { onVideoClick(resource.idRecurso) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, colors.border)) {
                            PlayGlyph(colors.primary)
                            Text("Reproducir video", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth(), enabled = !state.isDeleting, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error), border = BorderStroke(1.dp, colors.error.copy(alpha = 0.35f))) {
                        Text(if (state.isDeleting) "Eliminando..." else "Eliminar recurso")
                    }
                }
            }
        }
    }

    if (showTitleDialog) {
        AlertDialog(
            onDismissRequest = { showTitleDialog = false },
            title = { Text("Editar titulo") },
            text = { OutlinedTextField(value = titleDraft, onValueChange = { titleDraft = it }, label = { Text("Titulo") }, singleLine = true) },
            confirmButton = { Button(onClick = { showTitleDialog = false; onUpdateTitle(titleDraft) }, enabled = !state.isUpdatingTitle) { Text("Guardar") } },
            dismissButton = { TextButton(onClick = { showTitleDialog = false }) { Text("Cancelar") } }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar recurso") },
            text = { Text("Eliminar este recurso? Esta accion no se puede deshacer dentro de la aplicacion.") },
            confirmButton = { Button(onClick = { showDeleteDialog = false; onDeleteConfirm() }, enabled = !state.isDeleting, colors = ButtonDefaults.buttonColors(containerColor = colors.error)) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ResourcePreview(resourceType: ResourceType, title: String, fileUrl: String?, onVideoClick: () -> Unit, colors: DetailPalette) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border), shadowElevation = 2.dp) {
        when (resourceType) {
            ResourceType.Image -> {
                val context = LocalContext.current
                AsyncImage(model = fileUrl, imageLoader = remember { authenticatedImageLoader(context) }, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f).padding(10.dp))
            }
            ResourceType.Video -> Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f), contentAlignment = Alignment.Center) {
                Button(onClick = onVideoClick, shape = RoundedCornerShape(16.dp)) { PlayGlyph(Color.White); Text("Reproducir", modifier = Modifier.padding(start = 8.dp)) }
            }
            ResourceType.Copy -> Text("Contenido de la idea", modifier = Modifier.padding(16.dp), color = colors.textSecondary)
            ResourceType.Document -> Text("Documento disponible en la version web", modifier = Modifier.padding(16.dp), color = colors.textSecondary)
        }
    }
}

@Composable private fun MetaRow(label: String, value: String, colors: DetailPalette) { Column(verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(label, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary); Text(value, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary) } }
@Composable private fun BackGlyph(color: Color) { Canvas(Modifier.size(24.dp)) { drawLine(color, Offset(19.dp.toPx(), 12.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 7.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 17.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()) } }
@Composable private fun EditGlyph(color: Color) { Canvas(Modifier.size(18.dp)) { drawLine(color, Offset(5.dp.toPx(), 13.dp.toPx()), Offset(13.dp.toPx(), 5.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(12.dp.toPx(), 4.dp.toPx()), Offset(15.dp.toPx(), 7.dp.toPx()), 2.dp.toPx()) } }
@Composable private fun PlayGlyph(color: Color) { Canvas(Modifier.size(18.dp)) { val path = androidx.compose.ui.graphics.Path().apply { moveTo(6.dp.toPx(), 3.dp.toPx()); lineTo(6.dp.toPx(), 15.dp.toPx()); lineTo(15.dp.toPx(), 9.dp.toPx()); close() }; drawPath(path, color) } }

private data class DetailPalette(val background: Color = Color(0xFFF8FAFC), val surface: Color = Color.White, val soft: Color = Color(0xFFEFF6FF), val textPrimary: Color = Color(0xFF0F172A), val textSecondary: Color = Color(0xFF64748B), val primary: Color = Color(0xFF3B82F6), val border: Color = Color(0xFFDCE5F0), val error: Color = Color(0xFFB42318))
private val detailPalette = DetailPalette()
