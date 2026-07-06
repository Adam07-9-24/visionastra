package com.tecsup.visionastra.mobile.data.remote.api

import com.tecsup.visionastra.mobile.data.remote.dto.ResourceRequest
import com.tecsup.visionastra.mobile.data.remote.dto.ResourceResponse
import com.tecsup.visionastra.mobile.data.remote.dto.ResourceTitleRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ResourceApi {
    @GET("api/recursos/campana/{idCampana}")
    suspend fun listByCampaign(@Path("idCampana") idCampaign: Int): Response<List<ResourceResponse>>

    @GET("api/recursos/{idRecurso}")
    suspend fun getResource(@Path("idRecurso") idResource: Int): Response<ResourceResponse>

    @POST("api/recursos")
    suspend fun createResource(@Body request: ResourceRequest): Response<ResourceResponse>

    @Multipart
    @POST("api/recursos/upload")
    suspend fun uploadImage(
        @Part("idCampana") idCampaign: RequestBody,
        @Part("tipo") type: RequestBody,
        @Part("titulo") title: RequestBody?,
        @Part file: MultipartBody.Part
    ): Response<ResourceResponse>

    @PATCH("api/recursos/{idRecurso}/titulo")
    suspend fun updateTitle(
        @Path("idRecurso") idResource: Int,
        @Body request: ResourceTitleRequest
    ): Response<ResourceResponse>

    @GET("api/recursos/archivo/{idRecurso}")
    suspend fun getFile(@Path("idRecurso") idResource: Int): Response<ResponseBody>

    @DELETE("api/recursos/{idRecurso}")
    suspend fun deleteResource(@Path("idRecurso") idResource: Int): Response<Map<String, String>>
}
