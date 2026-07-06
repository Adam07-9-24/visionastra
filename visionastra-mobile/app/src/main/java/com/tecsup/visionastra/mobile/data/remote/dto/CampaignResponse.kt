package com.tecsup.visionastra.mobile.data.remote.dto

import java.math.BigDecimal

data class CampaignResponse(
    val idCampana: Int,
    val idUsuario: Long,
    val nombre: String,
    val objetivo: String?,
    val descripcion: String?,
    val presupuesto: BigDecimal?,
    val estado: String,
    val fechaInicio: String?,
    val fechaFin: String?,
    val fechaCreacion: String?,
    val fechaActualizacion: String?
)
