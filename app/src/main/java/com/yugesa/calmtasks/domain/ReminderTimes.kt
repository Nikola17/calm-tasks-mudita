package com.yugesa.calmtasks.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ReminderTimes {
    fun atDateTime(
        date: LocalDate,
        time: LocalTime,
        now: LocalDateTime = LocalDateTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Long {
        val selected = LocalDateTime.of(date, time)
        val adjusted = if (selected.isAfter(now)) selected else now.plusHours(1)
        return adjusted
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    fun reminderMillis(enabled: Boolean, date: LocalDate, time: LocalTime): Long? {
        return if (enabled) atDateTime(date, time) else null
    }
}
