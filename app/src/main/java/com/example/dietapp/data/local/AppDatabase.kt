package com.example.dietapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dietapp.data.local.dao.FoodDao
import com.example.dietapp.data.local.dao.MealDao
import com.example.dietapp.data.local.entity.FoodEntity
import com.example.dietapp.data.local.entity.MealEntity
import com.example.dietapp.data.local.entity.MealItemEntity

@Database(
    entities = [FoodEntity::class, MealEntity::class, MealItemEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun mealDao(): MealDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diet_app.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
