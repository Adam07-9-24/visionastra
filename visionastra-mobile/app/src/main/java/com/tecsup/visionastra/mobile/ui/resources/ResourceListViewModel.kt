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

@HiltViewModel
class ResourceListViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val idCampaign: Int = checkNotNull(savedStateHandle["idCampana"])
    private val _uiState = MutableStateFlow(ResourceListUiState(isLoading = true))
    val uiState: StateFlow<ResourceListUiState> = _uiState.asStateFlow()

    init {
        loadResources()
    }

    fun loadResources() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = resourceRepository.listByCampaign(idCampaign)) {
                is ResourceResult.Success -> {
                    _uiState.update {
                        it.copy(
                            resources = result.data,
                            campaignName = result.data.firstOrNull()?.nombreCampana,
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

    fun onTypeSelected(type: String?) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
