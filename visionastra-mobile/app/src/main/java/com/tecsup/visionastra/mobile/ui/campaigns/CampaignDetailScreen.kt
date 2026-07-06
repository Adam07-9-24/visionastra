package com.tecsup.visionastra.mobile.ui.campaigns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.data.remote.dto.CampaignResponse
import com.tecsup.visionastra.mobile.ui.campaigns.components.CampaignStatusChip
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailScreen(
    state: CampaignDetailUiState,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onResourcesClick: (Int) -> Unit,
    onGenerateAiClick: (Int, Boolean) -> Unit,
    onStatusChange: (CampaignStatus) -> Unit,
    onDeleteConfirm: () -> Unit,
    onSnackbarShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = campaignDetailPalette
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
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = "Detalle de campaña",
                subtitle = "Revisa el estado y acciones disponibles",
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
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
                    Text(state.errorMessage, color = colors.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetryClick, shape = RoundedCornerShape(16.dp)) {
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
                    canGenerateAi = state.campaign.estado == "activa",
                    colors = colors,
                    onEditClick = { onEditClick(state.campaign.idCampana) },
                    onResourcesClick = { onResourcesClick(state.campaign.idCampana) },
                    onGenerateAiClick = {
                        onGenerateAiClick(state.campaign.idCampana, state.campaign.estado == "activa")
                    },
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
            title = { Text("Finalizar campana") },
            text = { Text("Confirma que deseas marcar esta campana como finalizada.") },
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
            title = { Text("Eliminar campana") },
            text = { Text("Eliminar esta campana? Esta accion puede afectar sus recursos relacionados.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteConfirm()
                    },
                    enabled = !state.isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)
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
    canGenerateAi: Boolean,
    colors: CampaignDetailPalette,
    onEditClick: () -> Unit,
    onResourcesClick: () -> Unit,
    onGenerateAiClick: () -> Unit,
    onChangeStatusClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CampaignHeroCard(campaign = campaign, colors = colors)
        errorMessage?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.errorSoft,
                border = BorderStroke(1.dp, colors.error.copy(alpha = 0.25f))
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(14.dp),
                    color = colors.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        CampaignInfoCard(campaign = campaign, colors = colors)
        Button(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            EditGlyph(Color.White)
            Text(
                text = "Editar campana",
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
        ActionCard(
            title = "Ver recursos",
            description = "Administra imagenes, ideas y videos generados.",
            colors = colors,
            enabled = true,
            onClick = onResourcesClick,
            icon = { ResourcesGlyph(colors.sky) }
        )
        ActionCard(
            title = "Generar con IA",
            description = "Crea contenido a partir de esta campana.",
            colors = colors,
            enabled = canGenerateAi,
            onClick = onGenerateAiClick,
            icon = { SparkGlyph(colors.primary) }
        )
        if (!canGenerateAi) {
            Text(
                text = "La generacion IA requiere una campana activa.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
        OutlinedButton(
            onClick = onChangeStatusClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isChangingStatus,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colors.border)
        ) {
            Text(if (isChangingStatus) "Actualizando..." else "Cambiar estado")
        }
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDeleting,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
            border = BorderStroke(1.dp, colors.error.copy(alpha = 0.35f))
        ) {
            Text(if (isDeleting) "Eliminando..." else "Eliminar campana")
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CampaignHeroCard(
    campaign: CampaignResponse,
    colors: CampaignDetailPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.soft,
        border = BorderStroke(1.dp, colors.sky.copy(alpha = 0.35f)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
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
                Text(
                    text = campaign.nombre,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                CampaignStatusChip(status = campaign.estado)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Objetivo",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary
                )
                Text(
                    text = campaign.objetivo?.takeIf { it.isNotBlank() } ?: "Sin objetivo",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun CampaignInfoCard(
    campaign: CampaignResponse,
    colors: CampaignDetailPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(
                label = "Descripcion",
                value = campaign.descripcion?.takeIf { it.isNotBlank() } ?: "Sin descripcion",
                colors = colors,
                icon = { TextGlyph(colors.sky) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            InfoRow("Presupuesto", campaign.presupuesto.formatBudget(), colors) { MoneyGlyph(colors.primary) }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            InfoRow("Fecha de inicio", campaign.fechaInicio.formatCampaignDateTime().cleanMidnight(), colors) { CalendarGlyph(colors.sky) }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            InfoRow("Fecha de fin", campaign.fechaFin.formatCampaignDateTime().cleanMidnight(), colors) { CalendarGlyph(colors.sky) }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            InfoRow("Fecha de creacion", campaign.fechaCreacion.formatCampaignDateTime().cleanMidnight(), colors) { CalendarGlyph(colors.textSecondary) }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            InfoRow("Ultima actualizacion", campaign.fechaActualizacion.formatCampaignDateTime().cleanMidnight(), colors) { CalendarGlyph(colors.textSecondary) }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    colors: CampaignDetailPalette,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(12.dp),
            color = colors.soft
        ) {
            Box(contentAlignment = Alignment.Center) { icon() }
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    colors: CampaignDetailPalette,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val alpha = if (enabled) 1f else 0.55f
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, if (enabled) colors.border else colors.border.copy(alpha = 0.6f)),
        shadowElevation = if (enabled) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = colors.soft.copy(alpha = alpha)
            ) {
                Box(contentAlignment = Alignment.Center) { icon() }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.Bold, color = colors.textPrimary.copy(alpha = alpha))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary.copy(alpha = alpha))
            }
            ArrowGlyph(colors.sky.copy(alpha = alpha))
        }
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
                        enabled = !isChangingStatus,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(status.label)
                    }
                }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun String.cleanMidnight(): String = removeSuffix(" 00:00")

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
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(color, Offset(3.dp.toPx(), 5.dp.toPx()), Size(18.dp.toPx(), 14.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()), style = stroke)
        drawLine(color, Offset(7.dp.toPx(), 10.dp.toPx()), Offset(17.dp.toPx(), 10.dp.toPx()), 1.7.dp.toPx())
        drawLine(color, Offset(7.dp.toPx(), 14.dp.toPx()), Offset(14.dp.toPx(), 14.dp.toPx()), 1.7.dp.toPx())
    }
}

@Composable
private fun EditGlyph(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(5.dp.toPx(), 13.dp.toPx()), Offset(13.dp.toPx(), 5.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(12.dp.toPx(), 4.dp.toPx()), Offset(15.dp.toPx(), 7.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(4.dp.toPx(), 14.dp.toPx()), Offset(4.dp.toPx(), 16.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(4.dp.toPx(), 16.dp.toPx()), Offset(6.dp.toPx(), 16.dp.toPx()), 2.dp.toPx())
    }
}

@Composable
private fun ResourcesGlyph(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(color, Offset(3.dp.toPx(), 6.dp.toPx()), Size(16.dp.toPx(), 12.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()), style = stroke)
        drawCircle(color.copy(alpha = 0.22f), 3.dp.toPx(), Offset(8.dp.toPx(), 10.dp.toPx()), style = stroke)
        drawLine(color, Offset(7.dp.toPx(), 16.dp.toPx()), Offset(12.dp.toPx(), 12.dp.toPx()), 1.6.dp.toPx())
        drawLine(color, Offset(12.dp.toPx(), 12.dp.toPx()), Offset(17.dp.toPx(), 16.dp.toPx()), 1.6.dp.toPx())
    }
}

@Composable
private fun SparkGlyph(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        drawLine(color, Offset(11.dp.toPx(), 3.dp.toPx()), Offset(11.dp.toPx(), 8.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(11.dp.toPx(), 14.dp.toPx()), Offset(11.dp.toPx(), 19.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(3.dp.toPx(), 11.dp.toPx()), Offset(8.dp.toPx(), 11.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(14.dp.toPx(), 11.dp.toPx()), Offset(19.dp.toPx(), 11.dp.toPx()), 2.dp.toPx())
    }
}

@Composable
private fun MoneyGlyph(color: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        drawCircle(color.copy(alpha = 0.18f), 8.dp.toPx(), Offset(10.dp.toPx(), 10.dp.toPx()), style = Stroke(1.8.dp.toPx()))
        drawLine(color, Offset(10.dp.toPx(), 5.dp.toPx()), Offset(10.dp.toPx(), 15.dp.toPx()), 1.8.dp.toPx())
        drawLine(color, Offset(7.dp.toPx(), 8.dp.toPx()), Offset(13.dp.toPx(), 8.dp.toPx()), 1.8.dp.toPx())
        drawLine(color, Offset(7.dp.toPx(), 12.dp.toPx()), Offset(13.dp.toPx(), 12.dp.toPx()), 1.8.dp.toPx())
    }
}

@Composable
private fun CalendarGlyph(color: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(color, Offset(3.dp.toPx(), 4.dp.toPx()), Size(14.dp.toPx(), 13.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = stroke)
        drawLine(color, Offset(3.dp.toPx(), 8.dp.toPx()), Offset(17.dp.toPx(), 8.dp.toPx()), 1.6.dp.toPx())
    }
}

@Composable
private fun TextGlyph(color: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        drawLine(color, Offset(4.dp.toPx(), 6.dp.toPx()), Offset(16.dp.toPx(), 6.dp.toPx()), 1.8.dp.toPx())
        drawLine(color, Offset(4.dp.toPx(), 10.dp.toPx()), Offset(16.dp.toPx(), 10.dp.toPx()), 1.8.dp.toPx())
        drawLine(color, Offset(4.dp.toPx(), 14.dp.toPx()), Offset(12.dp.toPx(), 14.dp.toPx()), 1.8.dp.toPx())
    }
}

@Composable
private fun ArrowGlyph(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        drawLine(color, Offset(4.dp.toPx(), 9.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(10.dp.toPx(), 5.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx())
        drawLine(color, Offset(10.dp.toPx(), 13.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx())
    }
}

@Immutable
private data class CampaignDetailPalette(
    val background: Color = Color(0xFFF8FAFC),
    val surface: Color = Color.White,
    val soft: Color = Color(0xFFEFF6FF),
    val textPrimary: Color = Color(0xFF0F172A),
    val textSecondary: Color = Color(0xFF64748B),
    val primary: Color = Color(0xFF3B82F6),
    val sky: Color = Color(0xFF0EA5E9),
    val border: Color = Color(0xFFDCE5F0),
    val error: Color = Color(0xFFB42318),
    val errorSoft: Color = Color(0xFFFFF1F2)
)

private val campaignDetailPalette = CampaignDetailPalette()
