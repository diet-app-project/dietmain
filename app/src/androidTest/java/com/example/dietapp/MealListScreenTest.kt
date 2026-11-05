package com.example.dietapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.dietapp.data.model.DetailedMeal
import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.Meal
import com.example.dietapp.data.model.MealItemWithFood
import com.example.dietapp.data.model.MealType
import com.example.dietapp.ui.components.MealCard
import com.example.dietapp.ui.theme.DietAppTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class MealListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMealCard_displaysMealDetailsCorrectly() {
        val sampleFood = Food(id = 1, name = "쌀밥", kcal = 166.0, carb = 37.33, protein = 3.36, fat = 0.32)
        val detailedMeal = DetailedMeal(
            meal = Meal(id = 1, dateTime = LocalDateTime.of(2026, 11, 5, 12, 30), mealType = MealType.LUNCH),
            items = listOf(MealItemWithFood(sampleFood, 200.0))
        )

        composeTestRule.setContent {
            DietAppTheme {
                MealCard(
                    detailedMeal = detailedMeal,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("12:30").assertIsDisplayed()
        composeTestRule.onNodeWithText("중식").assertIsDisplayed()
        composeTestRule.onNodeWithText("쌀밥(200g)").assertIsDisplayed()
    }
}
