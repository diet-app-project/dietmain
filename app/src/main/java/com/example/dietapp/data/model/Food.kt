package com.example.dietapp.data.model

data class Food(
    val id: Long = 0,
    val name: String = "",
    val dbName: String = "",
    val foodCode: String = "",
    val category: String = "",
    val basis: String = "100g",
    val kcal: Double = 0.0,
    val carb: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0,
    val dbWeight: Double = 100.0,
    val origin: String = ""
)
