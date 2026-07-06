package com.tecsup.visionastra.mobile.ui.resources

import com.tecsup.visionastra.mobile.data.remote.dto.ResourceResponse

data class ResourceListUiState(
    val resources: List<ResourceResponse> = emptyList(),
    val selectedType: String? = null,
    val campaignName: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
) {
    val filteredResources: List<ResourceResponse>
        get() = selectedType?.let { type ->
            resources.filter { it.tipo == type }
        } ?: resources
}
