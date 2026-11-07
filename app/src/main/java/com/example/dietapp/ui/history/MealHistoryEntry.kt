package com.example.dietapp.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.dietapp.data.model.DetailedMeal
import com.example.dietapp.data.model.MealType
import com.example.dietapp.data.repository.MealRepository
import com.example.dietapp.domain.MealTimeZone
import com.example.dietapp.domain.NutritionCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal val SeoulMealZone = MealTimeZone

private val LoadingHistory = MealHistoryData(
    historyState = MealHistoryListState.Loading,
    intakeForDate = { DailyIntakeCardState.Loading },
)

@Composable
internal fun rememberMealHistoryData(repository: MealRepository): MealHistoryData {
    val dataFlow = remember(repository) {
        repository.getAllMealsWithItems()
            .map(::historyDataForMeals)
            .catch {
                emit(
                    MealHistoryData(
                        historyState = MealHistoryListState.Error,
                        intakeForDate = { DailyIntakeCardState.Error },
                    ),
                )
            }
            .flowOn(Dispatchers.Default)
    }
    val historyData by dataFlow.collectAsState(initial = LoadingHistory)
    return historyData
}

private fun historyDataForMeals(meals: List<DetailedMeal>): MealHistoryData {
    val calculator = NutritionCalculator()
    val rows = meals.map { detail ->
        val foods = detail.items.map { item ->
            val values = calculator.calculateNutrients(item.food, item.grams)
            MealFoodNutrition(
                name = item.food.name,
                amountG = item.grams,
                kcal = values.kcal,
                macros = MealMacros(carbsG = values.carb, proteinG = values.protein, fatG = values.fat),
            )
        }
        MealHistoryRow(
            id = detail.meal.id,
            dateTimeMillis = detail.meal.dateTime.atZone(SeoulMealZone).toInstant().toEpochMilli(),
            mealType = detail.meal.mealType,
            totalKcal = foods.sumOf { it.kcal },
            photo = detail.meal.photoPath?.takeIf { it.isNotBlank() }
                ?.let { MealHistoryPhoto.Saved(it) },
            nutrition = MealDetailNutrition(
                totalMacros = sumMealMacros(foods.map { it.macros }),
                foods = foods,
            ),
        )
    }
    val intakeByDate = rows.groupBy { seoulDateAsUtcMillis(it.dateTimeMillis) }
        .mapValues { (_, mealsOnDate) ->
            val summary = summarizeMealHistory(mealsOnDate)
            DailyIntakeCardState.Ready(
                totalKcal = summary.totalKcal,
                mealCount = summary.mealCount,
                carbsG = summary.totalMacros.carbsG,
                proteinG = summary.totalMacros.proteinG,
                fatG = summary.totalMacros.fatG,
            )
        }
    return MealHistoryData(
        historyState = MealHistoryListState.Ready(rows),
        intakeForDate = { date -> intakeByDate[date] ?: DailyIntakeCardState.Empty },
    )
}

@Composable
internal fun MealHistoryEntry(
    historyData: MealHistoryData,
    isRecordActive: Boolean,
    onMealClick: (Long) -> Unit,
    onAddMealClick: (MealType, Long) -> Unit,
    savedDateUtcMillis: Long?,
    onSavedDateHandled: () -> Unit,
) {
    MealListRoute(
        onMealClick = onMealClick,
        onAddMealClick = onAddMealClick,
        isRecordActive = isRecordActive,
        intakeForDate = historyData.intakeForDate,
        historyForDate = { historyData.historyState },
        savedDateUtcMillis = savedDateUtcMillis,
        onSavedDateHandled = onSavedDateHandled,
    )
}