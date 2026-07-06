package com.tecsup.visionastra.mobile.ui.resources

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.visionastra.mobile.core.util.UriFileInfo
import com.tecsup.visionastra.mobile.core.util.validateAndroidImage
import com.tecsup.visionastra.mobile.data.repository.ResourceRepository
import com.tecsup.visionastra.mobile.data.repository.ResourceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImageUploadUiState(
    val selectedImage: UriFileInfo? = null,
    val title: String = "",
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
    val uploaded: Boolean = false
)

@HiltViewModel
class ImageUploadViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val idCampaign: Int = checkNotNull(savedStateHandle["idCampana"])
    private val _uiState = MutableStateFlow(ImageUploadUiState())
    val uiState: StateFlow<ImageUploadUiState> = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri?) {
        if (uri == null) return
        val info = resourceRepository.readImageInfo(uri)
        val error = info.validateAndroidImage()
        _uiState.update {
            it.copy(selectedImage = if (error == null) info else null, errorMessage = error)
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, errorMessage = null) }
    }

    fun upload() {
        val image = _uiState.value.selectedImage
        if (image == null) {
            _uiState.update { it.copy(errorMessage = "Selecciona una imagen.") }
            return
        }
        val title = _uiState.value.title.trim()
        if (title.length > 150) {
            _uiState.update { it.copy(errorMessage = "El título no puede superar los 150 caracteres.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            when (val result = resourceRepository.uploadImage(idCampaign, title, image.uri)) {
                is ResourceResult.Success -> _uiState.update {
                    it.copy(isUploading = false, uploaded = true)
                }
                is ResourceResult.Error -> _uiState.update {
                    it.copy(isUploading = false, errorMessage = result.message)
                }
            }
        }
    }
}
