package com.example.dietapp.ui.history

import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.MealType
import com.example.dietapp.domain.MealDateRange
import com.example.dietapp.domain.MealPeriod
import com.example.dietapp.domain.NutritionCalculator
import com.example.dietapp.domain.mealDateInSeoul
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MealHistorySummaryTest {
    private val referenceDate = LocalDate.of(2026, 11, 7)
    private val rice = Food(name = "쌀밥", kcal = 166.0, carb = 37.33, protein = 3.36, fat = 0.32)
    private val kimchi = Food(name = "배추김치", kcal = 38.0, carb = 6.49, protein = 1.98, fat = 0.43)
    private val calculator = NutritionCalculator()

    @Test
    fun dailyTotalsMatchTheRecordSummary() {
        val rows = sampleRows()
        val range = MealPeriod.DAILY.endingOn(referenceDate)
        val daily = summarizeMealHistory(rows, range)
        val record = summarizeMealHistory(rows.filter { it.id == 1L || it.id == 2L })

        assertEquals(referenceDate, range.startDate)
        assertEquals(referenceDate, range.endDate)
        assertEquals(record, daily)
        assertEquals(2, daily.mealCount)
        assertEquals(303.03, daily.totalKcal, 0.000001)
        assertMacros(MealMacros(65.26765, 7.8288, 1.0836), daily.totalMacros)
    }

    @Test
    fun seoulMidnightSeparatesTwoUtcTimesOnTheSameDate() {
        val before = Instant.parse("2026-11-06T14:59:59Z").toEpochMilli()
        val midnight = Instant.parse("2026-11-06T15:00:00Z").toEpochMilli()
        val nextMidnight = Instant.parse("2026-11-07T15:00:00Z").toEpochMilli()
        val daily = summarizeMealHistory(
            listOf(
                meal(1, "2026-11-06T14:59:59Z", MealType.SNACK, rice to 100.0),
                meal(2, "2026-11-06T15:00:00Z", MealType.SNACK, kimchi to 100.0),
                meal(3, "2026-11-07T15:00:00Z", MealType.SNACK, rice to 100.0),
            ),
            MealPeriod.DAILY.endingOn(referenceDate),
        )

        assertEquals(LocalDate.of(2026, 11, 6), mealDateInSeoul(before))
        assertEquals(referenceDate, mealDateInSeoul(midnight))
        assertEquals(LocalDate.of(2026, 11, 8), mealDateInSeoul(nextMidnight))
        assertEquals(1, daily.mealCount)
        assertEquals(38.0, daily.totalKcal, 0.000001)
    }

    @Test
    fun weeklyTotalsStartOnMondayAndIncludeTheReferenceDate() {
        val sunday = meal(7, "2026-11-01T14:59:59Z", MealType.SNACK, rice to 100.0)
        val range = MealPeriod.WEEKLY.endingOn(referenceDate)
        val weekly = summarizeMealHistory(sampleRows() + sunday, range)

        assertEquals(LocalDate.of(2026, 11, 2), range.startDate)
        assertEquals(referenceDate, range.endDate)
        assertEquals(3, weekly.mealCount)
        assertEquals(469.03, weekly.totalKcal, 0.000001)
        assertMacros(MealMacros(102.59765, 11.1888, 1.4036), weekly.totalMacros)
    }

    @Test
    fun weeklyRangeOnMondayIsOnlyThatDay() {
        val monday = LocalDate.of(2026, 11, 2)
        val weekly = summarizeMealHistory(sampleRows(), MealPeriod.WEEKLY.endingOn(monday))

        assertEquals(MealDateRange(monday, monday), MealPeriod.WEEKLY.endingOn(monday))
        assertEquals(1, weekly.mealCount)
        assertEquals(166.0, weekly.totalKcal, 0.000001)
    }

    @Test
    fun last30DaysIncludesBothBoundaryDatesAndAddsAllFourMealTypes() {
        val range = MealPeriod.LAST_30_DAYS.endingOn(referenceDate)
        val month = summarizeMealHistory(sampleRows(), range)

        assertEquals(LocalDate.of(2026, 10, 9), range.startDate)
        assertEquals(referenceDate, range.endDate)
        assertEquals(4, month.mealCount)
        assertEquals(635.03, month.totalKcal, 0.000001)
        assertMacros(MealMacros(139.92765, 14.5488, 1.7236), month.totalMacros)
        assertMacros(MealMacros(37.33, 3.36, 0.32), month.macrosByMealType.getValue(MealType.BREAKFAST))
        assertMacros(MealMacros(58.77765, 5.8488, 0.6536), month.macrosByMealType.getValue(MealType.LUNCH))
        assertMacros(MealMacros(6.49, 1.98, 0.43), month.macrosByMealType.getValue(MealType.DINNER))
        assertMacros(MealMacros(37.33, 3.36, 0.32), month.macrosByMealType.getValue(MealType.SNACK))
    }

    @Test
    fun emptyPeriodKeepsZeroValuesForEveryMealType() {
        val january = LocalDate.of(2027, 1, 1)
        val range = MealPeriod.LAST_30_DAYS.endingOn(january)
        val empty = summarizeMealHistory(sampleRows(), range)

        assertEquals(LocalDate.of(2026, 12, 3), range.startDate)
        assertEquals(0, empty.mealCount)
        assertEquals(0.0, empty.totalKcal, 0.0)
        assertEquals(MealMacros(0.0, 0.0, 0.0), empty.totalMacros)
        assertEquals(MealType.entries.toSet(), empty.macrosByMealType.keys)
        assertTrue(empty.macrosByMealType.values.all { it == MealMacros(0.0, 0.0, 0.0) })
        assertFalse(empty.hasMissingNutrition)
    }

    @Test
    fun rowWithoutMacrosStillCountsAsAMealAndContributesCalories() {
        val row = sampleRows().first().copy(nutrition = null)
        val summary = summarizeMealHistory(listOf(row), MealPeriod.DAILY.endingOn(referenceDate))

        assertEquals(1, summary.mealCount)
        assertEquals(265.03, summary.totalKcal, 0.000001)
        assertEquals(MealMacros(0.0, 0.0, 0.0), summary.macrosByMealType.getValue(MealType.LUNCH))
        assertTrue(summary.hasMissingNutrition)
    }

    private fun sampleRows(): List<MealHistoryRow> = listOf(
        meal(1, "2026-11-07T03:30:00Z", MealType.LUNCH, rice to 150.5, kimchi to 40.0),
        meal(2, "2026-11-07T09:00:00Z", MealType.DINNER, kimchi to 100.0),
        meal(3, "2026-11-01T23:00:00Z", MealType.BREAKFAST, rice to 100.0),
        meal(4, "2026-10-08T15:00:00Z", MealType.SNACK, rice to 100.0),
        meal(5, "2026-10-08T14:59:59Z", MealType.SNACK, rice to 200.0),
        meal(6, "2026-11-07T15:00:00Z", MealType.SNACK, rice to 250.0),
    )

    private fun meal(
        id: Long,
        instant: String,
        type: MealType,
        vararg portions: Pair<Food, Double>,
    ): MealHistoryRow {
        val foods = portions.map { (food, grams) ->
            val nutrients = calculator.calculateNutrients(food, grams)
            MealFoodNutrition(
                name = food.name,
                amountG = grams,
                kcal = nutrients.kcal,
                macros = MealMacros(nutrients.carb, nutrients.protein, nutrients.fat),
            )
        }
        return MealHistoryRow(
            id = id,
            dateTimeMillis = Instant.parse(instant).toEpochMilli(),
            mealType = type,
            totalKcal = foods.sumOf { it.kcal },
            nutrition = MealDetailNutrition(
                totalMacros = MealMacros(
                    carbsG = foods.sumOf { it.macros.carbsG },
                    proteinG = foods.sumOf { it.macros.proteinG },
                    fatG = foods.sumOf { it.macros.fatG },
                ),
                foods = foods,
            ),
        )
    }

    private fun assertMacros(expected: MealMacros, actual: MealMacros) {
        assertEquals(expected.carbsG, actual.carbsG, 0.000001)
        assertEquals(expected.proteinG, actual.proteinG, 0.000001)
        assertEquals(expected.fatG, actual.fatG, 0.000001)
    }
}
