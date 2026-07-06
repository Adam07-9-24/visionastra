package com.tecsup.visionastra.mobile.ui.resources

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.visionastra.mobile.data.remote.dto.ResourceResponse
import com.tecsup.visionastra.mobile.data.repository.ResourceRepository
import com.tecsup.visionastra.mobile.data.repository.ResourceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResourceDetailUiState(
    val resource: ResourceResponse? = null,
    val fileUrl: String? = null,
    val isLoading: Boolean = false,
    val isUpdatingTitle: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val deleted: Boolean = false
)

@HiltViewModel
class ResourceDetailViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val idResource: Int = checkNotNull(savedStateHandle["idRecurso"])
    private val _uiState = MutableStateFlow(ResourceDetailUiState(isLoading = true))
    val uiState: StateFlow<ResourceDetailUiState> = _uiState.asStateFlow()

    init {
        loadResource()
    }

    fun loadResource() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = resourceRepository.getResource(idResource)) {
                is ResourceResult.Success -> {
                    _uiState.update {
                        it.copy(
                            resource = result.data,
                            fileUrl = resourceRepository.fileUrl(result.data.idRecurso),
                            isLoading = false
                        )
                    }
                }
                is ResourceResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El título es obligatorio.") }
            return
        }
        if (title.length > 200) {
            _uiState.update { it.copy(errorMessage = "El título no puede superar los 200 caracteres.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingTitle = true, errorMessage = null) }
            when (val result = resourceRepository.updateTitle(idResource, title)) {
                is ResourceResult.Success -> {
                    _uiState.update {
                        it.copy(
                            resource = result.data,
                            isUpdatingTitle = false,
                            snackbarMessage = "Título actualizado"
                        )
                    }
                }
                is ResourceResult.Error -> {
                    _uiState.update {
                        it.copy(isUpdatingTitle = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun deleteResource() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            when (val result = resourceRepository.deleteResource(idResource)) {
                is ResourceResult.Success -> _uiState.update {
                    it.copy(isDeleting = false, deleted = true)
                }
                is ResourceResult.Error -> _uiState.update {
                    it.copy(isDeleting = false, errorMessage = result.message)
                }
            }
        }
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
