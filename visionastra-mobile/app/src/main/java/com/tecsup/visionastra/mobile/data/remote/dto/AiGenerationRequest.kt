package com.tecsup.visionastra.mobile.data.remote.dto

data class AiGenerationRequest(
    val idCampana: Int,
    val idAgente: Int?,
    val prompt: String,
    val tipoSalida: String,
    val idsRecursos: List<Int>
)
