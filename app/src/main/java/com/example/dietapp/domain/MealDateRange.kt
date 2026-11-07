package com.example.dietapp.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

internal val MealTimeZone: ZoneId = ZoneId.of("Asia/Seoul")

internal fun mealDateInSeoul(dateTimeMillis: Long): LocalDate =
    Instant.ofEpochMilli(dateTimeMillis).atZone(MealTimeZone).toLocalDate()

internal data class MealDateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    operator fun contains(date: LocalDate): Boolean =
        !date.isBefore(startDate) && !date.isAfter(endDate)
}

internal enum class MealPeriod {
    DAILY,
    WEEKLY,
    LAST_30_DAYS;

    fun endingOn(date: LocalDate): MealDateRange {
        val start = when (this) {
            DAILY -> date
            WEEKLY -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            LAST_30_DAYS -> date.minusDays(29)
        }
        return MealDateRange(start, date)
    }
}
