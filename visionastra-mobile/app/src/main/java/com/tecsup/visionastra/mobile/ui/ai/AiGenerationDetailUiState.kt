package com.tecsup.visionastra.mobile.ui.ai

import com.tecsup.visionastra.mobile.data.remote.dto.AiGenerationResponse

data class AiGenerationDetailUiState(
    val generation: AiGenerationResponse? = null,
    val isLoading: Boolean = false,
    val isPreparingPrompt: Boolean = false,
    val isVideoGenerationRequested: Boolean = false,
    val isGeneratingVideo: Boolean = false,
    val showGenerateVideoConfirmation: Boolean = false,
    val errorMessage: String? = null
) {
    val hasPreparedPrompt: Boolean
        get() = !generation?.resumenContexto.isNullOrBlank() &&
            !generation?.guionGenerado.isNullOrBlank() &&
            !generation?.promptFinalEspanol.isNullOrBlank() &&
            !generation?.promptFinal.isNullOrBlank()

    val hasCompletedVideo: Boolean
        get() = generation?.idRecursoResultado != null

    val shouldShowVideoInProgress: Boolean
        get() = isVideoGenerationRequested && !hasCompletedVideo && generation?.estado != "error"
}
