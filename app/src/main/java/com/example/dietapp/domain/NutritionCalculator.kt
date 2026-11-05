package com.example.dietapp.domain

import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.MealItemWithFood
import kotlin.math.pow
import kotlin.math.round

data class CalculatedNutrients(
    val kcal: Double,
    val carb: Double,
    val protein: Double,
    val fat: Double,
) {
    operator fun plus(other: CalculatedNutrients): CalculatedNutrients {
        return CalculatedNutrients(
            kcal = this.kcal + other.kcal,
            carb = this.carb + other.carb,
            protein = this.protein + other.protein,
            fat = this.fat + other.fat,
        )
    }

    fun roundTo(decimals: Int = 1): CalculatedNutrients {
        val factor = 10.0.pow(decimals)
        return CalculatedNutrients(
            kcal = round(kcal * factor) / factor,
            carb = round(carb * factor) / factor,
            protein = round(protein * factor) / factor,
            fat = round(fat * factor) / factor,
        )
    }

    companion object {
        val ZERO = CalculatedNutrients(0.0, 0.0, 0.0, 0.0)
    }
}

class NutritionCalculator {

    /**
     * 100g 기준 영양성분을 바탕으로 섭취량(grams)에 따른 영양성분을 계산합니다.
     * @throws IllegalArgumentException grams가 음수인 경우 예외 발생
     */
    fun calculateNutrients(food: Food, grams: Double): CalculatedNutrients {
        require(grams >= 0.0) { "섭취량(grams)은 0 이상이어야 합니다." }
        if (grams == 0.0) return CalculatedNutrients.ZERO
        val ratio = grams / 100.0
        return CalculatedNutrients(
            kcal = food.kcal * ratio,
            carb = food.carb * ratio,
            protein = food.protein * ratio,
            fat = food.fat * ratio,
        )
    }

    fun calculateKcal(food: Food, grams: Double): Double {
        require(grams >= 0.0) { "섭취량(grams)은 0 이상이어야 합니다." }
        if (grams == 0.0) return 0.0
        return food.kcal * (grams / 100.0)
    }

    fun calculateCarb(food: Food, grams: Double): Double {
        require(grams >= 0.0) { "섭취량(grams)은 0 이상이어야 합니다." }
        if (grams == 0.0) return 0.0
        return food.carb * (grams / 100.0)
    }

    fun calculateProtein(food: Food, grams: Double): Double {
        require(grams >= 0.0) { "섭취량(grams)은 0 이상이어야 합니다." }
        if (grams == 0.0) return 0.0
        return food.protein * (grams / 100.0)
    }

    fun calculateFat(food: Food, grams: Double): Double {
        require(grams >= 0.0) { "섭취량(grams)은 0 이상이어야 합니다." }
        if (grams == 0.0) return 0.0
        return food.fat * (grams / 100.0)
    }

    /**
     * 여러 음식 항목의 총 영양성분 합계를 계산합니다.
     */
    fun calculateTotalNutrients(items: List<MealItemWithFood>): CalculatedNutrients {
        return items.fold(CalculatedNutrients.ZERO) { acc, item ->
            acc + calculateNutrients(item.food, item.grams)
        }
    }

    /**
     * (Food, grams) Pair 목록의 총 영양성분 합계를 계산합니다.
     */
    fun calculateTotalFromPairs(items: List<Pair<Food, Double>>): CalculatedNutrients {
        return items.fold(CalculatedNutrients.ZERO) { acc, (food, grams) ->
            acc + calculateNutrients(food, grams)
        }
    }
}
