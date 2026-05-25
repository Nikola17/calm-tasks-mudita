package com.yugesa.calmtasks.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object CalmDateTimeLabels {
    fun localizedDate(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        val raw = date.format(DateTimeFormatter.ofPattern("EEE dd/MM", locale))
        return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    fun reminderLabel(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String {
        val dateTime = Instant.ofEpochMilli(timestamp)
            .atZone(zoneId)
            .toLocalDateTime()
        return "${localizedDate(dateTime.toLocalDate(), locale)} ${dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))}"
    }
}
