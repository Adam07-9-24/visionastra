package com.tecsup.visionastra.mobile.ui.ai

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.visionastra.mobile.data.repository.AiGenerationRepository
import com.tecsup.visionastra.mobile.data.repository.AiGenerationResult
import com.tecsup.visionastra.mobile.data.repository.CampaignRepository
import com.tecsup.visionastra.mobile.data.repository.CampaignResult
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
class AiGenerationFormViewModel @Inject constructor(
    private val aiRepository: AiGenerationRepository,
    private val campaignRepository: CampaignRepository,
    private val resourceRepository: ResourceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val initialCampaignId: Int? = savedStateHandle["idCampana"]
    private val _uiState = MutableStateFlow(
        AiGenerationFormUiState(selectedCampaignId = initialCampaignId, isLoading = true)
    )
    val uiState: StateFlow<AiGenerationFormUiState> = _uiState.asStateFlow()

    init {
        loadCampaigns()
    }

    fun onCampaignSelected(idCampaign: Int) {
        _uiState.update {
            it.copy(selectedCampaignId = idCampaign, selectedResourceIds = emptySet(), resources = emptyList())
        }
        loadResources(idCampaign)
    }

    fun onPromptChange(value: String) {
        _uiState.update { it.copy(prompt = value, errorMessage = null) }
    }

    fun toggleResource(idResource: Int) {
        _uiState.update { state ->
            val ids = state.selectedResourceIds
            state.copy(
                selectedResourceIds = if (ids.contains(idResource)) ids - idResource else ids + idResource,
                errorMessage = null
            )
        }
    }

    fun createGeneration() {
        val state = _uiState.value
        when {
            state.selectedCampaignId == null -> {
                _uiState.update { it.copy(errorMessage = "Selecciona una campaña activa.") }
                return
            }
            state.prompt.trim().length < 10 -> {
                _uiState.update { it.copy(errorMessage = "El prompt debe tener al menos 10 caracteres.") }
                return
            }
            state.selectedResourceIds.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "Selecciona al menos un recurso.") }
                return
            }
            state.selectedResourceIds.none { selectedId ->
                state.inputResources.any { it.idRecurso == selectedId && it.tipo == "copy" }
            } -> {
                _uiState.update { it.copy(errorMessage = "Selecciona al menos un recurso copy.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }
            when (
                val result = aiRepository.createGeneration(
                    idCampaign = checkNotNull(state.selectedCampaignId),
                    prompt = state.prompt,
                    resourceIds = state.selectedResourceIds.toList()
                )
            ) {
                is AiGenerationResult.Success -> _uiState.update {
                    it.copy(isCreating = false, createdGenerationId = result.data.idGeneracion)
                }
                is AiGenerationResult.Error -> _uiState.update {
                    it.copy(isCreating = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun loadCampaigns() {
        viewModelScope.launch {
            when (val result = campaignRepository.listCampaigns("activa")) {
                is CampaignResult.Success -> {
                    val selected = initialCampaignId ?: result.data.firstOrNull()?.idCampana
                    _uiState.update {
                        it.copy(campaigns = result.data, selectedCampaignId = selected, isLoading = false)
                    }
                    if (selected != null) loadResources(selected)
                }
                is CampaignResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun loadResources(idCampaign: Int) {
        viewModelScope.launch {
            when (val result = resourceRepository.listByCampaign(idCampaign)) {
                is ResourceResult.Success -> _uiState.update {
                    it.copy(resources = result.data, selectedResourceIds = emptySet())
                }
                is ResourceResult.Error -> _uiState.update {
                    it.copy(errorMessage = result.message)
                }
            }
        }
    }
}
