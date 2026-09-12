package com.yugesa.calmtasks.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskPlanningTest {
    @Test
    fun unchangedPlanKeepsTheTaskOriginalDate() {
        assertEquals(
            "2026-05-26",
            TaskPlanning.plannedDateAfterEdit(
                originalDate = "2026-05-26",
                selectedDate = "2026-05-25",
                initiallyPlannedForSelectedDay = false,
                plannedForSelectedDay = false,
            ),
        )
    }

    @Test
    fun enablingPlanMovesTheTaskToTheSelectedDate() {
        assertEquals(
            "2026-05-25",
            TaskPlanning.plannedDateAfterEdit(
                originalDate = null,
                selectedDate = "2026-05-25",
                initiallyPlannedForSelectedDay = false,
                plannedForSelectedDay = true,
            ),
        )
    }

    @Test
    fun disablingPlanClearsTheTaskDate() {
        assertNull(
            TaskPlanning.plannedDateAfterEdit(
                originalDate = "2026-05-25",
                selectedDate = "2026-05-25",
                initiallyPlannedForSelectedDay = true,
                plannedForSelectedDay = false,
            ),
        )
    }
}
