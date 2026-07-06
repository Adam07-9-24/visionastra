package com.tecsup.visionastra.mobile.ui.campaigns

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.visionastra.mobile.data.repository.CampaignRepository
import com.tecsup.visionastra.mobile.data.repository.CampaignResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CampaignDetailViewModel @Inject constructor(
    private val campaignRepository: CampaignRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val campaignId: Int = checkNotNull(savedStateHandle["idCampana"])
    private val _uiState = MutableStateFlow(CampaignDetailUiState(isLoading = true))
    val uiState: StateFlow<CampaignDetailUiState> = _uiState.asStateFlow()

    init {
        loadCampaign()
    }

    fun loadCampaign() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = campaignRepository.getCampaign(campaignId)) {
                is CampaignResult.Success -> {
                    _uiState.update {
                        it.copy(campaign = result.data, isLoading = false)
                    }
                }

                is CampaignResult.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.message, isLoading = false)
                    }
                }
            }
        }
    }

    fun changeStatus(status: CampaignStatus) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isChangingStatus = true, errorMessage = null, snackbarMessage = null)
            }
            when (val result = campaignRepository.updateCampaignStatus(campaignId, status.value)) {
                is CampaignResult.Success -> {
                    _uiState.update {
                        it.copy(
                            campaign = result.data,
                            isChangingStatus = false,
                            snackbarMessage = "Estado actualizado"
                        )
                    }
                }

                is CampaignResult.Error -> {
                    _uiState.update {
                        it.copy(isChangingStatus = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun deleteCampaign() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            when (val result = campaignRepository.deleteCampaign(campaignId)) {
                is CampaignResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false, deleted = true) }
                }

                is CampaignResult.Error -> {
                    _uiState.update {
                        it.copy(isDeleting = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }
}
