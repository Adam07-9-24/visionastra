package com.tecsup.visionastra.mobile.ui.resources

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.visionastra.mobile.data.repository.ResourceRepository
import com.tecsup.visionastra.mobile.data.repository.ResourceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CopyFormUiState(
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class CopyFormViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val idCampaign: Int = checkNotNull(savedStateHandle["idCampana"])
    private val _uiState = MutableStateFlow(CopyFormUiState())
    val uiState: StateFlow<CopyFormUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, errorMessage = null) }
    }

    fun onContentChange(value: String) {
        _uiState.update { it.copy(content = value, errorMessage = null) }
    }

    fun save() {
        val state = _uiState.value
        when {
            state.title.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "El título es obligatorio.") }
                return
            }
            state.title.trim().length > 150 -> {
                _uiState.update { it.copy(errorMessage = "El título no puede superar los 150 caracteres.") }
                return
            }
            state.content.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "El contenido del copy es obligatorio.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (
                val result = resourceRepository.createCopy(
                    idCampaign = idCampaign,
                    title = state.title,
                    content = state.content
                )
            ) {
                is ResourceResult.Success -> _uiState.update {
                    it.copy(isSaving = false, saved = true)
                }
                is ResourceResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }
}
