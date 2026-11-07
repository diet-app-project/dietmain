package com.example.dietapp.ui.input

import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.MealItemWithFood

internal data class MealInputFoodAmount(
    val foodCode: String,
    val displayName: String,
    val gramsText: String = "",
)

internal val Food.supportsGramInput: Boolean
    get() = id > 0 && foodCode.isNotBlank() && basis == "100g"

internal fun validFoodGrams(text: String): Double? =
    text.trim().toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }

internal fun validMealInputsByCode(
    selectedFoods: List<MealInputFoodAmount>,
    foodsByCode: Map<String, Food>,
): Map<String, MealItemWithFood> = buildMap {
    for (selectedFood in selectedFoods) {
        val food = foodsByCode[selectedFood.foodCode] ?: continue
        val grams = validFoodGrams(selectedFood.gramsText) ?: continue
        if (food.supportsGramInput && food.foodCode == selectedFood.foodCode) {
            put(selectedFood.foodCode, MealItemWithFood(food = food, grams = grams))
        }
    }
}

internal fun areMealFoodsValid(
    foods: Collection<MealItemWithFood>,
    expectedCount: Int = foods.size,
): Boolean =
    foods.isNotEmpty() &&
        foods.size == expectedCount &&
        foods.all { it.food.supportsGramInput && it.grams.isFinite() && it.grams > 0.0 } &&
        foods.distinctBy { it.food.id }.size == foods.size
