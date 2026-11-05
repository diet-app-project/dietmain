package com.example.dietapp.data.repository

import com.example.dietapp.data.local.dao.MealDao
import com.example.dietapp.data.local.entity.MealEntity
import com.example.dietapp.data.local.entity.MealItemEntity
import com.example.dietapp.data.model.DetailedMeal
import com.example.dietapp.data.model.Meal
import com.example.dietapp.data.model.MealItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MealRepository(
    private val mealDao: MealDao
) {
    fun getAllMealsWithItems(): Flow<List<DetailedMeal>> {
        return mealDao.getAllMealsWithItems().map { mealsWithItems ->
            mealsWithItems.map { it.toDetailedMeal() }
        }
    }

    suspend fun getMealById(mealId: Long): DetailedMeal? {
        return mealDao.getMealWithItemsById(mealId)?.toDetailedMeal()
    }

    suspend fun saveMeal(meal: Meal, items: List<MealItem>): Long {
        val mealEntity = MealEntity.fromDomain(meal)
        val itemEntities = items.map { MealItemEntity.fromDomain(it) }
        return mealDao.insertMealWithItems(mealEntity, itemEntities)
    }

    suspend fun deleteMeal(mealId: Long) {
        mealDao.deleteMealById(mealId)
    }
}
