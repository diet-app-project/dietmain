package com.example.dietapp.ui.entry

import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.MealType
import com.example.dietapp.domain.CalculatedNutrients
import java.time.LocalDateTime

data class SelectedFoodItem(
    val food: Food,
    val grams: Double = 100.0
)

data class MealEntryUiState(
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val mealType: MealType = MealType.recommendByTime(LocalDateTime.now().hour),
    val photoUri: String? = null,
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val selectedFoods: List<SelectedFoodItem> = emptyList(),
    val calculatedNutrients: CalculatedNutrients = CalculatedNutrients.ZERO,
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val userMessage: String? = null
)
