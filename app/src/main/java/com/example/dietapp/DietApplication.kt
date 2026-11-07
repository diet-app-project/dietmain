package com.example.dietapp

import android.app.Application
import android.util.Log
import com.example.dietapp.data.local.AppDatabase
import com.example.dietapp.data.repository.FoodRepository
import com.example.dietapp.data.repository.MealRepository
import com.example.dietapp.data.util.JsonAssetLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal enum class FoodCatalogStatus { INITIALIZING, READY, ERROR }

class DietApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val jsonAssetLoader by lazy { JsonAssetLoader(this) }
    val foodRepository by lazy { FoodRepository(database.foodDao(), jsonAssetLoader) }
    val mealRepository by lazy { MealRepository(database.mealDao()) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableFoodCatalogStatus = MutableStateFlow(FoodCatalogStatus.INITIALIZING)
    internal val foodCatalogStatus = mutableFoodCatalogStatus.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            mutableFoodCatalogStatus.value = try {
                foodRepository.populateInitialDataIfEmpty()
                check(database.foodDao().getFoodCount() > 0) { "Food catalog is empty" }
                FoodCatalogStatus.READY
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Log.e("DietApplication", "Failed to initialize food catalog", failure)
                FoodCatalogStatus.ERROR
            }
        }
    }
}