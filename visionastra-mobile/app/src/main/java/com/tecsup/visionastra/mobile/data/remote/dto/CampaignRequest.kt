package com.tecsup.visionastra.mobile.data.remote.dto

import java.math.BigDecimal

data class CampaignRequest(
    val nombre: String,
    val objetivo: String?,
    val descripcion: String?,
    val presupuesto: BigDecimal?,
    val estado: String,
    val fechaInicio: String?,
    val fechaFin: String?
)
