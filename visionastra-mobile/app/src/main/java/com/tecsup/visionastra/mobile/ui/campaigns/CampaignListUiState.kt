package com.tecsup.visionastra.mobile.ui.campaigns

import com.tecsup.visionastra.mobile.data.remote.dto.CampaignResponse

data class CampaignListUiState(
    val campaigns: List<CampaignResponse> = emptyList(),
    val selectedStatus: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)
