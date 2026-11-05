package com.example.dietapp

import android.app.Application
import com.example.dietapp.data.local.AppDatabase
import com.example.dietapp.data.repository.FoodRepository
import com.example.dietapp.data.repository.MealRepository
import com.example.dietapp.data.util.JsonAssetLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DietApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val jsonAssetLoader by lazy { JsonAssetLoader(this) }
    val foodRepository by lazy { FoodRepository(database.foodDao(), jsonAssetLoader) }
    val mealRepository by lazy { MealRepository(database.mealDao()) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            foodRepository.populateInitialDataIfEmpty()
        }
    }
}
