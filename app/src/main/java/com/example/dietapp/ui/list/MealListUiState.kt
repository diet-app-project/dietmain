package com.example.dietapp.ui.list

import com.example.dietapp.data.model.DetailedMeal
import java.time.LocalDate

data class DailyMealSummary(
    val date: LocalDate,
    val totalCalories: Double,
    val mealCount: Int,
    val meals: List<DetailedMeal>
)

data class MealListUiState(
    val isLoading: Boolean = true,
    val dailySummaries: List<DailyMealSummary> = emptyList(),
    val totalMealCount: Int = 0,
    val selectedMealForDetail: DetailedMeal? = null,
    val userMessage: String? = null
)
