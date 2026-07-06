package com.tecsup.visionastra.mobile.ui.resources

import com.tecsup.visionastra.mobile.data.remote.dto.ResourceResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class ResourceType(val value: String, val label: String) {
    Image("imagen", "Imagen"),
    Copy("copy", "Copy"),
    Video("video", "Video"),
    Document("documento", "Documento");

    companion object {
        fun fromValue(value: String): ResourceType =
            entries.firstOrNull { it.value == value.lowercase() } ?: Document
    }
}

data class ResourceTypeFilter(
    val label: String,
    val value: String?
)

val resourceTypeFilters = listOf(
    ResourceTypeFilter("Todos", null),
    ResourceTypeFilter("Imágenes", ResourceType.Image.value),
    ResourceTypeFilter("Copy", ResourceType.Copy.value),
    ResourceTypeFilter("Videos", ResourceType.Video.value)
)

fun ResourceResponse.displayTitle(): String =
    titulo?.takeIf { it.isNotBlank() } ?: nombreArchivo

fun String?.formatResourceDate(): String {
    if (isNullOrBlank()) return "Sin fecha"
    return runCatching {
        LocalDateTime.parse(this).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }.recoverCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrElse { this }
}
