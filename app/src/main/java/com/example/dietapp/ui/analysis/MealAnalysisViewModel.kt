package com.example.dietapp.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dietapp.data.repository.MealRepository
import com.example.dietapp.domain.MealAnalyzer
import com.example.dietapp.domain.MealTimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MealAnalysisViewModel(
    private val mealRepository: MealRepository,
    private val analyzer: MealAnalyzer = MealAnalyzer()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealAnalysisUiState())
    val uiState: StateFlow<MealAnalysisUiState> = _uiState.asStateFlow()

    init {
        loadAnalysis()
    }

    fun loadAnalysis(now: LocalDateTime = LocalDateTime.now(MealTimeZone)) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            mealRepository.getAllMealsWithItems().collect { allMeals ->
                val analysisResult = analyzer.analyzeLastOneMonth(allMeals, now)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        monthAnalysisResult = analysisResult
                    )
                }
            }
        }
    }

    class Factory(
        private val mealRepository: MealRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MealAnalysisViewModel(mealRepository) as T
        }
    }
}
