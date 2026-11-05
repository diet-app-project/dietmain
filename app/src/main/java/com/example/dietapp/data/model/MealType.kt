package com.example.dietapp.data.model

import java.time.LocalTime

enum class MealType(val displayName: String) {
    BREAKFAST("조식"),
    LUNCH("중식"),
    DINNER("석식"),
    SNACK("간식/음료");

    companion object {
        fun fromDisplayName(name: String): MealType {
            return entries.find { it.displayName == name } ?: SNACK
        }

        /**
         * 시간(hour: 0~23)에 따라 적절한 끼니 종류를 추천합니다.
         * - 05:00 ~ 10:59 -> BREAKFAST (조식)
         * - 11:00 ~ 15:59 -> LUNCH (중식)
         * - 16:00 ~ 21:59 -> DINNER (석식)
         * - 기타 (22:00 ~ 04:59) -> SNACK (간식/음료)
         */
        fun recommendByTime(hour: Int): MealType {
            return when (hour) {
                in 5..10 -> BREAKFAST
                in 11..15 -> LUNCH
                in 16..21 -> DINNER
                else -> SNACK
            }
        }

        fun recommendByTime(time: LocalTime): MealType {
            return recommendByTime(time.hour)
        }
    }
}
