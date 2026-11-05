package com.example.dietapp.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.dietapp.data.local.entity.FoodEntity
import com.example.dietapp.data.local.entity.MealEntity
import com.example.dietapp.data.local.entity.MealItemEntity

data class MealItemWithFood(
    val food: Food,
    val grams: Double
)

data class MealItemWithFoodEntity(
    @Embedded val mealItem: MealItemEntity,
    @Relation(
        parentColumn = "foodId",
        entityColumn = "id"
    )
    val food: FoodEntity
)

data class MealWithItems(
    @Embedded val meal: MealEntity,
    @Relation(
        entity = MealItemEntity::class,
        parentColumn = "id",
        entityColumn = "mealId"
    )
    val items: List<MealItemWithFoodEntity>
) {
    fun toDetailedMeal(): DetailedMeal {
        return DetailedMeal(
            meal = meal.toDomain(),
            items = items.map {
                MealItemWithFood(
                    food = it.food.toDomain(),
                    grams = it.mealItem.grams
                )
            }
        )
    }
}

data class DetailedMeal(
    val meal: Meal,
    val items: List<MealItemWithFood> = emptyList()
)
