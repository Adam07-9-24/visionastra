package com.tecsup.visionastra.mobile.ui.ai

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class GenerationStatus(val value: String, val label: String) {
    Pending("pendiente", "Generación en espera"),
    Processing("procesando", "Procesando la generacion"),
    Completed("completado", "Tu video está listo"),
    Error("error", "No se pudo completar la generación");

    companion object {
        fun fromValue(value: String): GenerationStatus =
            entries.firstOrNull { it.value == value.lowercase() } ?: Pending
    }
}

fun String?.formatAiDate(): String {
    if (isNullOrBlank()) return "Sin fecha"
    return runCatching {
        LocalDateTime.parse(this).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }.recoverCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrElse { this }
}
