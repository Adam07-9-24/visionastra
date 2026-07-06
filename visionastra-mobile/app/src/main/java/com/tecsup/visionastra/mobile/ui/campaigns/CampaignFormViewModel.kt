package com.tecsup.visionastra.mobile.ui.campaigns

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.visionastra.mobile.data.remote.dto.CampaignRequest
import com.tecsup.visionastra.mobile.data.remote.dto.CampaignResponse
import com.tecsup.visionastra.mobile.data.repository.CampaignRepository
import com.tecsup.visionastra.mobile.data.repository.CampaignResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CampaignFormViewModel @Inject constructor(
    private val campaignRepository: CampaignRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val campaignId: Int? = savedStateHandle["idCampana"]
    private val _uiState = MutableStateFlow(
        CampaignFormUiState(
            isEditMode = campaignId != null,
            isLoading = campaignId != null
        )
    )
    val uiState: StateFlow<CampaignFormUiState> = _uiState.asStateFlow()

    init {
        if (campaignId != null) {
            loadCampaign(campaignId)
        }
    }

    fun onNombreChange(value: String) {
        _uiState.update { it.copy(nombre = value, nombreError = null) }
    }

    fun onObjetivoChange(value: String) {
        _uiState.update { it.copy(objetivo = value) }
    }

    fun onDescripcionChange(value: String) {
        _uiState.update { it.copy(descripcion = value) }
    }

    fun onPresupuestoChange(value: String) {
        _uiState.update { it.copy(presupuesto = value, presupuestoError = null) }
    }

    fun onFechaInicioSelected(value: String?) {
        _uiState.update { it.copy(fechaInicio = value, fechaError = null) }
    }

    fun onFechaFinSelected(value: String?) {
        _uiState.update { it.copy(fechaFin = value, fechaError = null) }
    }

    fun save() {
        val request = buildRequestOrUpdateErrors() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val id = campaignId
            val result = if (id == null) {
                campaignRepository.createCampaign(request)
            } else {
                campaignRepository.updateCampaign(id, request)
            }

            when (result) {
                is CampaignResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            savedCampaignId = result.data.idCampana
                        )
                    }
                }

                is CampaignResult.Error -> {
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    private fun loadCampaign(id: Int) {
        viewModelScope.launch {
            when (val result = campaignRepository.getCampaign(id)) {
                is CampaignResult.Success -> {
                    _uiState.value = result.data.toFormState()
                }

                is CampaignResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, loadErrorMessage = result.message)
                    }
                }
            }
        }
    }

    private fun buildRequestOrUpdateErrors(): CampaignRequest? {
        val current = _uiState.value
        val nombreError = if (current.nombre.isBlank()) {
            "El nombre es obligatorio."
        } else {
            null
        }
        val presupuestoValue = current.presupuesto.trim().takeIf { it.isNotEmpty() }
        val presupuesto = presupuestoValue?.toBigDecimalOrNull()
        val presupuestoError = when {
            presupuestoValue == null -> null
            presupuesto == null -> "Ingresa un presupuesto valido."
            presupuesto < BigDecimal.ZERO -> "El presupuesto no puede ser negativo."
            else -> null
        }
        val fechaError = when {
            current.fechaInicio != null &&
                current.fechaFin != null &&
                current.fechaFin < current.fechaInicio -> {
                "La fecha de fin no puede ser anterior a la fecha de inicio."
            }

            else -> null
        }

        if (nombreError != null || presupuestoError != null || fechaError != null) {
            _uiState.update {
                it.copy(
                    nombreError = nombreError,
                    presupuestoError = presupuestoError,
                    fechaError = fechaError
                )
            }
            return null
        }

        return CampaignRequest(
            nombre = current.nombre.trim(),
            objetivo = current.objetivo.trim().ifBlank { null },
            descripcion = current.descripcion.trim().ifBlank { null },
            presupuesto = presupuesto,
            estado = current.estado,
            fechaInicio = current.fechaInicio,
            fechaFin = current.fechaFin
        )
    }

    private fun CampaignResponse.toFormState(): CampaignFormUiState =
        CampaignFormUiState(
            isEditMode = true,
            nombre = nombre,
            objetivo = objetivo.orEmpty(),
            descripcion = descripcion.orEmpty(),
            presupuesto = presupuesto?.stripTrailingZeros()?.toPlainString().orEmpty(),
            estado = estado,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )
}
