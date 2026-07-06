package com.tecsup.visionastra.mobile.data.remote.api

import com.tecsup.visionastra.mobile.data.remote.dto.LoginRequest
import com.tecsup.visionastra.mobile.data.remote.dto.LoginResponse
import com.tecsup.visionastra.mobile.data.remote.dto.RefreshTokenRequest
import com.tecsup.visionastra.mobile.data.remote.dto.RefreshTokenResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<ResponseBody>
}
