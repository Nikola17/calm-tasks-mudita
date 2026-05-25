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
    fun atDateTimeMovesPastReminderOneHourAhead() {
        val timestamp = ReminderTimes.atDateTime(
            date = LocalDate.of(2026, 5, 24),
            time = LocalTime.of(8, 30),
            now = LocalDateTime.of(2026, 5, 24, 20, 0),
            zoneId = zone,
        )

        val expected = LocalDateTime.of(2026, 5, 24, 21, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        assertEquals(expected, timestamp)
    }
}
