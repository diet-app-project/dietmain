package com.example.dietapp.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dietapp.data.model.DetailedMeal
import com.example.dietapp.data.repository.MealRepository
import com.example.dietapp.domain.NutritionCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MealListViewModel(
    private val mealRepository: MealRepository,
    private val calculator: NutritionCalculator = NutritionCalculator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealListUiState())
    val uiState: StateFlow<MealListUiState> = _uiState.asStateFlow()

    init {
        loadMeals()
    }

    private fun loadMeals() {
        viewModelScope.launch {
            mealRepository.getAllMealsWithItems().collect { allMeals ->
                val groupedByDate = allMeals.groupBy { it.meal.dateTime.toLocalDate() }
                val summaries = groupedByDate.map { (date, mealsForDate) ->
                    val totalCalories = mealsForDate.sumOf { detailedMeal ->
                        calculator.calculateTotalNutrients(detailedMeal.items).kcal
                    }
                    DailyMealSummary(
                        date = date,
                        totalCalories = totalCalories,
                        mealCount = mealsForDate.size,
                        meals = mealsForDate.sortedByDescending { it.meal.dateTime }
                    )
                }.sortedByDescending { it.date }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dailySummaries = summaries,
                        totalMealCount = allMeals.size
                    )
                }
            }
        }
    }

    fun onMealClicked(meal: DetailedMeal) {
        _uiState.update { it.copy(selectedMealForDetail = meal) }
    }

    fun dismissDetailDialog() {
        _uiState.update { it.copy(selectedMealForDetail = null) }
    }

    fun deleteMeal(mealId: Long) {
        viewModelScope.launch {
            try {
                mealRepository.deleteMeal(mealId)
                _uiState.update {
                    it.copy(
                        selectedMealForDetail = null,
                        userMessage = "식사 기록이 삭제되었습니다."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userMessage = "삭제 실패: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    class Factory(
        private val mealRepository: MealRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MealListViewModel(mealRepository) as T
        }
    }
}
