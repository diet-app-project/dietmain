package com.example.dietapp.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dietapp.ui.components.DietCard
import com.example.dietapp.ui.components.MealFoodIcon
import com.example.dietapp.ui.components.MealGinghamDecoration
import com.example.dietapp.ui.components.MealScreenHeader

internal sealed interface MealDetailHeaderState {
    data object WaitingForIntegration : MealDetailHeaderState
    data object Loading : MealDetailHeaderState
    data object Error : MealDetailHeaderState
    data object NotFound : MealDetailHeaderState
    data class Ready(val meal: MealHistoryRow) : MealDetailHeaderState
}

internal fun MealHistoryListState.detailHeaderFor(mealId: Long): MealDetailHeaderState =
    when (this) {
        MealHistoryListState.WaitingForIntegration -> MealDetailHeaderState.WaitingForIntegration
        MealHistoryListState.Loading -> MealDetailHeaderState.Loading
        MealHistoryListState.Error -> MealDetailHeaderState.Error
        is MealHistoryListState.Ready -> rows.firstOrNull { it.id == mealId }
            ?.let { MealDetailHeaderState.Ready(it) }
            ?: MealDetailHeaderState.NotFound
    }

@Composable
internal fun MealDetailScreen(
    state: MealDetailHeaderState,
    onBack: () -> Unit,
) {
    val mealId = (state as? MealDetailHeaderState.Ready)?.meal?.id
    val detailTag = if (mealId == null) Modifier else Modifier.testTag("meal-detail-$mealId")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(detailTag)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MealScreenHeader(title = "식사 상세", onBack = onBack)

        when (state) {
            is MealDetailHeaderState.Ready -> {
                MealDetailHeader(meal = state.meal)
                MealDetailNutritionSection(meal = state.meal)
            }
            MealDetailHeaderState.WaitingForIntegration -> Text("식사 상세 연동 대기")
            MealDetailHeaderState.Loading -> Text("식사 기록을 불러오는 중…")
            MealDetailHeaderState.Error -> Text("식사 기록을 불러오지 못했어요.")
            MealDetailHeaderState.NotFound -> Text("식사 기록을 찾을 수 없어요")
        }
    }
}

@Composable
private fun MealDetailHeader(meal: MealHistoryRow) {
    val colors = MaterialTheme.colorScheme
    val painter = rememberMealHistoryPhotoPainter(photo = meal.photo, maxEdgePx = 1024)
    val dateTimeLabel = remember(meal.dateTimeMillis) {
        formatMealHistoryDateTime(meal.dateTimeMillis)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer,
    ) {
        if (painter == null) {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(min = 112.dp).padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_gallery),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Text(text = "사진 없음", style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Image(
                painter = painter,
                contentDescription = "식사 사진",
                modifier = Modifier.fillMaxWidth().aspectRatio(1.9f),
                contentScale = ContentScale.Crop,
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dateTimeLabel,
            modifier = Modifier.weight(1f),
            color = colors.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        MealHistoryBadge(mealType = meal.mealType)
    }
}

@Composable
private fun MealDetailNutritionSection(meal: MealHistoryRow) {
    val nutrition = meal.nutrition
    if (nutrition == null) {
        Text("영양 정보 연동 대기")
        return
    }

    MealDetailTotalCard(totalKcal = meal.totalKcal, macros = nutrition.totalMacros)
    Text(
        text = "음식별 영양",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
    if (nutrition.foods.isEmpty()) {
        Text("기록된 음식이 없어요")
    }
    nutrition.foods.forEach { food ->
        MealFoodNutritionCard(food = food)
    }
}

@Composable
private fun MealDetailTotalCard(totalKcal: Double, macros: MealMacros) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val energy: @Composable (Modifier) -> Unit = { modifier ->
        Text(
            text = "${formatIntakeValue(totalKcal)} kcal",
            modifier = modifier,
            color = colors.primary,
            style = typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        DietCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 11.dp)
                .shadow(2.dp, MaterialTheme.shapes.large),
            containerColor = colors.surfaceContainerLow,
        ) {
            Box(modifier = Modifier.padding(end = 82.dp)) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = colors.secondaryContainer,
                    contentColor = colors.primary,
                ) {
                    Text(
                        text = "한 끼 합계",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                val stacked = maxWidth < 296.dp || LocalDensity.current.fontScale >= 1.35f
                if (stacked) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        energy(Modifier)
                        HorizontalDivider(color = colors.outlineVariant)
                        MealDetailMacroSummary(macros)
                    }
                } else {
                    val maxEnergyWidth = maxWidth * 0.55f
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        energy(Modifier.widthIn(max = maxEnergyWidth))
                        VerticalDivider(
                            modifier = Modifier.fillMaxHeight().padding(vertical = 3.dp),
                            color = colors.outlineVariant,
                        )
                        MealDetailMacroSummary(macros, Modifier.weight(1f))
                    }
                }
            }
        }
        MealGinghamDecoration(Modifier.align(Alignment.TopEnd).padding(end = 8.dp))
    }
}

@Composable
private fun MealDetailMacroSummary(macros: MealMacros, modifier: Modifier = Modifier) {
    val carbs = formatIntakeValue(macros.carbsG)
    val protein = formatIntakeValue(macros.proteinG)
    val fat = formatIntakeValue(macros.fatG)
    Text(
        text = "탄 $carbs · 단 $protein · 지 $fat g",
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "탄수화물 $carbs g, 단백질 $protein g, 지방 $fat g"
        },
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealFoodNutritionCard(food: MealFoodNutrition) {
    val colors = MaterialTheme.colorScheme

    DietCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            MealFoodIcon(foodName = food.name)

            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f).widthIn(min = 112.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        itemVerticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${formatIntakeValue(food.amountG)} g",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = "탄수화물 ${formatIntakeValue(food.macros.carbsG)} g · " +
                                "단백질 ${formatIntakeValue(food.macros.proteinG)} g · " +
                                "지방 ${formatIntakeValue(food.macros.fatG)} g",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = colors.primaryContainer,
                    contentColor = colors.primary,
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.13f)),
                ) {
                    Text(
                        text = "${formatIntakeValue(food.kcal)} kcal",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
