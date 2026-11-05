package com.example.dietapp.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.Meal
import com.example.dietapp.data.model.MealItem
import com.example.dietapp.data.model.MealItemWithFood
import com.example.dietapp.data.model.MealType
import com.example.dietapp.data.repository.FoodRepository
import com.example.dietapp.data.repository.MealRepository
import com.example.dietapp.domain.CalculatedNutrients
import com.example.dietapp.domain.NutritionCalculator
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(FlowPreview::class)
class MealEntryViewModel(
    private val foodRepository: FoodRepository,
    private val mealRepository: MealRepository,
    private val calculator: NutritionCalculator = NutritionCalculator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealEntryUiState())
    val uiState: StateFlow<MealEntryUiState> = _uiState.asStateFlow()

    private val _searchQueryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _searchQueryFlow
                .debounce(200)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchResults = emptyList()) }
                    } else {
                        foodRepository.searchFoods(query).collect { foods ->
                            _uiState.update { it.copy(searchResults = foods) }
                        }
                    }
                }
        }
    }

    fun onDateTimeChanged(newDateTime: LocalDateTime) {
        _uiState.update {
            it.copy(
                dateTime = newDateTime,
                mealType = MealType.recommendByTime(newDateTime.hour)
            )
        }
    }

    fun onMealTypeChanged(newMealType: MealType) {
        _uiState.update { it.copy(mealType = newMealType) }
    }

    fun onPhotoSelected(photoUri: String?) {
        _uiState.update { it.copy(photoUri = photoUri) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQueryFlow.value = query
    }

    fun addFoodToSelection(food: Food) {
        _uiState.update { state ->
            val updated = state.selectedFoods + SelectedFoodItem(food, 100.0)
            state.copy(
                selectedFoods = updated,
                searchQuery = "",
                searchResults = emptyList(),
                calculatedNutrients = recalculateNutrients(updated)
            )
        }
    }

    fun updateFoodGrams(index: Int, grams: Double) {
        if (grams < 0) return
        _uiState.update { state ->
            val updated = state.selectedFoods.toMutableList().apply {
                if (index in indices) {
                    this[index] = this[index].copy(grams = grams)
                }
            }
            state.copy(
                selectedFoods = updated,
                calculatedNutrients = recalculateNutrients(updated)
            )
        }
    }

    fun removeFoodFromSelection(index: Int) {
        _uiState.update { state ->
            val updated = state.selectedFoods.toMutableList().apply {
                if (index in indices) {
                    removeAt(index)
                }
            }
            state.copy(
                selectedFoods = updated,
                calculatedNutrients = recalculateNutrients(updated)
            )
        }
    }

    fun saveMeal() {
        val state = _uiState.value
        if (state.selectedFoods.isEmpty()) {
            _uiState.update { it.copy(userMessage = "하나 이상의 음식을 추가해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val meal = Meal(
                    dateTime = state.dateTime,
                    mealType = state.mealType,
                    photoPath = state.photoUri
                )
                val mealItems = state.selectedFoods.map {
                    MealItem(
                        foodId = it.food.id,
                        grams = it.grams
                    )
                }
                mealRepository.saveMeal(meal, mealItems)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSavedSuccess = true,
                        userMessage = "식사가 성공적으로 저장되었습니다."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        userMessage = "저장 중 오류가 발생했습니다: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun resetSavedState() {
        _uiState.update {
            MealEntryUiState()
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun recalculateNutrients(selectedFoods: List<SelectedFoodItem>): CalculatedNutrients {
        val items = selectedFoods.map { MealItemWithFood(it.food, it.grams) }
        return calculator.calculateTotalNutrients(items)
    }

    class Factory(
        private val foodRepository: FoodRepository,
        private val mealRepository: MealRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MealEntryViewModel(foodRepository, mealRepository) as T
        }
    }
}
