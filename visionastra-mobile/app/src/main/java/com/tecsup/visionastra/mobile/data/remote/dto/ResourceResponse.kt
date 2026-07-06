package com.tecsup.visionastra.mobile.data.remote.dto

import java.math.BigDecimal

data class ResourceResponse(
    val idRecurso: Int,
    val idCampana: Int,
    val nombreCampana: String?,
    val tipo: String,
    val titulo: String?,
    val nombreArchivo: String,
    val urlArchivo: String?,
    val contenidoTexto: String?,
    val pesoMb: BigDecimal?,
    val formato: String?,
    val estado: String,
    val fechaSubida: String?
)
