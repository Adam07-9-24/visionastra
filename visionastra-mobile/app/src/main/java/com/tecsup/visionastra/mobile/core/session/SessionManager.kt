package com.tecsup.visionastra.mobile.core.session

import com.tecsup.visionastra.mobile.data.local.AppPreferences
import com.tecsup.visionastra.mobile.data.local.TokenStorage
import com.tecsup.visionastra.mobile.data.remote.api.AuthApi
import com.tecsup.visionastra.mobile.data.remote.api.SessionApi
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AuthUser(
    val idUsuario: Long,
    val nombres: String,
    val apellidos: String?,
    val email: String,
    val rol: String,
    val estado: String
)

sealed interface SessionState {
    data object Loading : SessionState
    data object Unauthenticated : SessionState
    data class Authenticated(val user: AuthUser) : SessionState
}

@Singleton
class SessionManager @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val appPreferences: AppPreferences,
    private val authApi: Provider<AuthApi>,
    private val sessionApi: Provider<SessionApi>
) {
    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    private val _sessionExpiredEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var heartbeatJob: Job? = null

    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    val sessionExpiredEvents: SharedFlow<Unit> = _sessionExpiredEvents.asSharedFlow()

    suspend fun restoreSession(): Boolean {
        val hasTokens = tokenStorage.hasTokens()
        val user = appPreferences.getAuthenticatedUser()

        return if (hasTokens && user != null) {
            _sessionState.value = SessionState.Authenticated(user)
            startHeartbeat()
            true
        } else {
            clearLocalSession(markExpired = false)
            false
        }
    }

    suspend fun setAuthenticatedSession(user: AuthUser) {
        appPreferences.saveAuthenticatedUser(user)
        _sessionState.value = SessionState.Authenticated(user)
        startHeartbeat()
    }

    suspend fun logout() {
        runCatching { authApi.get().logout() }
        clearLocalSession(markExpired = false)
    }

    suspend fun expireSession() {
        clearLocalSession(markExpired = true)
    }

    fun startHeartbeat() {
        if (heartbeatJob?.isActive == true) return

        heartbeatJob = sessionScope.launch {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                val hasSession = sessionState.value is SessionState.Authenticated
                if (!hasSession || !tokenStorage.hasTokens()) continue

                val response = runCatching {
                    sessionApi.get().heartbeat()
                }.getOrNull()

                if (response?.code() == 401) {
                    expireSession()
                    break
                }
            }
        }
    }

    fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private suspend fun clearLocalSession(markExpired: Boolean) {
        stopHeartbeat()
        tokenStorage.clearTokens()
        appPreferences.clearAuthenticatedUser()
        _sessionState.value = SessionState.Unauthenticated
        if (markExpired) {
            _sessionExpiredEvents.tryEmit(Unit)
        }
    }

    private companion object {
        const val HEARTBEAT_INTERVAL_MS = 300_000L
    }
}
