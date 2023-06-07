package dev.janssenbatista.shopping.list.api.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun formatDate(dateTime: LocalDateTime, pattern: String = "yyyy-MM-dd hh:mm:ss"): String =
        DateTimeFormatter.ofPattern(pattern).format(dateTime)

fun LocalDateTime.format(pattern: String = "yyyy-MM-dd hh:mm:ss"): String =
        DateTimeFormatter
                .ofPattern(pattern)
                .format(this)