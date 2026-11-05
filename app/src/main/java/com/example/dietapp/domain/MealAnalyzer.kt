package com.example.dietapp.domain

import com.example.dietapp.data.model.DetailedMeal
import com.example.dietapp.data.model.MealType
import java.time.LocalDate
import java.time.LocalDateTime

data class MonthAnalysisResult(
    val totalCalories: Double,
    val nutrientsByMealType: Map<MealType, CalculatedNutrients>,
    val mealCountByMealType: Map<MealType, Int>,
    val totalMealCount: Int
)

class MealAnalyzer(
    private val calculator: NutritionCalculator = NutritionCalculator()
) {

    /**
     * 기준일(now)로부터 최근 1달(30일 전 00:00:00 ~ now) 이내의 식사 목록만 필터링합니다.
     * 30일 전 포함, 31일 전 제외.
     */
    fun filterLastOneMonth(
        meals: List<DetailedMeal>,
        now: LocalDateTime = LocalDateTime.now()
    ): List<DetailedMeal> {
        val thirtyDaysAgoStart = now.minusDays(30).toLocalDate().atStartOfDay()
        return meals.filter { meal ->
            !meal.meal.dateTime.isBefore(thirtyDaysAgoStart) && !meal.meal.dateTime.isAfter(now)
        }
    }

    /**
     * 전달된 식사 목록의 총 칼로리 합계를 계산합니다.
     */
    fun calculateTotalCalories(meals: List<DetailedMeal>): Double {
        return meals.sumOf { detailedMeal ->
            calculator.calculateTotalNutrients(detailedMeal.items).kcal
        }
    }

    /**
     * 끼니 종류(조식, 중식, 석식, 간식/음료)별 영양성분 합계를 계산합니다.
     */
    fun analyzeNutrientsByMealType(meals: List<DetailedMeal>): Map<MealType, CalculatedNutrients> {
        val initialMap = MealType.entries.associateWith { CalculatedNutrients.ZERO }.toMutableMap()
        
        for (meal in meals) {
            val mealNutrients = calculator.calculateTotalNutrients(meal.items)
            val current = initialMap[meal.meal.mealType] ?: CalculatedNutrients.ZERO
            initialMap[meal.meal.mealType] = current + mealNutrients
        }

        return initialMap
    }

    /**
     * 식사 목록을 날짜별(LocalDate) 끼니 수로 집계합니다.
     */
    fun aggregateMealCountByDate(meals: List<DetailedMeal>): Map<LocalDate, Int> {
        return meals.groupBy { it.meal.dateTime.toLocalDate() }
            .mapValues { (_, mealList) -> mealList.size }
    }

    /**
     * 식사 목록을 날짜별(LocalDate) 총 칼로리 합계로 집계합니다.
     */
    fun aggregateTotalKcalByDate(meals: List<DetailedMeal>): Map<LocalDate, Double> {
        return meals.groupBy { it.meal.dateTime.toLocalDate() }
            .mapValues { (_, mealList) -> calculateTotalCalories(mealList) }
    }

    /**
     * 최근 1달간 식사를 분석하여 총 칼로리 및 끼니별 영양성분/식사 수를 반환합니다.
     */
    fun analyzeLastOneMonth(
        meals: List<DetailedMeal>,
        now: LocalDateTime = LocalDateTime.now()
    ): MonthAnalysisResult {
        val lastMonthMeals = filterLastOneMonth(meals, now)
        val totalCalories = calculateTotalCalories(lastMonthMeals)
        val nutrientsByMealType = analyzeNutrientsByMealType(lastMonthMeals)

        val countMap = MealType.entries.associateWith { 0 }.toMutableMap()
        for (meal in lastMonthMeals) {
            countMap[meal.meal.mealType] = (countMap[meal.meal.mealType] ?: 0) + 1
        }

        return MonthAnalysisResult(
            totalCalories = totalCalories,
            nutrientsByMealType = nutrientsByMealType,
            mealCountByMealType = countMap,
            totalMealCount = lastMonthMeals.size
        )
    }
}
