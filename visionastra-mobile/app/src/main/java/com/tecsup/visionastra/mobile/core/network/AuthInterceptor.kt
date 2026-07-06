package com.tecsup.visionastra.mobile.core.network

import com.tecsup.visionastra.mobile.BuildConfig
import com.tecsup.visionastra.mobile.data.local.TokenStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val builder = request.newBuilder()

        if (path.isLoginOrRefresh()) {
            builder.header("User-Agent", "VisionAstra-Android/${BuildConfig.VERSION_NAME}")
            return chain.proceed(builder.build())
        }

        val accessToken = runBlocking { tokenStorage.getAccessToken() }
        if (!accessToken.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(builder.build())
    }
}

fun String.isLoginOrRefresh(): Boolean =
    this == "/api/auth/login" || this == "/api/auth/refresh"
