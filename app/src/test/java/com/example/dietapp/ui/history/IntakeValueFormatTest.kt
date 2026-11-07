package com.example.dietapp.ui.history

import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.MealItemWithFood
import com.example.dietapp.domain.CalculatedNutrients
import com.example.dietapp.domain.NutritionCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class IntakeValueFormatTest {
    @Test
    fun displayAndNumericRoundingUseHalfUp() {
        assertEquals("2.3", formatIntakeValue(2.25))
        assertEquals("2", formatIntakeValue(2.04))
        assertEquals(2.3, CalculatedNutrients(2.25, 0.0, 0.0, 0.0).roundTo().kcal, 0.0)
    }

    @Test
    fun mealPreviewSumsRawValuesBeforeDisplayRounding() {
        val rice = Food(kcal = 166.0, carb = 37.33, protein = 3.36, fat = 0.32)
        val kimchi = Food(kcal = 38.0, carb = 6.49, protein = 1.98, fat = 0.43)
        val items = listOf(MealItemWithFood(rice, 150.5), MealItemWithFood(kimchi, 40.0))
        val total = NutritionCalculator().calculateTotalNutrients(items)

        assertEquals(5.8488, total.protein, 0.000001)
        assertEquals("265", formatIntakeValue(total.kcal))
        assertEquals("58.8", formatIntakeValue(total.carb))
        assertEquals("5.8", formatIntakeValue(total.protein))
        assertEquals("0.7", formatIntakeValue(total.fat))
    }

    @Test
    fun displayUsesDashForNonFiniteValues() {
        assertEquals("—", formatIntakeValue(Double.NaN))
        assertEquals("—", formatIntakeValue(Double.POSITIVE_INFINITY))
    }
}
