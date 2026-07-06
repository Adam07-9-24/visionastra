package com.tecsup.visionastra.mobile.ui.campaigns

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
class CampaignListViewModel @Inject constructor(
    private val campaignRepository: CampaignRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CampaignListUiState(isLoading = true))
    val uiState: StateFlow<CampaignListUiState> = _uiState.asStateFlow()

    init {
        loadCampaigns()
    }

    fun onStatusSelected(status: String?) {
        _uiState.update { it.copy(selectedStatus = status) }
        loadCampaigns()
    }

    fun retry() {
        loadCampaigns()
    }

    fun refresh() {
        loadCampaigns(isRefresh = true)
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    private fun loadCampaigns(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            when (val result = campaignRepository.listCampaigns(_uiState.value.selectedStatus)) {
                is CampaignResult.Success -> {
                    _uiState.update {
                        it.copy(
                            campaigns = result.data,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }

                is CampaignResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

}
