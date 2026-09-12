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
    ): Long? {
        val selected = LocalDateTime.of(date, time)
        if (!selected.isAfter(now)) return null
        return selected
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    fun reminderMillis(
        enabled: Boolean,
        date: LocalDate,
        time: LocalTime,
        now: LocalDateTime = LocalDateTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Long? {
        return if (enabled) atDateTime(date, time, now, zoneId) else null
    }

    fun reminderMillisForEdit(
        enabled: Boolean,
        date: LocalDate,
        time: LocalTime,
        originalReminderAt: Long?,
        originalReminderDate: LocalDate?,
        originalReminderTime: LocalTime?,
        now: LocalDateTime = LocalDateTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Long? {
        if (!enabled) return null
        if (originalReminderAt != null && date == originalReminderDate && time == originalReminderTime) {
            return originalReminderAt
        }
        return reminderMillis(true, date, time, now, zoneId)
    }
}
