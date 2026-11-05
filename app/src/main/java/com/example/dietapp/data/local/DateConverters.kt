package com.example.dietapp.data.local

import androidx.room.TypeConverter
import com.example.dietapp.data.model.MealType
import java.time.LocalDateTime
import java.time.ZoneOffset

class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromMealType(type: MealType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toMealType(value: String?): MealType? {
        return value?.let { MealType.valueOf(it) }
    }
}
