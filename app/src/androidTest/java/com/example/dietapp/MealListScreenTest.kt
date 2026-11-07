package com.example.dietapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.dietapp.data.local.entity.FoodEntity
import com.example.dietapp.data.model.DetailedMeal
import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.Meal
import com.example.dietapp.data.model.MealItem
import com.example.dietapp.data.model.MealItemWithFood
import com.example.dietapp.data.model.MealType
import com.example.dietapp.ui.components.MealCard
import com.example.dietapp.ui.history.SeoulMealZone
import com.example.dietapp.ui.navigation.DietNavGraph
import com.example.dietapp.ui.theme.DietAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class MealListScreenTest {

    @get:Rule(order = 0)
    val databaseRule = MealUiDatabaseRule()

    @get:Rule(order = 1)
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

    @Test
    fun uiB02_clickingMeal42OpensItsOwnDetails() {
        val today = LocalDate.now(SeoulMealZone)
        runBlocking {
            databaseRule.database.foodDao().insertFoods(
                listOf(
                    FoodEntity(id = 1, name = "쌀밥", foodCode = "rice", kcal = 166.0, carb = 37.33, protein = 3.36, fat = 0.32),
                    FoodEntity(id = 2, name = "배추김치", foodCode = "kimchi", kcal = 38.0, carb = 6.49, protein = 1.98, fat = 0.43),
                ),
            )
            databaseRule.mealRepository.saveMeal(
                Meal(id = 42, dateTime = today.atTime(12, 0), mealType = MealType.LUNCH),
                listOf(MealItem(foodId = 1, grams = 150.5)),
            )
            databaseRule.mealRepository.saveMeal(
                Meal(id = 43, dateTime = today.atTime(18, 0), mealType = MealType.DINNER),
                listOf(MealItem(foodId = 2, grams = 100.0)),
            )
        }

        val catalogStatus = MutableStateFlow(FoodCatalogStatus.READY)
        composeTestRule.setContent {
            DietAppTheme {
                DietNavGraph(databaseRule.foodRepository, databaseRule.mealRepository, catalogStatus)
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText("2끼").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasTestTag("meal-row-42"))
        val row42 = composeTestRule.onNodeWithTag("meal-row-42").assertIsDisplayed()
        val row43 = composeTestRule.onNodeWithTag("meal-row-43").assertIsDisplayed()
        assertTrue(row43.getUnclippedBoundsInRoot().top < row42.getUnclippedBoundsInRoot().top)
        row42.performClick()

        composeTestRule.onNodeWithTag("meal-detail-42").assertIsDisplayed()
        composeTestRule.onNodeWithTag("meal-detail-43").assertDoesNotExist()
        val inDetail42 = hasAnyAncestor(hasTestTag("meal-detail-42"))
        composeTestRule.onNode(hasText("쌀밥") and inDetail42, useUnmergedTree = true)
            .performScrollTo().assertIsDisplayed()
        composeTestRule.onNode(hasText("150.5 g") and inDetail42, useUnmergedTree = true)
            .performScrollTo().assertIsDisplayed()
        // 한 끼 합계와 음식 행에 같은 열량이 표시된다.
        composeTestRule.onAllNodes(hasText("249.8 kcal") and inDetail42, useUnmergedTree = true)
            .onLast().performScrollTo().assertIsDisplayed()
    }
}