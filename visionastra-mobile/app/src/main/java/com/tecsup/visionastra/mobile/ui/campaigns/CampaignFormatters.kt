package com.tecsup.visionastra.mobile.ui.campaigns

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

enum class CampaignStatus(val value: String, val label: String) {
    Draft("borrador", "Borrador"),
    Active("activa", "Activa"),
    Paused("pausada", "Pausada"),
    Finished("finalizada", "Finalizada");

    companion object {
        fun fromValue(value: String): CampaignStatus =
            entries.firstOrNull { it.value == value.lowercase() } ?: Draft
    }
}

data class CampaignStatusFilter(
    val label: String,
    val value: String?
)

val campaignStatusFilters = listOf(
    CampaignStatusFilter("Todas", null),
    CampaignStatusFilter("Borrador", CampaignStatus.Draft.value),
    CampaignStatusFilter("Activas", CampaignStatus.Active.value),
    CampaignStatusFilter("Pausadas", CampaignStatus.Paused.value),
    CampaignStatusFilter("Finalizadas", CampaignStatus.Finished.value)
)

val campaignStatuses = listOf(
    CampaignStatus.Draft,
    CampaignStatus.Active,
    CampaignStatus.Paused,
    CampaignStatus.Finished
)

fun BigDecimal?.formatBudget(): String =
    this?.let {
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(it)
    } ?: "Sin presupuesto"

fun String?.formatCampaignDateTime(): String {
    if (isNullOrBlank()) return "Sin fecha"
    return runCatching {
        LocalDateTime.parse(this).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }.recoverCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrElse {
        this
    }
}

fun String?.formatCampaignDate(): String {
    if (isNullOrBlank()) return "Sin fecha"
    return runCatching {
        LocalDateTime.parse(this).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.recoverCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrElse {
        this
    }
}

fun epochMillisToBackendDateTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay()
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

fun backendDateTimeToEpochMillis(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return try {
        LocalDateTime.parse(value)
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }
}
