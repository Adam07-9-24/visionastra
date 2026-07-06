package com.tecsup.visionastra.mobile.ui.resources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.tecsup.visionastra.mobile.core.network.authenticatedImageLoader
import com.tecsup.visionastra.mobile.core.util.formatMb
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
    var showTitleDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var titleDraft by remember(state.resource?.titulo) {
        mutableStateOf(state.resource?.titulo.orEmpty())
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurso") },
                navigationIcon = { TextButton(onClick = onBackClick) { Text("Atrás") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            state.isLoading -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            state.resource == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                Text(state.errorMessage ?: "No se pudo cargar el recurso")
                Button(onClick = onRetryClick, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Reintentar")
                }
            }
            else -> {
                val resource = state.resource
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(resource.displayTitle(), style = MaterialTheme.typography.headlineSmall)
                    ResourceTypeChip(type = resource.tipo)
                    state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    when (ResourceType.fromValue(resource.tipo)) {
                        ResourceType.Image -> {
                            val context = LocalContext.current
                            AsyncImage(
                                model = state.fileUrl,
                                imageLoader = remember { authenticatedImageLoader(context) },
                                contentDescription = resource.displayTitle(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                            )
                        }
                        ResourceType.Copy -> Text(resource.contenidoTexto.orEmpty())
                        ResourceType.Video -> Button(onClick = { onVideoClick(resource.idRecurso) }) {
                            Text("Reproducir video")
                        }
                        ResourceType.Document -> Text("Documento no disponible en la aplicación móvil")
                    }
                    Text("Archivo: ${resource.nombreArchivo}")
                    Text("Formato: ${resource.formato ?: "No disponible"}")
                    Text("Tamaño: ${resource.pesoMb.formatMb()}")
                    Text("Estado: ${resource.estado}")
                    Text("Fecha de subida: ${resource.fechaSubida.formatResourceDate()}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { showTitleDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Editar título")
                    }
                    OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.isDeleting) "Eliminando..." else "Eliminar")
                    }
                }
            }
        }
    }

    if (showTitleDialog) {
        AlertDialog(
            onDismissRequest = { showTitleDialog = false },
            title = { Text("Editar título") },
            text = {
                OutlinedTextField(
                    value = titleDraft,
                    onValueChange = { titleDraft = it },
                    label = { Text("Título") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTitleDialog = false
                        onUpdateTitle(titleDraft)
                    },
                    enabled = !state.isUpdatingTitle
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showTitleDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar recurso") },
            text = { Text("¿Eliminar este recurso? Esta acción no se puede deshacer dentro de la aplicación.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteConfirm()
                    },
                    enabled = !state.isDeleting
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
