package com.tecsup.visionastra.mobile.ui.campaigns

data class CampaignFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedCampaignId: Int? = null,
    val loadErrorMessage: String? = null,
    val errorMessage: String? = null,
    val nombre: String = "",
    val objetivo: String = "",
    val descripcion: String = "",
    val presupuesto: String = "",
    val estado: String = CampaignStatus.Draft.value,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val nombreError: String? = null,
    val presupuestoError: String? = null,
    val fechaError: String? = null
)
