package com.example.dietapp.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dietapp.R
import com.example.dietapp.domain.roundNutritionValue
import com.example.dietapp.ui.components.DietCard

internal sealed interface DailyIntakeCardState {
    data object WaitingForIntegration : DailyIntakeCardState
    data object Loading : DailyIntakeCardState
    data object Empty : DailyIntakeCardState
    data object Error : DailyIntakeCardState

    data class Ready(
        val totalKcal: Double,
        val mealCount: Int,
        val carbsG: Double,
        val proteinG: Double,
        val fatG: Double,
    ) : DailyIntakeCardState
}

private val EmptyIntakeValues = DailyIntakeCardState.Ready(
    totalKcal = 0.0,
    mealCount = 0,
    carbsG = 0.0,
    proteinG = 0.0,
    fatG = 0.0,
)

private val IntakeCloudShape = GenericShape { size, _ ->
    val edge = minOf(size.width * 0.035f, size.height * 0.11f)
    val right = size.width - edge
    val bottom = size.height - edge
    val scallops = 9
    val step = (right - edge) / scallops

    moveTo(edge, edge)
    repeat(scallops) { index ->
        val x = edge + index * step
        quadraticTo(x + step / 2, -edge * 0.55f, x + step, edge)
    }
    cubicTo(size.width, edge, size.width, bottom, right, bottom)
    repeat(scallops) { index ->
        val x = right - index * step
        quadraticTo(x - step / 2, size.height + edge * 0.55f, x - step, bottom)
    }
    cubicTo(0f, bottom, 0f, edge, edge, edge)
    close()
}

@Composable
internal fun DailyIntakeCard(state: DailyIntakeCardState) {
    val colors = MaterialTheme.colorScheme
    val values = when (state) {
        is DailyIntakeCardState.Ready -> state
        DailyIntakeCardState.Empty -> EmptyIntakeValues
        else -> null
    }
    val statusMessage = when (state) {
        is DailyIntakeCardState.Ready -> null
        DailyIntakeCardState.Empty -> "기록 없음"
        DailyIntakeCardState.WaitingForIntegration -> "데이터 연동 대기"
        DailyIntakeCardState.Loading -> "불러오는 중…"
        DailyIntakeCardState.Error -> "요약을 불러오지 못했어요."
    }
    val largeText = LocalDensity.current.fontScale >= 1.5f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = IntakeCloudShape,
            color = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colors.surface.copy(alpha = 0.24f),
                                colors.secondaryContainer,
                                colors.primaryContainer,
                            ),
                        ),
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1.4f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "하루 섭취",
                        color = colors.primary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    if (values != null) DailyIntakeEnergy(values)
                    if (statusMessage != null) {
                        Text(statusMessage, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Image(
                    painter = painterResource(R.drawable.bori_lunchbox),
                    contentDescription = null,
                    modifier = Modifier.weight(if (largeText) 0.65f else 1f).aspectRatio(1.5f),
                )
            }
        }

        if (values != null) DailyIntakeNutrients(values)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyIntakeEnergy(values: DailyIntakeCardState.Ready) {
    val colors = MaterialTheme.colorScheme

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${formatIntakeValue(values.totalKcal)} kcal",
            color = colors.primary,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            shape = CircleShape,
            color = colors.surface,
            contentColor = colors.primary,
        ) {
            Text(
                text = "${values.mealCount}끼",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DailyIntakeNutrients(values: DailyIntakeCardState.Ready) {
    val colors = MaterialTheme.colorScheme
    val nutrients = listOf(
        "탄수화물" to values.carbsG,
        "단백질" to values.proteinG,
        "지방" to values.fatG,
    )

    DietCard(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, MaterialTheme.shapes.large),
        containerColor = colors.surfaceContainerLow,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val stacked = maxWidth < 210.dp || LocalDensity.current.fontScale >= 1.5f
            if (stacked) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    nutrients.forEachIndexed { index, (label, grams) ->
                        if (index > 0) HorizontalDivider(color = colors.outlineVariant)
                        NutrientValue(label, grams, Modifier.fillMaxWidth())
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    nutrients.forEachIndexed { index, (label, grams) ->
                        if (index > 0) {
                            VerticalDivider(
                                modifier = Modifier.fillMaxHeight().padding(vertical = 2.dp),
                                color = colors.outlineVariant,
                            )
                        }
                        NutrientValue(label, grams, Modifier.weight(1f).padding(horizontal = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientValue(label: String, grams: Double, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "${formatIntakeValue(grams)} g",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

internal fun formatIntakeValue(value: Double): String {
    if (!value.isFinite()) return "—"

    return roundNutritionValue(value, decimals = 1)
        .stripTrailingZeros()
        .toPlainString()
}
