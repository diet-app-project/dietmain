package com.example.dietapp

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.dietapp.data.local.AppDatabase
import com.example.dietapp.data.local.entity.FoodEntity
import com.example.dietapp.data.repository.FoodRepository
import com.example.dietapp.data.repository.MealRepository
import com.example.dietapp.data.util.JsonAssetLoader
import com.example.dietapp.domain.CalculatedNutrients
import com.example.dietapp.ui.components.NutritionSummaryBar
import com.example.dietapp.ui.navigation.DietNavGraph
import com.example.dietapp.ui.theme.DietAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource

class MealEntryScreenTest {

    @get:Rule(order = 0)
    val databaseRule = MealUiDatabaseRule()

    @get:Rule(order = 1)
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

    @Test
    fun uiB01_changingGramsUpdatesFoodAndMealNutrition() {
        runBlocking {
            databaseRule.database.foodDao().insertFoods(
                listOf(
                    FoodEntity(id = 1, name = "F", foodCode = "F", kcal = 120.0, carb = 20.0, protein = 5.0, fat = 2.0),
                    FoodEntity(id = 2, name = "G", foodCode = "G", kcal = 80.0, carb = 10.0, protein = 2.0, fat = 2.0),
                ),
            )
        }

        val catalogStatus = MutableStateFlow(FoodCatalogStatus.READY)
        composeTestRule.setContent {
            DietAppTheme {
                DietNavGraph(databaseRule.foodRepository, databaseRule.mealRepository, catalogStatus)
            }
        }

        val breakfastButton = hasText("조식") and hasClickAction()
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToNode(breakfastButton)
        composeTestRule.onNode(breakfastButton).performClick()

        val foodF = hasText("F") and hasClickAction()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodes(foodF).fetchSemanticsNodes().isNotEmpty()
        }
        // 검색 결과 목록을 감싼 입력 화면부터 스크롤한다.
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollTo()
        composeTestRule.onNode(foodF).performClick()
        composeTestRule.onNode(hasText("G") and hasClickAction()).performClick()

        setGrams("F", "100")
        setGrams("G", "50")
        assertNutrition("food-nutrition-F", "120 kcal")
        assertNutrition("nutrition-preview", "160 kcal")

        setGrams("F", "180")
        assertNutrition("food-nutrition-F", "216 kcal")
        assertNutrition("nutrition-preview", "256 kcal")
        assertNutrition("nutrition-preview", "탄수화물 41 g · 단백질 10 g · 지방 4.6 g")
        composeTestRule.onNodeWithTag("food-grams-G")
            .performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("50")))
    }

    private fun setGrams(foodCode: String, grams: String) {
        composeTestRule.onNodeWithTag("food-grams-$foodCode")
            .performScrollTo()
            .performTextReplacement(grams)
    }

    private fun assertNutrition(sectionTag: String, text: String) {
        composeTestRule.onNode(
            hasText(text) and hasAnyAncestor(hasTestTag(sectionTag)),
            useUnmergedTree = true,
        ).performScrollTo().assertIsDisplayed()
    }
}

class MealUiDatabaseRule : ExternalResource() {
    lateinit var database: AppDatabase
        private set
    lateinit var foodRepository: FoodRepository
        private set
    lateinit var mealRepository: MealRepository
        private set

    override fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        foodRepository = FoodRepository(database.foodDao(), JsonAssetLoader(context))
        mealRepository = MealRepository(database.mealDao())
    }

    override fun after() {
        database.close()
    }
}