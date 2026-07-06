package com.tecsup.visionastra.mobile.ui.ai

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.visionastra.mobile.BuildConfig
import com.tecsup.visionastra.mobile.data.remote.dto.AiGenerationResponse
import com.tecsup.visionastra.mobile.data.repository.AiGenerationRepository
import com.tecsup.visionastra.mobile.data.repository.AiGenerationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class AiGenerationDetailViewModel @Inject constructor(
    private val repository: AiGenerationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val idGeneration: Int = checkNotNull(savedStateHandle["idGeneracion"])
    private val _uiState = MutableStateFlow(AiGenerationDetailUiState(isLoading = true))
    val uiState: StateFlow<AiGenerationDetailUiState> = _uiState.asStateFlow()
    private var pollingJob: Job? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getGeneration(idGeneration)) {
                is AiGenerationResult.Success -> {
                    _uiState.update { it.copy(generation = result.data, isLoading = false) }
                }
                is AiGenerationResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun preparePrompt() {
        val generation = _uiState.value.generation ?: return
        if (_uiState.value.isPreparingPrompt || _uiState.value.hasPreparedPrompt) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPreparingPrompt = true, errorMessage = null) }
            logDebug("prepare-prompt solicitado")
            when (val result = repository.preparePrompt(generation.idGeneracion)) {
                is AiGenerationResult.Success -> {
                    logDebug("prepare-prompt completado")
                    _uiState.update {
                        it.copy(generation = result.data, isPreparingPrompt = false)
                    }
                }
                is AiGenerationResult.Error -> {
                    _uiState.update {
                        it.copy(isPreparingPrompt = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun showGenerateVideoConfirmation() {
        if (!_uiState.value.hasPreparedPrompt || _uiState.value.isGeneratingVideo) return
        _uiState.update { it.copy(showGenerateVideoConfirmation = true) }
    }

    fun dismissGenerateVideoConfirmation() {
        _uiState.update { it.copy(showGenerateVideoConfirmation = false) }
    }

    fun generateVideo() {
        val generation = _uiState.value.generation ?: return
        if (_uiState.value.isGeneratingVideo || !_uiState.value.hasPreparedPrompt) return
        if (generation.estado == "completado") return

        _uiState.update {
            it.copy(
                isVideoGenerationRequested = true,
                isGeneratingVideo = true,
                showGenerateVideoConfirmation = false,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            logDebug("generate-video solicitado por confirmacion del usuario")
            when (val result = repository.generateVideo(generation.idGeneracion)) {
                is AiGenerationResult.Success -> _uiState.update {
                    it.copy(generation = result.data, isGeneratingVideo = false)
                }
                is AiGenerationResult.Error -> _uiState.update {
                    it.copy(
                        isVideoGenerationRequested = false,
                        isGeneratingVideo = false,
                        errorMessage = result.message
                    )
                }
            }
            startPollingAfterVideoRequest()
        }
    }

    private fun startPollingAfterVideoRequest() {
        val generation = _uiState.value.generation ?: return
        if (!_uiState.value.isVideoGenerationRequested || generation.estado != "procesando") return
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5_000L)
                when (val result = repository.getGeneration(idGeneration)) {
                    is AiGenerationResult.Success -> {
                        _uiState.update { it.copy(generation = result.data) }
                        if (result.data.estado == "completado" || result.data.estado == "error") break
                    }
                    is AiGenerationResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    private companion object {
        const val TAG = "AiGenerationDetail"
    }
}
