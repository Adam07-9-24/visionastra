package com.tecsup.visionastra.mobile.ui.campaigns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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
    var datePickerTarget by remember { mutableStateOf<DatePickerTarget?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditMode) "Editar campaña" else "Nueva campaña")
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Atrás")
                    }
                }
            )
        }
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

            state.loadErrorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.loadErrorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                CampaignFormContent(
                    state = state,
                    onNombreChange = onNombreChange,
                    onObjetivoChange = onObjetivoChange,
                    onDescripcionChange = onDescripcionChange,
                    onPresupuestoChange = onPresupuestoChange,
                    onFechaInicioClick = { datePickerTarget = DatePickerTarget.Start },
                    onFechaFinClick = { datePickerTarget = DatePickerTarget.End },
                    onClearFechaInicio = { onFechaInicioSelected(null) },
                    onClearFechaFin = { onFechaFinSelected(null) },
                    onSaveClick = onSaveClick,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                )
            }
        }
    }

    datePickerTarget?.let { target ->
        CampaignDatePickerDialog(
            initialDate = when (target) {
                DatePickerTarget.Start -> state.fechaInicio
                DatePickerTarget.End -> state.fechaFin
            },
            onDismiss = { datePickerTarget = null },
            onDateSelected = { value ->
                when (target) {
                    DatePickerTarget.Start -> onFechaInicioSelected(value)
                    DatePickerTarget.End -> onFechaFinSelected(value)
                }
                datePickerTarget = null
            }
        )
    }
}

@Composable
private fun CampaignFormContent(
    state: CampaignFormUiState,
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
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        OutlinedTextField(
            value = state.nombre,
            onValueChange = onNombreChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre") },
            enabled = !state.isSaving,
            singleLine = true,
            isError = state.nombreError != null,
            supportingText = state.nombreError?.let { { Text(it) } }
        )
        OutlinedTextField(
            value = state.objetivo,
            onValueChange = onObjetivoChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Objetivo") },
            enabled = !state.isSaving,
            singleLine = false,
            maxLines = 3
        )
        OutlinedTextField(
            value = state.descripcion,
            onValueChange = onDescripcionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción") },
            enabled = !state.isSaving,
            minLines = 3,
            maxLines = 6
        )
        OutlinedTextField(
            value = state.presupuesto,
            onValueChange = onPresupuestoChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Presupuesto") },
            enabled = !state.isSaving,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = state.presupuestoError != null,
            supportingText = state.presupuestoError?.let { { Text(it) } }
        )
        DateField(
            label = "Fecha de inicio",
            value = state.fechaInicio.formatCampaignDate(),
            enabled = !state.isSaving,
            onClick = onFechaInicioClick,
            onClear = onClearFechaInicio
        )
        DateField(
            label = "Fecha de fin",
            value = state.fechaFin.formatCampaignDate(),
            enabled = !state.isSaving,
            onClick = onFechaFinClick,
            onClear = onClearFechaFin
        )
        state.fechaError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (state.isEditMode) "Guardar cambios" else "Crear campaña")
            }
        }
    }
}

@Composable
private fun DateField(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Text(value)
        }
        TextButton(
            onClick = onClear,
            enabled = enabled
        ) {
            Text("Quitar fecha")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampaignDatePickerDialog(
    initialDate: String?,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = backendDateTimeToEpochMillis(initialDate)
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                    if (selectedDate != null) {
                        onDateSelected(epochMillisToBackendDateTime(selectedDate))
                    }
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private enum class DatePickerTarget {
    Start,
    End
}
