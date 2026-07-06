package com.tecsup.visionastra.mobile.ui.campaigns

import com.tecsup.visionastra.mobile.data.remote.dto.CampaignResponse

data class CampaignDetailUiState(
    val campaign: CampaignResponse? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val isChangingStatus: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val deleted: Boolean = false
)
