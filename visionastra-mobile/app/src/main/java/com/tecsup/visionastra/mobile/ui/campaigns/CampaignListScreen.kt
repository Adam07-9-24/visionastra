package com.tecsup.visionastra.mobile.ui.campaigns

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.ui.campaigns.components.CampaignCard
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBarVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignListScreen(
    state: CampaignListUiState,
    onStatusSelected: (String?) -> Unit,
    onRetryClick: () -> Unit,
    onCreateClick: () -> Unit,
    onCampaignClick: (Int) -> Unit,
    onSnackbarShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = campaignListPalette
    val snackbarHostState = remember { SnackbarHostState() }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onSnackbarShown()
        }
    }

    Scaffold(
        modifier = modifier.background(colors.background),
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = "Campañas",
                subtitle = "Organiza y administra tu contenido",
                onBackClick = { backDispatcher?.onBackPressed() },
                variant = VisionAstraTopAppBarVariant.Module
            )
        },
        floatingActionButton = {
            if (state.campaigns.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onCreateClick,
                    containerColor = colors.sky,
                    contentColor = Color.White,
                    icon = { AddGlyph(Color.White) },
                    text = { Text("Nueva campana", fontWeight = FontWeight.SemiBold) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(campaignStatusFilters, key = { it.label }) { filter ->
                    val selected = state.selectedStatus == filter.value
                    FilterChip(
                        selected = selected,
                        onClick = { onStatusSelected(filter.value) },
                        label = {
                            Text(
                                text = filter.label,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
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
                            selectedBorderColor = colors.sky
                        )
                    )
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                state.errorMessage != null -> {
                    CampaignMessageState(
                        title = "No se pudieron cargar las campanas",
                        message = state.errorMessage,
                        actionText = "Reintentar",
                        onActionClick = onRetryClick,
                        colors = colors,
                        isEmpty = false
                    )
                }

                state.campaigns.isEmpty() -> {
                    CampaignMessageState(
                        title = "Todavia no tienes campanas",
                        message = "Crea tu primera campana para organizar tus ideas, recursos y contenido generado con IA.",
                        actionText = "Crear primera campana",
                        onActionClick = onCreateClick,
                        colors = colors,
                        isEmpty = true
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.campaigns, key = { it.idCampana }) { campaign ->
                            CampaignCard(
                                campaign = campaign,
                                onClick = { onCampaignClick(campaign.idCampana) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignMessageState(
    title: String,
    message: String,
    actionText: String,
    onActionClick: () -> Unit,
    colors: CampaignListPalette,
    isEmpty: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(if (isEmpty) 24.dp else 20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, if (isEmpty) colors.sky.copy(alpha = 0.42f) else colors.error.copy(alpha = 0.28f)),
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
                        if (isEmpty) CampaignEmptyGlyph(colors.sky) else ErrorGlyph(colors.error)
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
                    modifier = Modifier.padding(top = 8.dp, bottom = 22.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isEmpty) colors.sky else colors.primary)
                ) {
                    if (isEmpty) AddGlyph(Color.White)
                    Text(
                        text = actionText,
                        modifier = Modifier.padding(start = if (isEmpty) 8.dp else 0.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CampaignEmptyGlyph(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(
            color = color,
            topLeft = Offset(4.dp.toPx(), 6.dp.toPx()),
            size = Size(20.dp.toPx(), 16.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()),
            style = stroke
        )
        drawLine(color, Offset(8.dp.toPx(), 11.dp.toPx()), Offset(20.dp.toPx(), 11.dp.toPx()), 1.8.dp.toPx())
        drawLine(color, Offset(8.dp.toPx(), 16.dp.toPx()), Offset(16.dp.toPx(), 16.dp.toPx()), 1.8.dp.toPx())
    }
}

@Composable
private fun BackGlyph(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
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
        val path = Path().apply {
            moveTo(size.width / 2f, 4.dp.toPx())
            lineTo(size.width - 4.dp.toPx(), size.height - 5.dp.toPx())
            lineTo(4.dp.toPx(), size.height - 5.dp.toPx())
            close()
        }
        drawPath(path, color, style = stroke)
        drawLine(color, Offset(size.width / 2f, 10.dp.toPx()), Offset(size.width / 2f, 17.dp.toPx()), 2.dp.toPx())
        drawCircle(color, 1.2.dp.toPx(), Offset(size.width / 2f, 21.dp.toPx()))
    }
}

@Immutable
private data class CampaignListPalette(
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

private val campaignListPalette = CampaignListPalette()
