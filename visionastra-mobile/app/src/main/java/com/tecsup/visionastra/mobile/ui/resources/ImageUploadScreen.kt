package com.tecsup.visionastra.mobile.ui.resources

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.tecsup.visionastra.mobile.core.util.formatFileSize

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
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        onImageSelected(it)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Subir imagen") },
                navigationIcon = { TextButton(onClick = onBackClick) { Text("Atrás") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isUploading
            ) {
                Text("Seleccionar imagen")
            }
            state.selectedImage?.let { image ->
                AsyncImage(
                    model = image.uri,
                    contentDescription = "Vista previa",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                Text(image.displayName)
                Text(image.sizeBytes.formatFileSize())
            }
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título") },
                enabled = !state.isUploading,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onUploadClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isUploading
            ) {
                if (state.isUploading) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Subir imagen")
            }
        }
    }
}
