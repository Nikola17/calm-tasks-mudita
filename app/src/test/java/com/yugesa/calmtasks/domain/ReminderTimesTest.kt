package com.yugesa.calmtasks.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderTimesTest {
    private val zone = ZoneId.of("UTC")

    @Test
    fun reminderMillisIsNullWhenReminderIsDisabled() {
        assertNull(ReminderTimes.reminderMillis(false, LocalDate.of(2026, 5, 25), LocalTime.of(9, 0)))
    }

    @Test
    fun atDateTimeUsesChosenDateAndPreciseTimeWhenFuture() {
        val timestamp = ReminderTimes.atDateTime(
            date = LocalDate.of(2026, 5, 25),
            time = LocalTime.of(8, 30),
            now = LocalDateTime.of(2026, 5, 24, 20, 0),
            zoneId = zone,
        )

        val expected = LocalDateTime.of(2026, 5, 25, 8, 30)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        assertEquals(expected, timestamp)
    }

    @Test
    fun atDateTimeRejectsPastReminder() {
        val timestamp = ReminderTimes.atDateTime(
            date = LocalDate.of(2026, 5, 24),
            time = LocalTime.of(8, 30),
            now = LocalDateTime.of(2026, 5, 24, 20, 0),
            zoneId = zone,
        )

        assertNull(timestamp)
    }

    @Test
    fun reminderMillisForEditKeepsAnUnchangedExistingReminder() {
        val originalDate = LocalDate.of(2026, 5, 24)
        val originalTime = LocalTime.of(8, 30)
        val originalReminderAt = originalDate.atTime(originalTime)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val timestamp = ReminderTimes.reminderMillisForEdit(
            enabled = true,
            date = originalDate,
            time = originalTime,
            originalReminderAt = originalReminderAt,
            originalReminderDate = originalDate,
            originalReminderTime = originalTime,
            now = LocalDateTime.of(2026, 5, 24, 20, 0),
            zoneId = zone,
        )

        assertEquals(originalReminderAt, timestamp)
    }

    @Test
    fun reminderMillisForEditRejectsAChangedPastReminder() {
        assertNull(
            ReminderTimes.reminderMillisForEdit(
                enabled = true,
                date = LocalDate.of(2026, 5, 24),
                time = LocalTime.of(8, 30),
                originalReminderAt = null,
                originalReminderDate = null,
                originalReminderTime = null,
                now = LocalDateTime.of(2026, 5, 24, 20, 0),
                zoneId = zone,
            ),
        )
    }
}
