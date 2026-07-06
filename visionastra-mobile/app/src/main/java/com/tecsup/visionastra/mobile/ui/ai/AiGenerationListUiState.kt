package com.tecsup.visionastra.mobile.ui.ai

import com.tecsup.visionastra.mobile.data.remote.dto.AiGenerationResponse

data class AiGenerationListUiState(
    val generations: List<AiGenerationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
