package com.yugesa.calmtasks.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class CalmDateTimeLabelsTest {
    @Test
    fun localizedDateIncludesTranslatedWeekdayAndDate() {
        assertEquals("Lun. 25/05", CalmDateTimeLabels.localizedDate(LocalDate.of(2026, 5, 25), Locale.FRANCE))
        assertEquals("Lun 25/05", CalmDateTimeLabels.localizedDate(LocalDate.of(2026, 5, 25), Locale.ITALY))
        assertEquals("Mo. 25/05", CalmDateTimeLabels.localizedDate(LocalDate.of(2026, 5, 25), Locale.GERMANY))
    }

    @Test
    fun reminderLabelIncludesLocalizedDateAndPreciseTime() {
        val zone = ZoneId.of("UTC")
        val timestamp = LocalDateTime.of(2026, 5, 26, 14, 30)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        assertEquals("Mar. 26/05 14:30", CalmDateTimeLabels.reminderLabel(timestamp, zone, Locale.FRANCE))
    }
}
