package com.tecsup.visionastra.mobile.ui.ai

import com.tecsup.visionastra.mobile.data.remote.dto.CampaignResponse
import com.tecsup.visionastra.mobile.data.remote.dto.ResourceResponse

data class AiGenerationFormUiState(
    val campaigns: List<CampaignResponse> = emptyList(),
    val resources: List<ResourceResponse> = emptyList(),
    val selectedCampaignId: Int? = null,
    val selectedResourceIds: Set<Int> = emptySet(),
    val prompt: String = "",
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val createdGenerationId: Int? = null,
    val errorMessage: String? = null
) {
    val selectedCampaign: CampaignResponse?
        get() = campaigns.firstOrNull { it.idCampana == selectedCampaignId }

    val inputResources: List<ResourceResponse>
        get() = resources.filter { it.estado == "activo" && (it.tipo == "imagen" || it.tipo == "copy") }
}
