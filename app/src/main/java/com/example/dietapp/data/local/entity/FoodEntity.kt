package com.example.dietapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dietapp.data.model.Food

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dbName: String = "",
    val foodCode: String = "",
    val category: String = "",
    val basis: String = "100g",
    val kcal: Double,
    val carb: Double,
    val protein: Double,
    val fat: Double,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0,
    val dbWeight: Double = 100.0,
    val origin: String = ""
) {
    fun toDomain(): Food = Food(
        id = id,
        name = name,
        dbName = dbName,
        foodCode = foodCode,
        category = category,
        basis = basis,
        kcal = kcal,
        carb = carb,
        protein = protein,
        fat = fat,
        sugar = sugar,
        sodium = sodium,
        dbWeight = dbWeight,
        origin = origin
    )

    companion object {
        fun fromDomain(food: Food): FoodEntity = FoodEntity(
            id = food.id,
            name = food.name,
            dbName = food.dbName,
            foodCode = food.foodCode,
            category = food.category,
            basis = food.basis,
            kcal = food.kcal,
            carb = food.carb,
            protein = food.protein,
            fat = food.fat,
            sugar = food.sugar,
            sodium = food.sodium,
            dbWeight = food.dbWeight,
            origin = food.origin
        )
    }
}
