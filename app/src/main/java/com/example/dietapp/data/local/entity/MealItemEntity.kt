package com.example.dietapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.dietapp.data.model.MealItem

@Entity(
    tableName = "meal_items",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("mealId"), Index("foodId")]
)
data class MealItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mealId: Long,
    val foodId: Long,
    val grams: Double
) {
    fun toDomain(): MealItem = MealItem(
        id = id,
        mealId = mealId,
        foodId = foodId,
        grams = grams
    )

    companion object {
        fun fromDomain(item: MealItem): MealItemEntity = MealItemEntity(
            id = item.id,
            mealId = item.mealId,
            foodId = item.foodId,
            grams = item.grams
        )
    }
}
