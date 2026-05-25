package com.yugesa.calmtasks.domain

import com.yugesa.calmtasks.data.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayPlannerTest {
    @Test
    fun visibleTodayUsesDefaultStyleLimit() {
        val tasks = (1L..5L).map { id -> task(id = id, plannedDate = "2026-05-24") }

        val visible = TodayPlanner.visibleToday(tasks, "2026-05-24", 3)
        val overflow = TodayPlanner.overflowToday(tasks, "2026-05-24", 3)

        assertEquals(listOf(1L, 2L, 3L), visible.map { it.id })
        assertEquals(listOf(4L, 5L), overflow.map { it.id })
    }

    @Test
    fun settingsDefaultKeepsTodayLimitAtThree() {
        assertEquals(3, com.yugesa.calmtasks.data.SettingsEntity().todayPriorityLimit)
    }

    @Test
    fun activeTodayIgnoresDoneInboxAndOtherDates() {
        val tasks = listOf(
            task(id = 1, plannedDate = "2026-05-24"),
            task(id = 2, plannedDate = null),
            task(id = 3, plannedDate = "2026-05-25"),
            task(id = 4, plannedDate = "2026-05-24", status = TaskEntity.STATUS_DONE),
        )

        val visible = TodayPlanner.visibleToday(tasks, "2026-05-24", 5)

        assertEquals(listOf(1L), visible.map { it.id })
    }

    @Test
    fun limitIsClampedOnlyToMinimum() {
        val tasks = (1L..9L).map { id -> task(id = id, plannedDate = "2026-05-24") }

        assertEquals(1, TodayPlanner.visibleToday(tasks, "2026-05-24", 0).size)
        assertEquals(9, TodayPlanner.visibleToday(tasks, "2026-05-24", 9).size)
    }

    private fun task(
        id: Long,
        plannedDate: String?,
        status: String = TaskEntity.STATUS_ACTIVE,
    ): TaskEntity {
        return TaskEntity(
            id = id,
            title = "Task $id",
            folderId = null,
            status = status,
            plannedDate = plannedDate,
            reminderAt = null,
            createdAt = id,
            updatedAt = id,
        )
    }
}
