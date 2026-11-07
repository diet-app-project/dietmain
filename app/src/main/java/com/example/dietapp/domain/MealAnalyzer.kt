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
     * 서울 기준일을 포함한 최근 30개 달력 날짜의 식사만 선택합니다.
     * Room에 저장된 dateTime과 전달받은 now는 서울 현지 시각입니다.
     */
    fun filterLastOneMonth(
        meals: List<DetailedMeal>,
        now: LocalDateTime = LocalDateTime.now(MealTimeZone)
    ): List<DetailedMeal> {
        val range = MealPeriod.LAST_30_DAYS.endingOn(now.toLocalDate())
        return meals.filter { it.meal.dateTime.toLocalDate() in range }
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
        now: LocalDateTime = LocalDateTime.now(MealTimeZone)
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
