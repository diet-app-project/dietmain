package com.example.dietapp.ui.input

import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.MealItemWithFood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MealInputFoodValidationTest {
    private val rice = Food(id = 1, name = "쌀밥", foodCode = "RICE", basis = "100g")
    private val kimchi = Food(id = 2, name = "배추김치", foodCode = "KIMCHI", basis = "100g")
    private val catalog = listOf(rice, kimchi).associateBy { it.foodCode }

    @Test
    fun decimalGramsAreKeptForCalculationAndSave() {
        val selected = listOf(input(rice, " 150.5 "), input(kimchi, "40"))
        val valid = validMealInputsByCode(selected, catalog)

        assertEquals(150.5, valid.getValue(rice.foodCode).grams, 0.0)
        assertEquals(40.0, valid.getValue(kimchi.foodCode).grams, 0.0)
        assertTrue(areMealFoodsValid(valid.values, selected.size))
    }

    @Test
    fun blankZeroNegativeAndNonFiniteTextIsRejected() {
        val invalidInputs = listOf("", "  ", "0", "-10", "NaN", "Infinity", "1e309", "150g")
        for (text in invalidInputs) {
            assertNull(text, validFoodGrams(text))
        }
    }

    @Test
    fun emptyOrPartiallyValidMealCannotBeSaved() {
        val selected = listOf(input(rice, "150"), input(kimchi, "0"))
        val valid = validMealInputsByCode(selected, catalog)

        assertEquals(setOf(rice.foodCode), valid.keys)
        assertFalse(areMealFoodsValid(valid.values, selected.size))
        assertFalse(areMealFoodsValid(emptyList()))
    }

    @Test
    fun milliliterFoodIsRejectedForPreviewAndDirectSave() {
        val milk = Food(id = 3, name = "우유", foodCode = "MILK", basis = "100ml")
        val selected = listOf(input(milk, "100"))

        assertFalse(milk.supportsGramInput)
        assertTrue(validMealInputsByCode(selected, mapOf(milk.foodCode to milk)).isEmpty())
        assertFalse(areMealFoodsValid(listOf(MealItemWithFood(milk, 100.0))))
    }

    @Test
    fun duplicateSelectionsAndDuplicateRoomFoodIdsCannotBeSaved() {
        val repeated = listOf(input(rice, "150"), input(rice, "200"))
        val valid = validMealInputsByCode(repeated, catalog)
        val duplicateId = rice.copy(foodCode = "RICE_ALIAS")

        assertFalse(areMealFoodsValid(valid.values, repeated.size))
        assertFalse(areMealFoodsValid(listOf(MealItemWithFood(rice, 150.0), MealItemWithFood(duplicateId, 200.0))))
    }

    @Test
    fun directSaveRejectsInvalidGramsEvenWithoutTextParsing() {
        val invalidGrams = listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)
        for (grams in invalidGrams) {
            assertFalse(areMealFoodsValid(listOf(MealItemWithFood(rice, grams))))
        }
    }

    private fun input(food: Food, text: String): MealInputFoodAmount =
        MealInputFoodAmount(foodCode = food.foodCode, displayName = food.name, gramsText = text)
}
