package com.example.dietapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dietapp.data.model.DetailedMeal
import com.example.dietapp.domain.NutritionCalculator
import java.time.format.DateTimeFormatter

@Composable
fun MealCard(
    detailedMeal: DetailedMeal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    calculator: NutritionCalculator = NutritionCalculator()
) {
    val totalNutrients = calculator.calculateTotalNutrients(detailedMeal.items).roundTo(1)
    val foodNamesSummary = detailedMeal.items.joinToString(", ") { "${it.food.name}(${it.grams.toInt()}g)" }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (detailedMeal.meal.photoPath != null) {
                AsyncImage(
                    model = detailedMeal.meal.photoPath,
                    contentDescription = "식사 사진",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = detailedMeal.meal.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    SuggestionChip(
                        onClick = { },
                        label = { Text(detailedMeal.meal.mealType.displayName) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (foodNamesSummary.isNotBlank()) foodNamesSummary else "등록된 음식 없음",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${totalNutrients.kcal} kcal (탄 ${totalNutrients.carb}g · 단 ${totalNutrients.protein}g · 지 ${totalNutrients.fat}g)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
