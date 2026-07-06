package com.tecsup.visionastra.mobile.data.repository

import com.squareup.moshi.Moshi
import com.tecsup.visionastra.mobile.core.network.parseApiErrorMessage
import com.tecsup.visionastra.mobile.data.remote.api.AiGenerationApi
import com.tecsup.visionastra.mobile.data.remote.dto.AiGenerationRequest
import com.tecsup.visionastra.mobile.data.remote.dto.AiGenerationResponse
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

sealed interface AiGenerationResult<out T> {
    data class Success<T>(val data: T) : AiGenerationResult<T>
    data class Error(val message: String, val code: Int? = null) : AiGenerationResult<Nothing>
}

@Singleton
class AiGenerationRepository @Inject constructor(
    private val api: AiGenerationApi,
    private val moshi: Moshi
) {
    suspend fun listGenerations(): AiGenerationResult<List<AiGenerationResponse>> =
        execute { api.listGenerations() }

    suspend fun getGeneration(idGeneration: Int): AiGenerationResult<AiGenerationResponse> =
        execute { api.getGeneration(idGeneration) }

    suspend fun createGeneration(
        idCampaign: Int,
        prompt: String,
        resourceIds: List<Int>
    ): AiGenerationResult<AiGenerationResponse> =
        execute {
            api.createGeneration(
                AiGenerationRequest(
                    idCampana = idCampaign,
                    idAgente = null,
                    prompt = prompt.trim(),
                    tipoSalida = "video",
                    idsRecursos = resourceIds
                )
            )
        }

    suspend fun preparePrompt(idGeneration: Int): AiGenerationResult<AiGenerationResponse> =
        execute { api.preparePrompt(idGeneration) }

    suspend fun generateVideo(idGeneration: Int): AiGenerationResult<AiGenerationResponse> =
        execute { api.generateVideo(idGeneration) }

    private suspend fun <T> execute(call: suspend () -> Response<T>): AiGenerationResult<T> {
        val response = try {
            call()
        } catch (_: IOException) {
            return AiGenerationResult.Error("Sin conexion o servidor no disponible.")
        } catch (_: Exception) {
            return AiGenerationResult.Error("Error inesperado.")
        }

        if (!response.isSuccessful) {
            return AiGenerationResult.Error(
                message = parseApiErrorMessage(response, moshi).toAiMessage(response.code()),
                code = response.code()
            )
        }

        return response.body()?.let { AiGenerationResult.Success(it) }
            ?: AiGenerationResult.Error("Respuesta vacia del servidor.")
    }

    private fun String.toAiMessage(code: Int): String =
        when (code) {
            400 -> ifBlank { "Revisa los datos de la generacion IA." }
            401 -> "Sesion expirada. Inicia sesion nuevamente."
            403 -> "No tienes permiso para esta accion."
            429 -> "Limite o cuota del servicio de IA alcanzado. Intenta mas tarde."
            in 500..599 -> "Servidor no disponible o proveedor IA temporalmente inaccesible."
            else -> ifBlank { "Error inesperado." }
        }
}
