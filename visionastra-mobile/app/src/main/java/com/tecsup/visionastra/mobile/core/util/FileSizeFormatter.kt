package com.tecsup.visionastra.mobile.core.util

import java.math.BigDecimal
import java.math.RoundingMode

fun Long?.formatFileSize(): String =
    when {
        this == null -> "Tamaño no disponible"
        this < 1024L -> "$this B"
        this < 1024L * 1024L -> "${this / 1024L} KB"
        else -> BigDecimal(this)
            .divide(BigDecimal(1024L * 1024L), 2, RoundingMode.HALF_UP)
            .toPlainString() + " MB"
    }

fun BigDecimal?.formatMb(): String =
    this?.setScale(2, RoundingMode.HALF_UP)?.toPlainString()?.plus(" MB") ?: "Tamaño no disponible"
