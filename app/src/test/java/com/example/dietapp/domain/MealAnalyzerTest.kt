package com.example.dietapp.domain

import com.example.dietapp.data.model.DetailedMeal
import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.Meal
import com.example.dietapp.data.model.MealItemWithFood
import com.example.dietapp.data.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class MealAnalyzerTest {

    private lateinit var analyzer: MealAnalyzer
    private lateinit var sampleFood1: Food
    private lateinit var sampleFood2: Food

    @Before
    fun setUp() {
        analyzer = MealAnalyzer()
        sampleFood1 = Food(id = 1, name = "밥", kcal = 100.0, carb = 20.0, protein = 3.0, fat = 1.0)
        sampleFood2 = Food(id = 2, name = "국", kcal = 200.0, carb = 10.0, protein = 15.0, fat = 5.0)
    }

    // 7. 시간으로 끼니 자동 추천 (예: 8시 → 조식)
    @Test
    fun test7_recommendMealTypeByTime() {
        assertEquals(MealType.BREAKFAST, MealType.recommendByTime(8))   // 8시 -> 조식
        assertEquals(MealType.LUNCH, MealType.recommendByTime(12))      // 12시 -> 중식
        assertEquals(MealType.DINNER, MealType.recommendByTime(18))     // 18시 -> 석식
        assertEquals(MealType.SNACK, MealType.recommendByTime(23))      // 23시 -> 간식/음료
    }

    // 8. 날짜별 끼니 수 집계
    @Test
    fun test8_aggregateMealCountByDate() {
        val date1 = LocalDate.of(2026, 11, 1)
        val date2 = LocalDate.of(2026, 11, 2)

        val meals = listOf(
            DetailedMeal(Meal(id = 1, dateTime = date1.atTime(8, 0), mealType = MealType.BREAKFAST)),
            DetailedMeal(Meal(id = 2, dateTime = date1.atTime(12, 0), mealType = MealType.LUNCH)),
            DetailedMeal(Meal(id = 3, dateTime = date2.atTime(18, 0), mealType = MealType.DINNER))
        )

        val countByDate = analyzer.aggregateMealCountByDate(meals)

        assertEquals(2, countByDate[date1])
        assertEquals(1, countByDate[date2])
    }

    // 9. 날짜별 kcal 합계
    @Test
    fun test9_aggregateTotalKcalByDate() {
        val date1 = LocalDate.of(2026, 11, 1)
        val date2 = LocalDate.of(2026, 11, 2)

        val meal1 = DetailedMeal(
            meal = Meal(id = 1, dateTime = date1.atTime(8, 0), mealType = MealType.BREAKFAST),
            items = listOf(MealItemWithFood(sampleFood1, 100.0)) // 100 kcal
        )
        val meal2 = DetailedMeal(
            meal = Meal(id = 2, dateTime = date1.atTime(12, 0), mealType = MealType.LUNCH),
            items = listOf(MealItemWithFood(sampleFood2, 100.0)) // 200 kcal
        )
        val meal3 = DetailedMeal(
            meal = Meal(id = 3, dateTime = date2.atTime(18, 0), mealType = MealType.DINNER),
            items = listOf(MealItemWithFood(sampleFood1, 200.0)) // 200 kcal
        )

        val kcalByDate = analyzer.aggregateTotalKcalByDate(listOf(meal1, meal2, meal3))

        assertEquals(300.0, kcalByDate[date1] ?: 0.0, 0.001)
        assertEquals(200.0, kcalByDate[date2] ?: 0.0, 0.001)
    }

    // 10. 최근 30개 날짜의 양쪽 경계와 다음 날 제외
    @Test
    fun test10_filterLast30Days_boundaryValues() {
        val now = LocalDateTime.of(2026, 11, 5, 12, 0)
        val meals = listOf(
            DetailedMeal(Meal(id = 1, dateTime = LocalDateTime.of(2026, 10, 6, 23, 59, 59))),
            DetailedMeal(Meal(id = 2, dateTime = LocalDateTime.of(2026, 10, 7, 0, 0))),
            DetailedMeal(Meal(id = 3, dateTime = LocalDateTime.of(2026, 11, 5, 23, 59, 59))),
            DetailedMeal(Meal(id = 4, dateTime = LocalDateTime.of(2026, 11, 6, 0, 0))),
        )

        val filtered = analyzer.filterLastOneMonth(meals, now)

        assertEquals(listOf(2L, 3L), filtered.map { it.meal.id })
    }

    // 11. 끼니 종류별 영양소 집계
    @Test
    fun test11_aggregateNutrientsByMealType() {
        val breakfast = DetailedMeal(
            meal = Meal(id = 1, dateTime = LocalDateTime.now(), mealType = MealType.BREAKFAST),
            items = listOf(MealItemWithFood(sampleFood1, 100.0)) // 100 kcal
        )
        val lunch = DetailedMeal(
            meal = Meal(id = 2, dateTime = LocalDateTime.now(), mealType = MealType.LUNCH),
            items = listOf(MealItemWithFood(sampleFood2, 100.0)) // 200 kcal
        )

        val result = analyzer.analyzeNutrientsByMealType(listOf(breakfast, lunch))

        assertEquals(CalculatedNutrients(100.0, 20.0, 3.0, 1.0), result.getValue(MealType.BREAKFAST))
        assertEquals(CalculatedNutrients(200.0, 10.0, 15.0, 5.0), result.getValue(MealType.LUNCH))
        assertEquals(CalculatedNutrients.ZERO, result.getValue(MealType.DINNER))
        assertEquals(CalculatedNutrients.ZERO, result.getValue(MealType.SNACK))
    }

    // 12. 기록이 없을 때 빈 결과 처리
    @Test
    fun test12_emptyRecords_returnsEmptyOrZeroResult() {
        val emptyList = emptyList<DetailedMeal>()

        val result = analyzer.analyzeLastOneMonth(emptyList)

        assertEquals(0.0, result.totalCalories, 0.001)
        assertEquals(0, result.totalMealCount)
        MealType.entries.forEach { type ->
            assertEquals(0.0, result.nutrientsByMealType[type]?.kcal ?: 0.0, 0.001)
            assertEquals(0, result.mealCountByMealType[type])
        }
    }
}
