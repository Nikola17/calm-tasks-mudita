package com.yugesa.calmtasks.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DateNavigationTest {
    @Test
    fun previousDayCanReachAdayBeforeToday() {
        assertEquals(
            LocalDate.of(2026, 5, 23),
            DateNavigation.previousDay(LocalDate.of(2026, 5, 24)),
        )
    }
}
