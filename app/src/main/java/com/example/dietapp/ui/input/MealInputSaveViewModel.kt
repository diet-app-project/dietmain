package com.example.dietapp.ui.input

import android.app.Application
import android.net.Uri
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietapp.data.local.MealPhotoCopyException
import com.example.dietapp.data.local.saveWithMealPhoto
import com.example.dietapp.data.model.Meal
import com.example.dietapp.data.model.MealItem
import com.example.dietapp.data.model.MealItemWithFood
import com.example.dietapp.data.model.MealType
import com.example.dietapp.data.repository.MealRepository
import com.example.dietapp.ui.history.SeoulMealZone
import com.example.dietapp.ui.history.seoulDateAsUtcMillis
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class MealSaveRequest(
    val dateTimeMillis: Long,
    val mealType: MealType,
    val photoUri: Uri?,
    val foods: List<MealItemWithFood>,
)

internal data class MealSaveResult(
    val mealId: Long,
    val dateUtcMillis: Long,
)

internal sealed interface MealSaveState {
    data object Idle : MealSaveState
    data object Saving : MealSaveState
    data class Failed(val message: String) : MealSaveState
    data class Succeeded(val result: MealSaveResult) : MealSaveState

    val locksInput: Boolean
        get() = this is Saving || this is Succeeded
}

internal class MealInputSaveViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableState = MutableStateFlow<MealSaveState>(MealSaveState.Idle)
    val state = mutableState.asStateFlow()
    val isLocked: Boolean get() = mutableState.value.locksInput
    private var successConsumed = false

    @MainThread
    fun save(request: MealSaveRequest, repository: MealRepository) {
        if (isLocked) return
        val foods = request.foods
        if (!areMealFoodsValid(foods)) {
            mutableState.value = MealSaveState.Failed("선택한 음식과 섭취량을 확인해 주세요.")
            return
        }

        mutableState.value = MealSaveState.Saving
        viewModelScope.launch {
            try {
                val dateUtcMillis = seoulDateAsUtcMillis(request.dateTimeMillis)
                val seoulDateTime = Instant.ofEpochMilli(request.dateTimeMillis)
                    .atZone(SeoulMealZone)
                    .toLocalDateTime()
                val meal = Meal(dateTime = seoulDateTime, mealType = request.mealType)
                val items = foods.map { item ->
                    MealItem(foodId = item.food.id, grams = item.grams)
                }
                val photoUri = request.photoUri
                val mealId = if (photoUri == null) {
                    repository.saveMeal(meal, items)
                } else {
                    saveWithMealPhoto(getApplication<Application>(), photoUri) { photoPath ->
                        repository.saveMeal(meal.copy(photoPath = photoPath), items)
                    }
                }
                check(mealId > 0)
                mutableState.value = MealSaveState.Succeeded(
                    MealSaveResult(mealId = mealId, dateUtcMillis = dateUtcMillis),
                )
            } catch (cancelled: CancellationException) {
                mutableState.value = MealSaveState.Idle
                throw cancelled
            } catch (_: MealPhotoCopyException) {
                mutableState.value = MealSaveState.Failed(
                    "사진을 저장하지 못했어요. 사진을 다시 선택하거나 저장을 다시 시도해 주세요.",
                )
            } catch (_: Exception) {
                mutableState.value = MealSaveState.Failed(
                    "식사를 저장하지 못했어요. 다시 시도해 주세요.",
                )
            }
        }
    }

    @MainThread
    fun consumeSuccess(result: MealSaveResult): Boolean {
        if (successConsumed || mutableState.value != MealSaveState.Succeeded(result)) {
            return false
        }
        successConsumed = true
        return true
    }
}
