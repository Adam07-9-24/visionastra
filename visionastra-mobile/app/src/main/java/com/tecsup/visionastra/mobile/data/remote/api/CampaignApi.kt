package com.tecsup.visionastra.mobile.data.remote.api

import com.tecsup.visionastra.mobile.data.remote.dto.CampaignRequest
import com.tecsup.visionastra.mobile.data.remote.dto.CampaignResponse
import com.tecsup.visionastra.mobile.data.remote.dto.CampaignStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CampaignApi {
    @GET("api/campanas")
    suspend fun listCampaigns(
        @Query("estado") status: String? = null
    ): Response<List<CampaignResponse>>

    @GET("api/campanas/{idCampana}")
    suspend fun getCampaign(
        @Path("idCampana") idCampaign: Int
    ): Response<CampaignResponse>

    @POST("api/campanas")
    suspend fun createCampaign(
        @Body request: CampaignRequest
    ): Response<CampaignResponse>

    @PUT("api/campanas/{idCampana}")
    suspend fun updateCampaign(
        @Path("idCampana") idCampaign: Int,
        @Body request: CampaignRequest
    ): Response<CampaignResponse>

    @PATCH("api/campanas/{idCampana}/estado")
    suspend fun updateCampaignStatus(
        @Path("idCampana") idCampaign: Int,
        @Body request: CampaignStatusRequest
    ): Response<CampaignResponse>

    @DELETE("api/campanas/{idCampana}")
    suspend fun deleteCampaign(
        @Path("idCampana") idCampaign: Int
    ): Response<Map<String, String>>
}
