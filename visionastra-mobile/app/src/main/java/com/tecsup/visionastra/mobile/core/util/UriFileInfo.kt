package com.tecsup.visionastra.mobile.core.util

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

data class UriFileInfo(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long?,
    val mimeType: String?
)

fun ContentResolver.getUriFileInfo(uri: Uri): UriFileInfo {
    var displayName: String? = null
    var sizeBytes: Long? = null

    query(uri, null, null, null, null)?.use { cursor: Cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (nameIndex >= 0) displayName = cursor.getString(nameIndex)
            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) sizeBytes = cursor.getLong(sizeIndex)
        }
    }

    return UriFileInfo(
        uri = uri,
        displayName = displayName?.takeIf { it.isNotBlank() } ?: "imagen",
        sizeBytes = sizeBytes,
        mimeType = getType(uri)
    )
}

fun UriFileInfo.validateAndroidImage(): String? {
    val mime = mimeType.orEmpty().lowercase()
    val extension = displayName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
    val acceptedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")
    val acceptedExtensions = setOf("jpg", "jpeg", "png", "webp")

    return when {
        mime !in acceptedMimeTypes -> "Selecciona una imagen JPG, PNG o WEBP."
        extension.isNotBlank() && extension !in acceptedExtensions -> {
            "El archivo debe ser JPG, JPEG, PNG o WEBP."
        }
        displayName.length > 255 -> "El nombre del archivo no puede superar los 255 caracteres."
        sizeBytes == null -> "No se pudo leer el tamaño del archivo."
        sizeBytes <= 0L -> "El archivo seleccionado esta vacio."
        sizeBytes > MAX_IMAGE_SIZE_BYTES -> "La imagen no puede superar los 10 MB."
        else -> null
    }
}

private const val MAX_IMAGE_SIZE_BYTES = 10L * 1024L * 1024L
