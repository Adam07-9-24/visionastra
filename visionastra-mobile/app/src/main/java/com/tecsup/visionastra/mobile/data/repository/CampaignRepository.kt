package com.tecsup.visionastra.mobile.data.repository

import com.squareup.moshi.Moshi
import com.tecsup.visionastra.mobile.core.network.parseApiErrorMessage
import com.tecsup.visionastra.mobile.data.remote.api.CampaignApi
import com.tecsup.visionastra.mobile.data.remote.dto.CampaignRequest
import com.tecsup.visionastra.mobile.data.remote.dto.CampaignResponse
import com.tecsup.visionastra.mobile.data.remote.dto.CampaignStatusRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

sealed interface CampaignResult<out T> {
    data class Success<T>(val data: T) : CampaignResult<T>
    data class Error(val message: String, val code: Int? = null) : CampaignResult<Nothing>
}

@Singleton
class CampaignRepository @Inject constructor(
    private val campaignApi: CampaignApi,
    private val moshi: Moshi
) {
    suspend fun listCampaigns(status: String?): CampaignResult<List<CampaignResponse>> =
        execute { campaignApi.listCampaigns(status) }

    suspend fun getCampaign(idCampaign: Int): CampaignResult<CampaignResponse> =
        execute { campaignApi.getCampaign(idCampaign) }

    suspend fun createCampaign(request: CampaignRequest): CampaignResult<CampaignResponse> =
        execute { campaignApi.createCampaign(request) }

    suspend fun updateCampaign(
        idCampaign: Int,
        request: CampaignRequest
    ): CampaignResult<CampaignResponse> =
        execute { campaignApi.updateCampaign(idCampaign, request) }

    suspend fun updateCampaignStatus(
        idCampaign: Int,
        status: String
    ): CampaignResult<CampaignResponse> =
        execute { campaignApi.updateCampaignStatus(idCampaign, CampaignStatusRequest(status)) }

    suspend fun deleteCampaign(idCampaign: Int): CampaignResult<Unit> {
        val result = execute { campaignApi.deleteCampaign(idCampaign) }
        return when (result) {
            is CampaignResult.Success -> CampaignResult.Success(Unit)
            is CampaignResult.Error -> result
        }
    }

    private suspend fun <T> execute(
        call: suspend () -> Response<T>
    ): CampaignResult<T> {
        val response = try {
            call()
        } catch (_: IOException) {
            return CampaignResult.Error("Sin conexion o servidor no disponible.")
        } catch (_: Exception) {
            return CampaignResult.Error("Error inesperado.")
        }

        if (!response.isSuccessful) {
            return CampaignResult.Error(
                message = parseApiErrorMessage(response, moshi).toCampaignMessage(response.code()),
                code = response.code()
            )
        }

        val body = response.body()
        return if (body != null) {
            CampaignResult.Success(body)
        } else {
            CampaignResult.Error("Respuesta vacia del servidor.")
        }
    }

    private fun String.toCampaignMessage(code: Int): String =
        when (code) {
            400 -> ifBlank { "Revisa los datos de la campana." }
            401 -> "Sesion expirada. Inicia sesion nuevamente."
            403 -> "No tienes permiso para esta accion."
            in 500..599 -> "Servidor no disponible."
            else -> ifBlank { "Error inesperado." }
        }
}
