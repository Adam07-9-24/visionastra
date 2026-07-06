package com.tecsup.visionastra.mobile.ui.resources

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.tecsup.visionastra.mobile.core.util.formatFileSize
import com.tecsup.visionastra.mobile.ui.components.VisionAstraTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageUploadScreen(
    state: ImageUploadUiState,
    onImageSelected: (android.net.Uri?) -> Unit,
    onTitleChange: (String) -> Unit,
    onUploadClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = uploadPalette
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { onImageSelected(it) }

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            VisionAstraTopAppBar(
                title = "Subir imagen",
                subtitle = "Agrega material visual a tu campaña",
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
            Text("Sube una imagen para orientar a la IA y enriquecer tu campana.", style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
            state.errorMessage?.let { ErrorBanner(it, colors) }
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border), shadowElevation = 2.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (state.selectedImage == null) {
                        ImageGlyph(colors.primary)
                        Text("Seleccionar imagen", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                        Text("JPG, PNG o WEBP · maximo 10 MB", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        AsyncImage(model = state.selectedImage.uri, contentDescription = "Vista previa", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f))
                        Text(state.selectedImage.displayName, color = colors.textPrimary)
                        Text("${state.selectedImage.sizeBytes.formatFileSize()} · ${state.selectedImage.mimeType}", color = colors.textSecondary)
                    }
                    OutlinedButton(onClick = { launcher.launch("image/*") }, enabled = !state.isUploading, shape = RoundedCornerShape(16.dp)) {
                        Text(if (state.selectedImage == null) "Seleccionar imagen" else "Cambiar imagen")
                    }
                }
            }
            OutlinedTextField(value = state.title, onValueChange = onTitleChange, modifier = Modifier.fillMaxWidth(), label = { Text("Titulo del recurso") }, supportingText = { Text("Opcional. Usa un nombre claro para identificar la imagen.") }, enabled = !state.isUploading, singleLine = true, shape = RoundedCornerShape(18.dp))
            Button(onClick = onUploadClick, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !state.isUploading, shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
                if (state.isUploading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("Subir imagen")
            }
        }
    }
}

@Composable private fun ErrorBanner(message: String, colors: UploadPalette) { Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.errorSoft, border = BorderStroke(1.dp, colors.error.copy(alpha = 0.25f))) { Text(message, modifier = Modifier.padding(14.dp), color = colors.error) } }
@Composable private fun BackGlyph(color: Color) { Canvas(Modifier.size(24.dp)) { drawLine(color, Offset(19.dp.toPx(), 12.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 7.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()); drawLine(color, Offset(11.dp.toPx(), 17.dp.toPx()), Offset(6.dp.toPx(), 12.dp.toPx()), 2.2.dp.toPx()) } }
@Composable private fun ImageGlyph(color: Color) { Canvas(Modifier.size(42.dp)) { val s = Stroke(2.dp.toPx(), cap = StrokeCap.Round); drawRoundRect(color, Offset(5.dp.toPx(), 8.dp.toPx()), Size(32.dp.toPx(), 24.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(7.dp.toPx()), style = s); drawCircle(color.copy(alpha = 0.22f), 3.dp.toPx(), Offset(14.dp.toPx(), 15.dp.toPx()), style = s); drawLine(color, Offset(10.dp.toPx(), 29.dp.toPx()), Offset(20.dp.toPx(), 21.dp.toPx()), 2.dp.toPx()); drawLine(color, Offset(20.dp.toPx(), 21.dp.toPx()), Offset(33.dp.toPx(), 30.dp.toPx()), 2.dp.toPx()) } }
private data class UploadPalette(val background: Color = Color(0xFFF8FAFC), val surface: Color = Color.White, val textPrimary: Color = Color(0xFF0F172A), val textSecondary: Color = Color(0xFF64748B), val primary: Color = Color(0xFF3B82F6), val border: Color = Color(0xFFDCE5F0), val error: Color = Color(0xFFB42318), val errorSoft: Color = Color(0xFFFFF1F2))
private val uploadPalette = UploadPalette()
