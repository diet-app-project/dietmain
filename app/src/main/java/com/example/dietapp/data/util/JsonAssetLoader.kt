package com.example.dietapp.data.util

import android.content.Context
import com.example.dietapp.data.local.entity.FoodEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonAssetLoader(private val context: Context) {
    fun loadFoodsFromAsset(fileName: String = "foods.json"): List<FoodEntity> {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<FoodEntity>>() {}.type
            Gson().fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
