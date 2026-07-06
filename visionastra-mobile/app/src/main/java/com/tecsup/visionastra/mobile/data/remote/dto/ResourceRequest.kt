package com.tecsup.visionastra.mobile.data.remote.dto

import java.math.BigDecimal

data class ResourceRequest(
    val idCampana: Int,
    val tipo: String,
    val titulo: String?,
    val nombreArchivo: String,
    val urlArchivo: String?,
    val contenidoTexto: String?,
    val pesoMb: BigDecimal?,
    val formato: String?
)
