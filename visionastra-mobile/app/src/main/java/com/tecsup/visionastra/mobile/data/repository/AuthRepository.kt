package com.tecsup.visionastra.mobile.data.repository

import com.squareup.moshi.Moshi
import com.tecsup.visionastra.mobile.core.network.parseApiErrorMessage
import com.tecsup.visionastra.mobile.core.session.AuthUser
import com.tecsup.visionastra.mobile.core.session.SessionManager
import com.tecsup.visionastra.mobile.data.local.TokenStorage
import com.tecsup.visionastra.mobile.data.remote.api.AuthApi
import com.tecsup.visionastra.mobile.data.remote.dto.LoginRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface LoginResult {
    data object Success : LoginResult
    data class Error(val message: String) : LoginResult
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager,
    private val moshi: Moshi
) {
    suspend fun login(email: String, password: String): LoginResult {
        val response = try {
            authApi.login(LoginRequest(email = email, password = password))
        } catch (_: IOException) {
            return LoginResult.Error("Sin conexion o servidor no disponible.")
        } catch (_: Exception) {
            return LoginResult.Error("Error inesperado al iniciar sesion.")
        }

        if (!response.isSuccessful) {
            val message = parseApiErrorMessage(response, moshi).toLoginMessage(response.code())
            return LoginResult.Error(message)
        }

        val body = response.body()
            ?: return LoginResult.Error("Respuesta invalida del servidor.")

        tokenStorage.saveTokens(
            accessToken = body.token,
            refreshToken = body.refreshToken
        )
        sessionManager.setAuthenticatedSession(
            AuthUser(
                idUsuario = body.idUsuario,
                nombres = body.nombres,
                apellidos = body.apellidos,
                email = body.email,
                rol = body.rol,
                estado = body.estado
            )
        )

        return LoginResult.Success
    }

    suspend fun logout() {
        sessionManager.logout()
    }

    private fun String.toLoginMessage(code: Int): String =
        when (code) {
            400 -> when {
                contains("bloque", ignoreCase = true) ||
                    contains("activo", ignoreCase = true) ||
                    contains("inactivo", ignoreCase = true) -> "Usuario bloqueado o no activo."
                else -> "Credenciales incorrectas."
            }
            401 -> "Sesion expirada. Inicia sesion nuevamente."
            403 -> "Acceso denegado."
            in 500..599 -> "Servidor no disponible."
            else -> if (isBlank()) "Error inesperado." else this
        }
}
