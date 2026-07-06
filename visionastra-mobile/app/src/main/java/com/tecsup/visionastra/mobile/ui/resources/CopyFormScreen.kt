package com.tecsup.visionastra.mobile.ui.resources

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyFormScreen(
    state: CopyFormUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val colors = copyPalette
    Scaffold(
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = "Crear idea",
                subtitle = "Describe el concepto para la IA",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Agrega una idea, mensaje o descripcion que ayude a VisionAstra a comprender el contenido que deseas crear.", style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
            state.errorMessage?.let { ErrorBanner(it, colors) }
            OutlinedTextField(value = state.title, onValueChange = onTitleChange, modifier = Modifier.fillMaxWidth(), label = { Text("Titulo del recurso") }, enabled = !state.isSaving, singleLine = true, shape = RoundedCornerShape(18.dp))
            OutlinedTextField(
                value = state.content,
                onValueChange = onContentChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contenido de la idea") },
                placeholder = { Text("Escribe aqui el mensaje, concepto, descripcion o frase que deseas utilizar...") },
                enabled = !state.isSaving,
                minLines = 8,
                shape = RoundedCornerShape(18.dp)
            )
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = colors.soft, border = BorderStroke(1.dp, colors.border)) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoGlyph(colors.primary)
                    Text("Este texto podra utilizarse como referencia durante la Generacion IA.", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(onClick = onSaveClick, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !state.isSaving, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("Guardar idea")
            }
        }
    }
}

@Composable private fun ErrorBanner(message: String, colors: CopyPalette) { Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.errorSoft, border = BorderStroke(1.dp, colors.error.copy(alpha = 0.25f))) { Text(message, modifier = Modifier.padding(14.dp), color = colors.error) } }
@Composable private fun BackGlyph(color: Color) { Canvas(Modifier.size(24.dp)) { drawLine(color, Offset(19.dp.toPx(), 12.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 7.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 17.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()) } }
@Composable private fun InfoGlyph(color: Color) { Canvas(Modifier.size(22.dp)) { drawCircle(color.copy(alpha = 0.16f), 9.dp.toPx(), Offset(11.dp.toPx(), 11.dp.toPx())); drawCircle(color, 1.3.dp.toPx(), Offset(11.dp.toPx(), 7.dp.toPx())); drawLine(color, Offset(11.dp.toPx(), 10.dp.toPx()), Offset(11.dp.toPx(), 15.dp.toPx()), 1.8.dp.toPx()) } }
private data class CopyPalette(val background: Color = Color(0xFFF8FAFC), val soft: Color = Color(0xFFEFF6FF), val textPrimary: Color = Color(0xFF0F172A), val textSecondary: Color = Color(0xFF64748B), val primary: Color = Color(0xFF3B82F6), val border: Color = Color(0xFFDCE5F0), val error: Color = Color(0xFFB42318), val errorSoft: Color = Color(0xFFFFF1F2))
private val copyPalette = CopyPalette()
