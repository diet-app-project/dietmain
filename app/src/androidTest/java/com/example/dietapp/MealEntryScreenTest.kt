package com.example.dietapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.dietapp.domain.CalculatedNutrients
import com.example.dietapp.ui.components.NutritionSummaryBar
import com.example.dietapp.ui.theme.DietAppTheme
import org.junit.Rule
import org.junit.Test

class MealEntryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNutritionSummaryBar_displaysCalculatedNutrients() {
        val nutrients = CalculatedNutrients(
            kcal = 350.0,
            carb = 40.0,
            protein = 36.0,
            fat = 5.0
        )

        composeTestRule.setContent {
            DietAppTheme {
                NutritionSummaryBar(nutrients = nutrients)
            }
        }

        composeTestRule.onNodeWithText("실시간 영양성분 미리보기").assertIsDisplayed()
        composeTestRule.onNodeWithText("350.0 kcal").assertIsDisplayed()
        composeTestRule.onNodeWithText("40.0 g").assertIsDisplayed()
        composeTestRule.onNodeWithText("36.0 g").assertIsDisplayed()
        composeTestRule.onNodeWithText("5.0 g").assertIsDisplayed()
    }
}
