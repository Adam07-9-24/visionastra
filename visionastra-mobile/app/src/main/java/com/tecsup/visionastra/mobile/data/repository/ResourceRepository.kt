package com.tecsup.visionastra.mobile.data.repository

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import com.tecsup.visionastra.mobile.core.network.ContentUriRequestBody
import com.tecsup.visionastra.mobile.core.network.NetworkConstants
import com.tecsup.visionastra.mobile.core.network.parseApiErrorMessage
import com.tecsup.visionastra.mobile.core.util.UriFileInfo
import com.tecsup.visionastra.mobile.core.util.getUriFileInfo
import com.tecsup.visionastra.mobile.core.util.validateAndroidImage
import com.tecsup.visionastra.mobile.data.remote.api.ResourceApi
import com.tecsup.visionastra.mobile.data.remote.dto.ResourceRequest
import com.tecsup.visionastra.mobile.data.remote.dto.ResourceResponse
import com.tecsup.visionastra.mobile.data.remote.dto.ResourceTitleRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

sealed interface ResourceResult<out T> {
    data class Success<T>(val data: T) : ResourceResult<T>
    data class Error(val message: String, val code: Int? = null) : ResourceResult<Nothing>
}

@Singleton
class ResourceRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val resourceApi: ResourceApi,
    private val moshi: Moshi
) {
    suspend fun listByCampaign(idCampaign: Int): ResourceResult<List<ResourceResponse>> =
        execute { resourceApi.listByCampaign(idCampaign) }

    suspend fun getResource(idResource: Int): ResourceResult<ResourceResponse> =
        execute { resourceApi.getResource(idResource) }

    suspend fun createCopy(
        idCampaign: Int,
        title: String,
        content: String
    ): ResourceResult<ResourceResponse> {
        val fileName = title.trim().ifBlank { "copy" }.toResourceFileName()
        return execute {
            resourceApi.createResource(
                ResourceRequest(
                    idCampana = idCampaign,
                    tipo = RESOURCE_TYPE_COPY,
                    titulo = title.trim(),
                    nombreArchivo = fileName,
                    urlArchivo = null,
                    contenidoTexto = content.trim(),
                    pesoMb = null,
                    formato = "texto"
                )
            )
        }
    }

    suspend fun uploadImage(
        idCampaign: Int,
        title: String?,
        uri: Uri
    ): ResourceResult<ResourceResponse> {
        val resolver = context.contentResolver
        val info = resolver.getUriFileInfo(uri)
        val validationError = info.validateAndroidImage()
        if (validationError != null) return ResourceResult.Error(validationError)

        val mediaType = info.mimeType?.toMediaTypeOrNull()
        val fileBody = ContentUriRequestBody(
            contentResolver = resolver,
            uri = uri,
            mediaType = mediaType,
            contentLength = info.sizeBytes
        )
        val filePart = MultipartBody.Part.createFormData(
            name = "archivo",
            filename = info.displayName,
            body = fileBody
        )

        return execute {
            resourceApi.uploadImage(
                idCampaign = idCampaign.toString().toPlainTextBody(),
                type = RESOURCE_TYPE_IMAGE.toPlainTextBody(),
                title = title?.trim()?.takeIf { it.isNotBlank() }?.toPlainTextBody(),
                file = filePart
            )
        }
    }

    suspend fun updateTitle(idResource: Int, title: String): ResourceResult<ResourceResponse> =
        execute {
            resourceApi.updateTitle(
                idResource = idResource,
                request = ResourceTitleRequest(title.trim())
            )
        }

    suspend fun deleteResource(idResource: Int): ResourceResult<Unit> {
        val result = execute { resourceApi.deleteResource(idResource) }
        return when (result) {
            is ResourceResult.Success -> ResourceResult.Success(Unit)
            is ResourceResult.Error -> result
        }
    }

    fun fileUrl(idResource: Int): String =
        "${NetworkConstants.BASE_URL}api/recursos/archivo/$idResource"

    fun readImageInfo(uri: Uri): UriFileInfo =
        context.contentResolver.getUriFileInfo(uri)

    private suspend fun <T> execute(
        call: suspend () -> Response<T>
    ): ResourceResult<T> {
        val response = try {
            call()
        } catch (_: IOException) {
            return ResourceResult.Error("Sin conexion o servidor no disponible.")
        } catch (_: Exception) {
            return ResourceResult.Error("Error inesperado.")
        }

        if (!response.isSuccessful) {
            return ResourceResult.Error(
                message = parseApiErrorMessage(response, moshi).toResourceMessage(response.code()),
                code = response.code()
            )
        }

        val body = response.body()
        return if (body != null) {
            ResourceResult.Success(body)
        } else {
            ResourceResult.Error("Respuesta vacia del servidor.")
        }
    }

    private fun String.toResourceMessage(code: Int): String =
        when (code) {
            400 -> ifBlank { "Revisa los datos del recurso." }
            401 -> "Sesion expirada. Inicia sesion nuevamente."
            403 -> "No tienes permiso para esta accion."
            in 500..599 -> "Servidor no disponible."
            else -> ifBlank { "Error inesperado." }
        }

    private fun String.toPlainTextBody() =
        toRequestBody("text/plain".toMediaTypeOrNull())

    private fun String.toResourceFileName(): String =
        lowercase()
            .replace(Regex("[^a-z0-9._-]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .ifBlank { "copy" }
            .take(255)

    private companion object {
        const val RESOURCE_TYPE_COPY = "copy"
        const val RESOURCE_TYPE_IMAGE = "imagen"
    }
}
