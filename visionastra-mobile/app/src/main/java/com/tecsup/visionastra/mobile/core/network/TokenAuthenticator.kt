package com.tecsup.visionastra.mobile.core.network

import com.tecsup.visionastra.mobile.core.session.SessionManager
import com.tecsup.visionastra.mobile.data.local.TokenStorage
import com.tecsup.visionastra.mobile.data.remote.api.AuthApi
import com.tecsup.visionastra.mobile.data.remote.dto.RefreshTokenRequest
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    @param:RefreshAuthApi
    private val refreshAuthApi: Provider<AuthApi>,
    private val sessionManager: Provider<SessionManager>
) : Authenticator {
    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.isLoginOrRefresh() || response.responseCount >= MAX_AUTH_ATTEMPTS) {
            return null
        }

        return runBlocking {
            refreshMutex.withLock {
                val currentAccessToken = tokenStorage.getAccessToken()
                val requestAccessToken = response.request.bearerToken()

                if (!currentAccessToken.isNullOrBlank() && currentAccessToken != requestAccessToken) {
                    return@withLock response.request.withBearer(currentAccessToken)
                }

                val refreshToken = tokenStorage.getRefreshToken()
                if (refreshToken.isNullOrBlank()) {
                    sessionManager.get().expireSession()
                    return@withLock null
                }

                val refreshResponse = runCatching {
                    refreshAuthApi.get().refresh(RefreshTokenRequest(refreshToken))
                }.getOrNull()

                if (refreshResponse?.isSuccessful == true) {
                    val body = refreshResponse.body()
                    if (body?.token.isNullOrBlank() || body?.refreshToken.isNullOrBlank()) {
                        sessionManager.get().expireSession()
                        return@withLock null
                    }

                    tokenStorage.saveTokens(
                        accessToken = body.token,
                        refreshToken = body.refreshToken
                    )
                    response.request.withBearer(body.token)
                } else {
                    sessionManager.get().expireSession()
                    null
                }
            }
        }
    }

    private fun Request.bearerToken(): String? =
        header("Authorization")?.removePrefix("Bearer ")?.takeIf { it.isNotBlank() }

    private fun Request.withBearer(token: String): Request =
        newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

    private val Response.responseCount: Int
        get() {
            var currentResponse: Response? = this
            var count = 1
            while (currentResponse?.priorResponse != null) {
                count++
                currentResponse = currentResponse.priorResponse
            }
            return count
        }

    private companion object {
        const val MAX_AUTH_ATTEMPTS = 2
    }
}
