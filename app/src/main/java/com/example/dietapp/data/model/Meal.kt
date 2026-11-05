package com.example.dietapp.data.model

import java.time.LocalDateTime

data class Meal(
    val id: Long = 0,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val mealType: MealType = MealType.LUNCH,
    val photoPath: String? = null
)
