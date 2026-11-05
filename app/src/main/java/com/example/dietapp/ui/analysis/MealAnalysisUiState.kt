package com.example.dietapp.ui.analysis

import com.example.dietapp.data.model.MealType
import com.example.dietapp.domain.CalculatedNutrients
import com.example.dietapp.domain.MonthAnalysisResult

data class MealAnalysisUiState(
    val isLoading: Boolean = true,
    val monthAnalysisResult: MonthAnalysisResult = MonthAnalysisResult(
        totalCalories = 0.0,
        nutrientsByMealType = MealType.entries.associateWith { CalculatedNutrients.ZERO },
        mealCountByMealType = MealType.entries.associateWith { 0 },
        totalMealCount = 0
    )
)
