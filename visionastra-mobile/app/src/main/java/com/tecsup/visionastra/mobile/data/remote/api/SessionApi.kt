package com.tecsup.visionastra.mobile.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.POST

interface SessionApi {
    @POST("api/sesiones/heartbeat")
    suspend fun heartbeat(): Response<ResponseBody>
}
