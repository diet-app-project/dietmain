package com.example.dietapp.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.dietapp.data.model.DetailedMeal
import com.example.dietapp.domain.NutritionCalculator
import com.example.dietapp.ui.components.NutritionSummaryBar
import java.time.format.DateTimeFormatter

@Composable
fun MealDetailDialog(
    detailedMeal: DetailedMeal,
    onDismiss: () -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
    calculator: NutritionCalculator = NutritionCalculator()
) {
    val totalNutrients = calculator.calculateTotalNutrients(detailedMeal.items)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header: DateTime and MealType
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = detailedMeal.meal.dateTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "끼니: ${detailedMeal.meal.mealType.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Photo if available
                if (detailedMeal.meal.photoPath != null) {
                    AsyncImage(
                        model = detailedMeal.meal.photoPath,
                        contentDescription = "식사 사진",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Food Items List
                Text(
                    text = "섭취 음식 항목",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detailedMeal.items) { item ->
                        val itemNutrients = calculator.calculateNutrients(item.food, item.grams).roundTo(1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.food.name,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${item.grams.toInt()}g | 탄:${itemNutrients.carb}g 단:${itemNutrients.protein}g 지:${itemNutrients.fat}g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${itemNutrients.kcal} kcal",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nutrition Summary
                NutritionSummaryBar(nutrients = totalNutrients)

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons: Delete & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { onDelete(detailedMeal.meal.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제")
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text("기록 삭제")
                    }

                    TextButton(onClick = onDismiss) {
                        Text("닫기")
                    }
                }
            }
        }
    }
}
