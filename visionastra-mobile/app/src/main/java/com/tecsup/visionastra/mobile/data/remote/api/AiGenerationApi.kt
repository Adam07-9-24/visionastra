package com.tecsup.visionastra.mobile.data.remote.api

import com.tecsup.visionastra.mobile.data.remote.dto.AiGenerationRequest
import com.tecsup.visionastra.mobile.data.remote.dto.AiGenerationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AiGenerationApi {
    @GET("api/generaciones-ia")
    suspend fun listGenerations(): Response<List<AiGenerationResponse>>

    @GET("api/generaciones-ia/{idGeneracion}")
    suspend fun getGeneration(
        @Path("idGeneracion") idGeneration: Int
    ): Response<AiGenerationResponse>

    @POST("api/generaciones-ia")
    suspend fun createGeneration(
        @Body request: AiGenerationRequest
    ): Response<AiGenerationResponse>

    @PATCH("api/generaciones-ia/{idGeneracion}/preparar-prompt")
    suspend fun preparePrompt(
        @Path("idGeneracion") idGeneration: Int
    ): Response<AiGenerationResponse>

    @PATCH("api/generaciones-ia/{idGeneracion}/generar-video")
    suspend fun generateVideo(
        @Path("idGeneracion") idGeneration: Int
    ): Response<AiGenerationResponse>
}
