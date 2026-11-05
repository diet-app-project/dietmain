package com.example.dietapp.domain

import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.MealItemWithFood
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NutritionCalculatorTest {

    private lateinit var calculator: NutritionCalculator
    private lateinit var sampleFood: Food

    @Before
    fun setUp() {
        calculator = NutritionCalculator()
        sampleFood = Food(
            id = 1,
            name = "쌀밥",
            kcal = 166.0,
            carb = 37.33,
            protein = 3.36,
            fat = 0.32
        )
    }

    // 1. 100g 섭취 시 성분값이 DB 값과 같다
    @Test
    fun test1_intake100g_matchesDbValue() {
        val nutrients = calculator.calculateNutrients(sampleFood, 100.0)

        assertEquals(166.0, nutrients.kcal, 0.001)
        assertEquals(37.33, nutrients.carb, 0.001)
        assertEquals(3.36, nutrients.protein, 0.001)
        assertEquals(0.32, nutrients.fat, 0.001)
    }

    // 2. 250g 섭취 시 2.5배가 된다
    @Test
    fun test2_intake250g_is2Point5TimesDbValue() {
        val nutrients = calculator.calculateNutrients(sampleFood, 250.0)

        assertEquals(166.0 * 2.5, nutrients.kcal, 0.001)
        assertEquals(37.33 * 2.5, nutrients.carb, 0.001)
        assertEquals(3.36 * 2.5, nutrients.protein, 0.001)
        assertEquals(0.32 * 2.5, nutrients.fat, 0.001)
    }

    // 3. 0g이면 모두 0이다
    @Test
    fun test3_intake0g_returnsZeroNutrients() {
        val nutrients = calculator.calculateNutrients(sampleFood, 0.0)

        assertEquals(0.0, nutrients.kcal, 0.001)
        assertEquals(0.0, nutrients.carb, 0.001)
        assertEquals(0.0, nutrients.protein, 0.001)
        assertEquals(0.0, nutrients.fat, 0.001)
        assertEquals(CalculatedNutrients.ZERO, nutrients)
    }

    // 4. 음수 g 입력은 거부(예외)한다
    @Test(expected = IllegalArgumentException::class)
    fun test4_negativeGrams_throwsIllegalArgumentException() {
        calculator.calculateNutrients(sampleFood, -50.0)
    }

    // 5. 여러 음식의 합계가 맞다
    @Test
    fun test5_multipleFoods_sumIsCorrect() {
        val food1 = Food(id = 1, name = "쌀밥", kcal = 100.0, carb = 20.0, protein = 3.0, fat = 1.0)
        val food2 = Food(id = 2, name = "닭가슴살", kcal = 150.0, carb = 0.0, protein = 30.0, fat = 3.0)

        val items = listOf(
            MealItemWithFood(food1, 200.0), // 100 * 2 = 200 kcal
            MealItemWithFood(food2, 100.0)  // 150 * 1 = 150 kcal
        )

        val total = calculator.calculateTotalNutrients(items)

        assertEquals(350.0, total.kcal, 0.001)
        assertEquals(40.0, total.carb, 0.001)
        assertEquals(36.0, total.protein, 0.001)
        assertEquals(5.0, total.fat, 0.001)
    }

    // 6. 소수점 반올림 규칙이 맞다
    @Test
    fun test6_decimalRoundingRule_isCorrect() {
        val rawNutrients = CalculatedNutrients(
            kcal = 166.6666,
            carb = 37.3333,
            protein = 3.355,
            fat = 0.324
        )

        val rounded1Digit = rawNutrients.roundTo(1)
        assertEquals(166.7, rounded1Digit.kcal, 0.001)
        assertEquals(37.3, rounded1Digit.carb, 0.001)
        assertEquals(3.4, rounded1Digit.protein, 0.001)
        assertEquals(0.3, rounded1Digit.fat, 0.001)

        val rounded2Digits = rawNutrients.roundTo(2)
        assertEquals(166.67, rounded2Digits.kcal, 0.001)
        assertEquals(37.33, rounded2Digits.carb, 0.001)
        assertEquals(3.36, rounded2Digits.protein, 0.001)
        assertEquals(0.32, rounded2Digits.fat, 0.001)
    }
}
