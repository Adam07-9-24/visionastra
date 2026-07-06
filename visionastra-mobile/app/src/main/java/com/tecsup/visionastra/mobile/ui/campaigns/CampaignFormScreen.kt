package com.tecsup.visionastra.mobile.ui.campaigns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignFormScreen(
    state: CampaignFormUiState,
    onNombreChange: (String) -> Unit,
    onObjetivoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onPresupuestoChange: (String) -> Unit,
    onFechaInicioSelected: (String?) -> Unit,
    onFechaFinSelected: (String?) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = formPalette
    var datePickerTarget by remember { mutableStateOf<DatePickerTarget?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = if (state.isEditMode) "Editar campaña" else "Nueva campaña",
                subtitle = "Configura los datos principales",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            state.loadErrorMessage != null -> Box(Modifier.fillMaxSize().padding(innerPadding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(state.loadErrorMessage, color = colors.error)
            }
            else -> CampaignFormContent(
                state = state,
                colors = colors,
                onNombreChange = onNombreChange,
                onObjetivoChange = onObjetivoChange,
                onDescripcionChange = onDescripcionChange,
                onPresupuestoChange = onPresupuestoChange,
                onFechaInicioClick = { datePickerTarget = DatePickerTarget.Start },
                onFechaFinClick = { datePickerTarget = DatePickerTarget.End },
                onClearFechaInicio = { onFechaInicioSelected(null) },
                onClearFechaFin = { onFechaFinSelected(null) },
                onSaveClick = onSaveClick,
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )
        }
    }

    datePickerTarget?.let { target ->
        CampaignDatePickerDialog(
            initialDate = if (target == DatePickerTarget.Start) state.fechaInicio else state.fechaFin,
            onDismiss = { datePickerTarget = null },
            onDateSelected = { value ->
                if (target == DatePickerTarget.Start) onFechaInicioSelected(value) else onFechaFinSelected(value)
                datePickerTarget = null
            }
        )
    }
}

