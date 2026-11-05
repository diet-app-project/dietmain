package com.example.dietapp.data.model

data class MealItem(
    val id: Long = 0,
    val mealId: Long = 0,
    val foodId: Long,
    val grams: Double
)
