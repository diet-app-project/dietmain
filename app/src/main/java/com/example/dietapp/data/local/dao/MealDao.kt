package com.example.dietapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.dietapp.data.local.entity.MealEntity
import com.example.dietapp.data.local.entity.MealItemEntity
import com.example.dietapp.data.model.MealWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Transaction
    @Query("SELECT * FROM meals ORDER BY dateTime DESC")
    fun getAllMealsWithItems(): Flow<List<MealWithItems>>

    @Transaction
    @Query("SELECT * FROM meals WHERE id = :mealId LIMIT 1")
    suspend fun getMealWithItemsById(mealId: Long): MealWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealItems(items: List<MealItemEntity>)

    @Transaction
    suspend fun insertMealWithItems(meal: MealEntity, items: List<MealItemEntity>): Long {
        val mealId = insertMeal(meal)
        val itemsWithMealId = items.map { it.copy(mealId = mealId) }
        insertMealItems(itemsWithMealId)
        return mealId
    }

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMealById(mealId: Long)
}