@Composable
private fun CampaignFormContent(
    state: CampaignFormUiState,
    colors: CampaignFormPalette,
    onNombreChange: (String) -> Unit,
    onObjetivoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onPresupuestoChange: (String) -> Unit,
    onFechaInicioClick: () -> Unit,
    onFechaFinClick: () -> Unit,
    onClearFechaInicio: () -> Unit,
    onClearFechaFin: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FormIntroHeader(state.isEditMode, colors)
        state.errorMessage?.let { ErrorBox(it, colors) }
        FormSection(
            title = "Informacion general",
            description = "Agrega los datos principales que identifican la campana.",
            colors = colors,
            icon = { CampaignGlyph(colors.sky) }
        ) {
            FormTextField(value = state.nombre, onValueChange = onNombreChange, label = "Nombre de la campana", enabled = !state.isSaving, singleLine = true, isError = state.nombreError != null, supportingText = state.nombreError ?: "Usa un nombre claro para reconocerla facilmente.", colors = colors)
            FormTextField(value = state.objetivo, onValueChange = onObjetivoChange, label = "Objetivo principal", enabled = !state.isSaving, maxLines = 3, supportingText = "Resume que quieres lograr con esta campana.", colors = colors)
            FormTextField(value = state.descripcion, onValueChange = onDescripcionChange, label = "Descripcion de la campana", enabled = !state.isSaving, minLines = 3, maxLines = 6, supportingText = "Incluye contexto, publico o mensajes clave.", colors = colors)
        }
        FormSection(
            title = "Presupuesto estimado",
            description = "Registra el monto planificado para esta campana.",
            colors = colors,
            icon = { MoneyGlyph(colors.primary) }
        ) {
            OutlinedTextField(
                value = state.presupuesto,
                onValueChange = onPresupuestoChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Presupuesto") },
                prefix = { Text("S/") },
                leadingIcon = { MoneyGlyph(colors.textSecondary) },
                enabled = !state.isSaving,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = state.presupuestoError != null,
                supportingText = state.presupuestoError?.let { { Text(it) } },
                shape = RoundedCornerShape(18.dp),
                colors = formTextFieldColors(colors)
            )
        }
        FormSection(
            title = "Fechas",
            description = "Define el rango de trabajo de la campana.",
            colors = colors,
            icon = { CalendarGlyph(colors.sky) }
        ) {
            DateField("Fecha de inicio", state.fechaInicio, !state.isSaving, colors, onFechaInicioClick, onClearFechaInicio)
            DateField("Fecha de fin", state.fechaFin, !state.isSaving, colors, onFechaFinClick, onClearFechaFin)
            state.fechaError?.let { Text(it, color = colors.error, style = MaterialTheme.typography.bodyMedium) }
        }
        DraftInfo(colors)
        Button(onClick = onSaveClick, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !state.isSaving, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.sky)) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                if (state.isEditMode) SaveGlyph(Color.White) else ArrowForwardGlyph(Color.White)
                Text(
                    text = if (state.isEditMode) "Guardar cambios" else "Crear campana",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FormIntroHeader(isEditMode: Boolean, colors: CampaignFormPalette) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = colors.skySoft, border = BorderStroke(1.dp, colors.sky.copy(alpha = .26f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(Modifier.size(40.dp), shape = RoundedCornerShape(14.dp), color = colors.surface) {
                Box(contentAlignment = Alignment.Center) { CampaignGlyph(colors.sky) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(if (isEditMode) "Actualiza tu campana" else "Crea una nueva campana", color = colors.textPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(if (isEditMode) "Modifica la informacion sin perder los recursos y contenido relacionados." else "Define el objetivo, el presupuesto y las fechas para comenzar a organizar tu contenido.", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun FormSection(title: String, description: String, colors: CampaignFormPalette, icon: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border), shadowElevation = 3.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(Modifier.size(34.dp), shape = RoundedCornerShape(12.dp), color = colors.skySoft) {
                    Box(contentAlignment = Alignment.Center) { icon() }
                }
                Column {
                    Text(title, color = colors.textPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(description, color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            content()
        }
    }
}

@Composable
private fun FormTextField(value: String, onValueChange: (String) -> Unit, label: String, enabled: Boolean, colors: CampaignFormPalette, singleLine: Boolean = false, minLines: Int = 1, maxLines: Int = 1, isError: Boolean = false, supportingText: String? = null) {
    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, enabled = enabled, singleLine = singleLine, minLines = minLines, maxLines = maxLines, isError = isError, supportingText = supportingText?.let { { Text(it) } }, shape = RoundedCornerShape(18.dp), colors = formTextFieldColors(colors))
}

@Composable
private fun DateField(label: String, value: String?, enabled: Boolean, colors: CampaignFormPalette, onClick: () -> Unit, onClear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(64.dp), enabled = enabled, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, colors.border)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CalendarGlyph(colors.sky)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                    Text(if (value.isNullOrBlank()) "Seleccionar fecha" else value.formatCampaignDate(), style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                }
                ArrowForwardGlyph(colors.textSecondary)
            }
        }
        if (!value.isNullOrBlank()) {
            TextButton(onClick = onClear, enabled = enabled) { Text("Quitar fecha", color = colors.textSecondary) }
        }
    }
}

@Composable
private fun DraftInfo(colors: CampaignFormPalette) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = colors.soft, border = BorderStroke(1.dp, colors.sky.copy(alpha = .22f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoGlyph(colors.primary)
            Text("Las campanas nuevas se guardan inicialmente como borrador. Podras activarlas cuando tengan la informacion necesaria.", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }
}

@Composable
private fun formTextFieldColors(colors: CampaignFormPalette) = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.sky, focusedLabelColor = colors.sky, cursorColor = colors.sky, unfocusedBorderColor = colors.border, focusedTextColor = colors.textPrimary, unfocusedTextColor = colors.textPrimary)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampaignDatePickerDialog(initialDate: String?, onDismiss: () -> Unit, onDateSelected: (String) -> Unit) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = backendDateTimeToEpochMillis(initialDate))
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { datePickerState.selectedDateMillis?.let { onDateSelected(epochMillisToBackendDateTime(it)) } }) { Text("Aceptar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) { DatePicker(state = datePickerState) }
}

@Composable private fun ErrorBox(message: String, colors: CampaignFormPalette) { Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.errorSoft, border = BorderStroke(1.dp, colors.error.copy(alpha = .25f))) { Text(message, modifier = Modifier.padding(14.dp), color = colors.error) } }
@Composable private fun BackGlyph(color: Color) { Canvas(Modifier.size(24.dp)) { drawLine(color, Offset(19.dp.toPx(), 12.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 7.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 17.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()) } }
@Composable private fun CampaignGlyph(color: Color) { Canvas(Modifier.size(22.dp)) { drawRoundRect(color, Offset(3.dp.toPx(), 5.dp.toPx()), androidx.compose.ui.geometry.Size(16.dp.toPx(), 12.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(1.8.dp.toPx())); drawLine(color, Offset(7.dp.toPx(), 10.dp.toPx()), Offset(15.dp.toPx(), 10.dp.toPx()), 1.6.dp.toPx()); drawLine(color, Offset(7.dp.toPx(), 14.dp.toPx()), Offset(13.dp.toPx(), 14.dp.toPx()), 1.6.dp.toPx()) } }
@Composable private fun MoneyGlyph(color: Color) { Canvas(Modifier.size(20.dp)) { drawCircle(color.copy(alpha = .14f), 9.dp.toPx(), Offset(10.dp.toPx(), 10.dp.toPx())); drawLine(color, Offset(10.dp.toPx(), 5.dp.toPx()), Offset(10.dp.toPx(), 15.dp.toPx()), 1.8.dp.toPx()); drawLine(color, Offset(7.dp.toPx(), 7.dp.toPx()), Offset(12.dp.toPx(), 7.dp.toPx()), 1.6.dp.toPx()); drawLine(color, Offset(8.dp.toPx(), 13.dp.toPx()), Offset(13.dp.toPx(), 13.dp.toPx()), 1.6.dp.toPx()) } }
@Composable private fun CalendarGlyph(color: Color) { Canvas(Modifier.size(20.dp)) { drawRoundRect(color, Offset(3.dp.toPx(), 5.dp.toPx()), androidx.compose.ui.geometry.Size(14.dp.toPx(), 12.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(1.6.dp.toPx())); drawLine(color, Offset(6.dp.toPx(), 3.dp.toPx()), Offset(6.dp.toPx(), 7.dp.toPx()), 1.8.dp.toPx()); drawLine(color, Offset(14.dp.toPx(), 3.dp.toPx()), Offset(14.dp.toPx(), 7.dp.toPx()), 1.8.dp.toPx()); drawLine(color, Offset(4.dp.toPx(), 9.dp.toPx()), Offset(16.dp.toPx(), 9.dp.toPx()), 1.4.dp.toPx()) } }
@Composable private fun InfoGlyph(color: Color) { Canvas(Modifier.size(22.dp)) { drawCircle(color.copy(alpha = .16f), 9.dp.toPx(), Offset(11.dp.toPx(), 11.dp.toPx())); drawCircle(color, 1.3.dp.toPx(), Offset(11.dp.toPx(), 7.dp.toPx())); drawLine(color, Offset(11.dp.toPx(), 10.dp.toPx()), Offset(11.dp.toPx(), 15.dp.toPx()), 1.8.dp.toPx()) } }
@Composable private fun ArrowForwardGlyph(color: Color) { Canvas(Modifier.size(18.dp)) { drawLine(color, Offset(4.dp.toPx(), 9.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(10.dp.toPx(), 5.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(10.dp.toPx(), 13.dp.toPx()), Offset(14.dp.toPx(), 9.dp.toPx()), 2.dp.toPx()) } }
@Composable private fun SaveGlyph(color: Color) { Canvas(Modifier.size(18.dp)) { drawRoundRect(color, Offset(3.dp.toPx(), 3.dp.toPx()), androidx.compose.ui.geometry.Size(12.dp.toPx(), 12.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(1.8.dp.toPx())); drawLine(color, Offset(6.dp.toPx(), 4.dp.toPx()), Offset(12.dp.toPx(), 4.dp.toPx()), 1.6.dp.toPx()); drawLine(color, Offset(6.dp.toPx(), 13.dp.toPx()), Offset(12.dp.toPx(), 13.dp.toPx()), 1.6.dp.toPx()) } }
private enum class DatePickerTarget { Start, End }
private data class CampaignFormPalette(val background: Color = Color(0xFFF8FAFC), val surface: Color = Color.White, val soft: Color = Color(0xFFEFF6FF), val skySoft: Color = Color(0xFFE0F2FE), val textPrimary: Color = Color(0xFF0F172A), val textSecondary: Color = Color(0xFF64748B), val primary: Color = Color(0xFF2563EB), val sky: Color = Color(0xFF0EA5E9), val border: Color = Color(0xFFD7E3F0), val error: Color = Color(0xFFB42318), val errorSoft: Color = Color(0xFFFFF1F2))
private val formPalette = CampaignFormPalette()
