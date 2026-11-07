package com.example.dietapp.ui.history

import com.example.dietapp.data.model.MealType
import com.example.dietapp.domain.MealDateRange
import com.example.dietapp.domain.mealDateInSeoul

internal data class MealHistorySummary(
    val totalKcal: Double,
    val mealCount: Int,
    val totalMacros: MealMacros,
    val macrosByMealType: Map<MealType, MealMacros>,
    val hasMissingNutrition: Boolean,
)

internal fun summarizeMealHistory(
    rows: List<MealHistoryRow>,
    range: MealDateRange? = null,
): MealHistorySummary {
    val meals = if (range == null) {
        rows
    } else {
        rows.filter { mealDateInSeoul(it.dateTimeMillis) in range }
    }
    val byType = meals.groupBy { it.mealType }
    return MealHistorySummary(
        totalKcal = meals.sumOf { it.totalKcal },
        mealCount = meals.size,
        totalMacros = sumMealMacros(meals.mapNotNull { it.nutrition?.totalMacros }),
        macrosByMealType = MealType.entries.associateWith { type ->
            sumMealMacros(byType[type].orEmpty().mapNotNull { it.nutrition?.totalMacros })
        },
        hasMissingNutrition = meals.any { it.nutrition == null },
    )
}

internal fun sumMealMacros(values: List<MealMacros>): MealMacros =
    MealMacros(
        carbsG = values.sumOf { it.carbsG },
        proteinG = values.sumOf { it.proteinG },
        fatG = values.sumOf { it.fatG },
    )
