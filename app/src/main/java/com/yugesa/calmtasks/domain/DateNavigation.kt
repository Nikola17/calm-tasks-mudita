package com.yugesa.calmtasks.domain

import java.time.LocalDate

object DateNavigation {
    fun previousDay(date: LocalDate): LocalDate = date.minusDays(1)
}
