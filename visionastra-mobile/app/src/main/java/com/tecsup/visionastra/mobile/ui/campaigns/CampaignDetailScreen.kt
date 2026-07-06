package com.tecsup.visionastra.mobile.ui.campaigns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.data.remote.dto.CampaignResponse
import com.tecsup.visionastra.mobile.ui.campaigns.components.CampaignStatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailScreen(
    state: CampaignDetailUiState,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onResourcesClick: (Int) -> Unit,
    onStatusChange: (CampaignStatus) -> Unit,
    onDeleteConfirm: () -> Unit,
    onSnackbarShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusSheet by remember { mutableStateOf(false) }
    var pendingStatus by remember { mutableStateOf<CampaignStatus?>(null) }
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
        topBar = {
            TopAppBar(
                title = { Text("Detalle de campaña") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null && state.campaign == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.errorMessage)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetryClick) {
                        Text("Reintentar")
                    }
                }
            }

            state.campaign != null -> {
                CampaignDetailContent(
                    campaign = state.campaign,
                    errorMessage = state.errorMessage,
                    isChangingStatus = state.isChangingStatus,
                    isDeleting = state.isDeleting,
                    onEditClick = { onEditClick(state.campaign.idCampana) },
                    onResourcesClick = { onResourcesClick(state.campaign.idCampana) },
                    onChangeStatusClick = { showStatusSheet = true },
                    onDeleteClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                )
            }
        }
    }

    if (showStatusSheet && state.campaign != null) {
        StatusBottomSheet(
            currentStatus = CampaignStatus.fromValue(state.campaign.estado),
            isChangingStatus = state.isChangingStatus,
            onDismiss = { showStatusSheet = false },
            onStatusSelected = { status ->
                if (status == CampaignStatus.Finished) {
                    pendingStatus = status
                } else {
                    showStatusSheet = false
                    onStatusChange(status)
                }
            }
        )
    }

    if (pendingStatus != null) {
        AlertDialog(
            onDismissRequest = { pendingStatus = null },
            title = { Text("Finalizar campaña") },
            text = { Text("Confirma que deseas marcar esta campaña como finalizada.") },
            confirmButton = {
                Button(
                    onClick = {
                        val status = pendingStatus
                        pendingStatus = null
                        showStatusSheet = false
                        if (status != null) onStatusChange(status)
                    }
                ) {
                    Text("Finalizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingStatus = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar campaña") },
            text = { Text("¿Eliminar esta campaña? Esta acción puede afectar sus recursos relacionados.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteConfirm()
                    },
                    enabled = !state.isDeleting
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CampaignDetailContent(
    campaign: CampaignResponse,
    errorMessage: String?,
    isChangingStatus: Boolean,
    isDeleting: Boolean,
    onEditClick: () -> Unit,
    onResourcesClick: () -> Unit,
    onChangeStatusClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = campaign.nombre,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            CampaignStatusChip(status = campaign.estado)
        }
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        DetailField("Objetivo", campaign.objetivo ?: "Sin objetivo")
        DetailField("Descripción", campaign.descripcion ?: "Sin descripción")
        DetailField("Presupuesto", campaign.presupuesto.formatBudget())
        DetailField("Fecha de inicio", campaign.fechaInicio.formatCampaignDateTime())
        DetailField("Fecha de fin", campaign.fechaFin.formatCampaignDateTime())
        DetailField("Fecha de creación", campaign.fechaCreacion.formatCampaignDateTime())
        DetailField("Última actualización", campaign.fechaActualizacion.formatCampaignDateTime())
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Editar")
        }
        OutlinedButton(
            onClick = onResourcesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver recursos")
        }
        OutlinedButton(
            onClick = onChangeStatusClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isChangingStatus
        ) {
            Text(if (isChangingStatus) "Actualizando..." else "Cambiar estado")
        }
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDeleting
        ) {
            Text(if (isDeleting) "Eliminando..." else "Eliminar")
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusBottomSheet(
    currentStatus: CampaignStatus,
    isChangingStatus: Boolean,
    onDismiss: () -> Unit,
    onStatusSelected: (CampaignStatus) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Estado actual: ${currentStatus.label}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            campaignStatuses
                .filter { it != currentStatus }
                .forEach { status ->
                    OutlinedButton(
                        onClick = { onStatusSelected(status) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChangingStatus
                    ) {
                        Text(status.label)
                    }
                }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
