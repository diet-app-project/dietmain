package com.example.dietapp.data.repository

import com.example.dietapp.data.local.dao.FoodDao
import com.example.dietapp.data.model.Food
import com.example.dietapp.data.util.JsonAssetLoader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FoodRepository(
    private val foodDao: FoodDao,
    private val jsonAssetLoader: JsonAssetLoader
) {
    /**
     * 최초 실행 시 DB에 음식 데이터가 없으면 foods.json에서 읽어 초기화합니다.
     */
    suspend fun populateInitialDataIfEmpty() {
        if (foodDao.getFoodCount() == 0) {
            val foods = jsonAssetLoader.loadFoodsFromAsset("foods.json")
            if (foods.isNotEmpty()) {
                foodDao.insertFoods(foods)
            }
        }
    }

    fun getAllFoods(): Flow<List<Food>> {
        return foodDao.getAllFoods().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun searchFoods(query: String): Flow<List<Food>> {
        return foodDao.searchFoods(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getFoodById(id: Long): Food? {
        return foodDao.getFoodById(id)?.toDomain()
    }
}
