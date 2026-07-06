package com.tecsup.visionastra.mobile.data.remote.dto

data class AiGenerationResponse(
    val idGeneracion: Int,
    val idUsuario: Long?,
    val nombreUsuario: String?,
    val idCampana: Int,
    val nombreCampana: String?,
    val idAgente: Int?,
    val nombreAgente: String?,
    val prompt: String,
    val resumenContexto: String?,
    val guionGenerado: String?,
    val promptFinalEspanol: String?,
    val promptFinal: String?,
    val proveedorPrompt: String?,
    val proveedorVideo: String?,
    val tipoSalida: String,
    val estado: String,
    val mensajeError: String?,
    val idRecursoResultado: Int?,
    val tituloRecursoResultado: String?,
    val tipoRecursoResultado: String?,
    val recursosEntrada: List<AiInputResourceResponse>,
    val fechaCreacion: String?,
    val fechaActualizacion: String?
)

data class AiInputResourceResponse(
    val idRecurso: Int,
    val titulo: String?,
    val tipo: String,
    val nombreArchivo: String?,
    val rolRecurso: String
)
