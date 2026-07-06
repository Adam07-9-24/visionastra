package com.tecsup.visionastra.mobile.core.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.ResponseBody
import retrofit2.Response

fun parseApiErrorMessage(
    response: Response<*>,
    moshi: Moshi
): String {
    val rawBody = response.errorBody()?.safeString().orEmpty()
    val parsedMessage = rawBody.messageFromJson(moshi)

    return when {
        !parsedMessage.isNullOrBlank() -> parsedMessage
        rawBody.isNotBlank() -> rawBody
        response.code() == 400 -> "Solicitud invalida."
        response.code() == 401 -> "Sesion expirada. Inicia sesion nuevamente."
        response.code() == 403 -> "Acceso denegado."
        response.code() in 500..599 -> "Servidor no disponible."
        else -> "Error inesperado."
    }
}

private fun ResponseBody.safeString(): String =
    runCatching { string() }.getOrDefault("")

private fun String.messageFromJson(moshi: Moshi): String? {
    if (isBlank()) return null
    return runCatching {
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Any::class.java
        )
        val adapter = moshi.adapter<Map<String, Any?>>(type)
        val body = adapter.fromJson(this)
        body?.get("mensaje") as? String ?: body?.get("error") as? String
    }.getOrNull()
}
