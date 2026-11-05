package com.example.dietapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dietapp.data.model.Meal
import com.example.dietapp.data.model.MealType
import java.time.LocalDateTime

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateTime: LocalDateTime,
    val mealType: MealType,
    val photoPath: String? = null
) {
    fun toDomain(): Meal = Meal(
        id = id,
        dateTime = dateTime,
        mealType = mealType,
        photoPath = photoPath
    )

    companion object {
        fun fromDomain(meal: Meal): MealEntity = MealEntity(
            id = meal.id,
            dateTime = meal.dateTime,
            mealType = meal.mealType,
            photoPath = meal.photoPath
        )
    }
}
