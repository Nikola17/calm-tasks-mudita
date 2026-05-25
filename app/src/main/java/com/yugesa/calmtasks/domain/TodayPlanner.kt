package com.yugesa.calmtasks.domain

import com.yugesa.calmtasks.data.TaskEntity

object TodayPlanner {
    fun activeToday(tasks: List<TaskEntity>, today: String): List<TaskEntity> {
        return tasks
            .filter { it.status == TaskEntity.STATUS_ACTIVE && it.plannedDate == today }
            .sortedWith(compareBy<TaskEntity> { it.createdAt }.thenBy { it.id })
    }

    fun visibleToday(tasks: List<TaskEntity>, today: String, limit: Int): List<TaskEntity> {
        return activeToday(tasks, today).take(limit.coerceAtLeast(1))
    }

    fun overflowToday(tasks: List<TaskEntity>, today: String, limit: Int): List<TaskEntity> {
        return activeToday(tasks, today).drop(limit.coerceAtLeast(1))
    }
}
