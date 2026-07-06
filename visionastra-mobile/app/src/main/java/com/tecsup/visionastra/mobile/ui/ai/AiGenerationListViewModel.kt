package com.tecsup.visionastra.mobile.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.visionastra.mobile.data.repository.AiGenerationRepository
import com.tecsup.visionastra.mobile.data.repository.AiGenerationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AiGenerationListViewModel @Inject constructor(
    private val repository: AiGenerationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiGenerationListUiState(isLoading = true))
    val uiState: StateFlow<AiGenerationListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.listGenerations()) {
                is AiGenerationResult.Success -> _uiState.update {
                    it.copy(generations = result.data, isLoading = false)
                }
                is AiGenerationResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
