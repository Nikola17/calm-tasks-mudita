package com.yugesa.calmtasks.domain

object TaskPlanning {
    fun plannedDateAfterEdit(
        originalDate: String?,
        selectedDate: String,
        initiallyPlannedForSelectedDay: Boolean,
        plannedForSelectedDay: Boolean,
    ): String? {
        if (plannedForSelectedDay == initiallyPlannedForSelectedDay) return originalDate
        return if (plannedForSelectedDay) selectedDate else null
    }
}
