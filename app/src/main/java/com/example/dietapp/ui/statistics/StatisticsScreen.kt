package com.example.dietapp.ui.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dietapp.R
import com.example.dietapp.data.model.Food
import com.example.dietapp.data.model.MealType
import com.example.dietapp.domain.MealDateRange
import com.example.dietapp.domain.MealPeriod
import com.example.dietapp.domain.NutritionCalculator
import com.example.dietapp.ui.components.DietCard
import com.example.dietapp.ui.history.MealDetailNutrition
import com.example.dietapp.ui.history.MealFoodNutrition
import com.example.dietapp.ui.history.MealHistoryListState
import com.example.dietapp.ui.history.MealHistoryRow
import com.example.dietapp.ui.history.MealHistorySummary
import com.example.dietapp.ui.history.MealMacros
import com.example.dietapp.ui.history.formatIntakeValue
import com.example.dietapp.ui.history.seoulMealTimeMillis
import com.example.dietapp.ui.history.summarizeMealHistory
import com.example.dietapp.ui.history.todayInSeoulAsUtcMillis
import com.example.dietapp.ui.theme.DietAppTheme
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val KFindDatabaseUrl = "https://various.foodsafetykorea.go.kr/nutrient/"
private val StatisticsUtcTimeZone = TimeZone.getTimeZone("UTC")

internal enum class StatisticsPeriod(val label: String, val mealPeriod: MealPeriod) {
    DAILY("일간", MealPeriod.DAILY),
    WEEKLY("주간", MealPeriod.WEEKLY),
    LAST_30_DAYS("최근 30일", MealPeriod.LAST_30_DAYS),
}

private val StatisticsDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.ROOT)

private fun formatStatisticsDateRange(range: MealDateRange): String {
    val start = range.startDate.format(StatisticsDateFormatter)
    if (range.startDate == range.endDate) return start
    val end = range.endDate.format(StatisticsDateFormatter)
    return "$start~$end"
}

@Composable
internal fun StatisticsScreen(
    selectedPeriod: StatisticsPeriod,
    onPeriodSelected: (StatisticsPeriod) -> Unit,
    historyState: MealHistoryListState,
    referenceDateUtcMillis: Long = todayInSeoulAsUtcMillis(),
) {
    val range = remember(selectedPeriod, referenceDateUtcMillis) {
        // 날짜 선택 UI에서 넘긴 값은 UTC 자정에 담은 달력 날짜입니다.
        val referenceDate = Instant.ofEpochMilli(referenceDateUtcMillis)
            .atOffset(ZoneOffset.UTC)
            .toLocalDate()
        selectedPeriod.mealPeriod.endingOn(referenceDate)
    }
    val dateRangeLabel = remember(range) { formatStatisticsDateRange(range) }
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "섭취 통계",
            modifier = Modifier.semantics { heading() },
            color = colors.primary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        StatisticsPeriodSelector(selectedPeriod, onPeriodSelected)

        Text(
            text = dateRangeLabel,
            modifier = Modifier.fillMaxWidth(),
            color = colors.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        StatisticsIntakeSection(historyState, range)
        NutritionDataSourceCard()
    }
}

@Composable
private fun StatisticsPeriodSelector(
    selectedPeriod: StatisticsPeriod,
    onPeriodSelected: (StatisticsPeriod) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(3.dp).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatisticsPeriod.entries.forEach { period ->
                val selected = period == selectedPeriod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .background(if (selected) colors.primary else colors.surfaceContainerLow)
                        .selectable(
                            selected = selected,
                            onClick = { onPeriodSelected(period) },
                            role = Role.Tab,
                        )
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = period.label,
                        color = if (selected) colors.onPrimary else colors.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionDataSourceCard() {
    val uriHandler = LocalUriHandler.current
    var openFailed by remember { mutableStateOf(false) }
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme

    DietCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.surfaceContainerLow,
        contentColor = colors.onSurface,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("영양 데이터 출처", style = typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "식품의약품안전처 식품영양성분 데이터베이스(K-FIND)",
                modifier = Modifier.fillMaxWidth(),
                style = typography.bodyMedium,
            )
            TextButton(
                onClick = {
                    openFailed = try {
                        uriHandler.openUri(KFindDatabaseUrl)
                        false
                    } catch (_: IllegalArgumentException) {
                        true
                    } catch (_: SecurityException) {
                        true
                    }
                },
            ) {
                Text("공식 DB 보기", fontWeight = FontWeight.SemiBold)
            }
            if (openFailed) {
                Text(
                    text = "공식 DB를 열 수 없어요.",
                    color = colors.error,
                    style = typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun StatisticsIntakeSection(
    historyState: MealHistoryListState,
    range: MealDateRange,
) {
    when (historyState) {
        is MealHistoryListState.Ready -> {
            val summary = remember(historyState.rows, range) {
                summarizeMealHistory(historyState.rows, range)
            }
            StatisticsIntakeCards(summary)
        }

        MealHistoryListState.WaitingForIntegration -> StatisticsNotice("통계 데이터 연동 대기")
        MealHistoryListState.Loading -> StatisticsNotice("식사 기록을 불러오는 중…")
        MealHistoryListState.Error -> StatisticsNotice("식사 기록을 불러오지 못했어요.")
    }
}

@Composable
private fun StatisticsNotice(message: String) {
    DietCard(modifier = Modifier.fillMaxWidth()) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

private data class MacroPortion(
    val label: String,
    val grams: Double,
    val color: Color,
) {
    val drawableGrams: Double
        get() = if (grams.isFinite() && grams > 0.0) grams else 0.0
}

private fun macroPortions(macros: MealMacros): List<MacroPortion> =
    listOf(
        MacroPortion("탄수화물", macros.carbsG, Color(0xFFF5C758)),
        MacroPortion("단백질", macros.proteinG, Color(0xFFF18475)),
        MacroPortion("지방", macros.fatG, Color(0xFF9C82D6)),
    )

private data class MealMacroRow(
    val mealType: MealType,
    val portions: List<MacroPortion>,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatisticsIntakeCards(summary: MealHistorySummary) {
    val mealRows = remember(summary.macrosByMealType) {
        MealType.entries.map { mealType ->
            MealMacroRow(mealType, macroPortions(summary.macrosByMealType.getValue(mealType)))
        }
    }
    val scaleGrams = mealRows.maxOf { row -> row.portions.sumOf { it.drawableGrams } }

    StatisticsBoriEnergyCard(summary.totalKcal)

    DietCard(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, MaterialTheme.shapes.large),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                StatisticsChartTitle("끼니별 영양")
                StatisticsMacroLegend(mealRows.first().portions)
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val stacked = maxWidth < 292.dp || LocalDensity.current.fontScale >= 1.35f
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    mealRows.forEachIndexed { index, row ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        MealMacroBarRow(row, scaleGrams, stacked)
                    }
                }
            }
            if (summary.hasMissingNutrition) {
                MissingMacrosNotice()
            }
        }
    }
}

private val StatisticsCloudShape = GenericShape { size, _ ->
    val edge = minOf(size.width * 0.035f, size.height * 0.11f)
    val right = size.width - edge
    val bottom = size.height - edge
    val scallops = 8
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
private fun StatisticsBoriEnergyCard(totalKcal: Double) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = StatisticsCloudShape,
        color = colors.tertiaryContainer,
        contentColor = colors.onTertiaryContainer,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(colors.surface.copy(alpha = 0.35f), colors.tertiaryContainer),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            val largeText = LocalDensity.current.fontScale >= 1.35f
            val imageSize = minOf(maxWidth * (if (largeText) 0.25f else 0.37f), 128.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("총 섭취 열량", style = typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${formatIntakeValue(totalKcal)} kcal",
                        color = colors.primary,
                        style = typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Image(
                    painter = painterResource(R.drawable.bori_statistics),
                    contentDescription = null,
                    modifier = Modifier.size(imageSize),
                )
            }
        }
    }
}

@Composable
private fun StatisticsChartTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.semantics { heading() },
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatisticsMacroLegend(portions: List<MacroPortion>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        portions.forEach { portion ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(9.dp).background(portion.color, CircleShape))
                Text(portion.label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealMacroBarRow(row: MealMacroRow, scaleGrams: Double, stacked: Boolean) {
    val colors = MaterialTheme.colorScheme
    val values = row.portions.joinToString(" · ") { formatIntakeValue(it.grams) } + " g"
    val spokenValues = row.portions.joinToString(", ") { "${it.label} ${formatIntakeValue(it.grams)} g" }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        maxItemsInEachRow = if (stacked) 1 else 3,
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = if (stacked) Modifier else Modifier.width(70.dp),
            shape = CircleShape,
            color = colors.surfaceContainerHigh,
            contentColor = colors.onSurface,
        ) {
            Text(
                text = row.mealType.displayName,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
        MacroBalanceBar(
            portions = row.portions,
            scaleGrams = scaleGrams,
            modifier = if (stacked) Modifier else Modifier.weight(1f).widthIn(min = 72.dp),
        )
        Text(
            text = values,
            modifier = (if (stacked) Modifier else Modifier.width(108.dp))
                .clearAndSetSemantics { contentDescription = spokenValues },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = if (stacked) TextAlign.Start else TextAlign.End,
        )
    }
}

@Composable
private fun MissingMacrosNotice() {
    Text(
        text = "탄단지 수치는 영양 정보가 있는 식사만 반영했어요.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun MacroBalanceBar(
    portions: List<MacroPortion>,
    scaleGrams: Double,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (scaleGrams <= 0.0 || !scaleGrams.isFinite()) return@Canvas

        var left = 0f
        var gramsSoFar = 0.0
        portions.forEach { portion ->
            val grams = portion.drawableGrams
            if (grams == 0.0) return@forEach

            gramsSoFar += grams
            val right = size.width * (gramsSoFar / scaleGrams).toFloat()
            drawRect(
                color = portion.color,
                topLeft = Offset(left, 0f),
                size = Size(right - left, size.height),
            )
            left = right
        }
    }
}

@Preview(
    name = "영양 집계 · 2026.11.06",
    showBackground = true,
    widthDp = 412,
    heightDp = 600,
)
@Composable
private fun StatisticsScreenPreview() {
    var selectedPeriod by rememberSaveable { mutableStateOf(StatisticsPeriod.DAILY) }
    val historyState = remember { MealHistoryListState.Ready(previewMeals()) }
    DietAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StatisticsScreen(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it },
                historyState = historyState,
                referenceDateUtcMillis = previewDate(dayOfMonth = 6),
            )
        }
    }
}

@Preview(
    name = "월요일 주간 · 2026.11.02",
    showBackground = true,
    widthDp = 412,
    heightDp = 600,
)
@Composable
private fun StatisticsMondayPreview() {
    val historyState = remember { MealHistoryListState.Ready(previewMeals()) }
    DietAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StatisticsScreen(
                selectedPeriod = StatisticsPeriod.WEEKLY,
                onPeriodSelected = {},
                historyState = historyState,
                referenceDateUtcMillis = previewDate(dayOfMonth = 2),
            )
        }
    }
}

@Preview(
    name = "최근 30일 끼니별 탄단지 · 2026.11.06",
    showBackground = true,
    widthDp = 412,
    heightDp = 920,
)
@Composable
private fun StatisticsLast30DaysPreview() {
    val historyState = remember { MealHistoryListState.Ready(previewLast30DaysMeals()) }
    DietAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StatisticsScreen(
                selectedPeriod = StatisticsPeriod.LAST_30_DAYS,
                onPeriodSelected = {},
                historyState = historyState,
                referenceDateUtcMillis = previewDate(dayOfMonth = 6),
            )
        }
    }
}

@Preview(
    name = "기록 없는 기간 · 최근 30일",
    showBackground = true,
    widthDp = 412,
    heightDp = 920,
)
@Preview(
    name = "기록 없는 기간 · 큰 글자",
    showBackground = true,
    widthDp = 412,
    heightDp = 920,
    fontScale = 2f,
)
@Composable
private fun StatisticsEmptyPreview() {
    var selectedPeriod by rememberSaveable { mutableStateOf(StatisticsPeriod.LAST_30_DAYS) }
    DietAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StatisticsScreen(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it },
                historyState = MealHistoryListState.Ready(emptyList()),
                referenceDateUtcMillis = previewDate(dayOfMonth = 6),
            )
        }
    }
}

private fun previewRice(): Food = Food(
    name = "쌀밥",
    kcal = 166.0,
    carb = 37.33,
    protein = 3.36,
    fat = 0.32,
)

private fun previewKimchi(): Food = Food(
    name = "배추김치",
    kcal = 38.0,
    carb = 6.49,
    protein = 1.98,
    fat = 0.43,
)

private fun previewMeals(): List<MealHistoryRow> {
    val rice = previewRice()
    val kimchi = previewKimchi()
    return listOf(
        previewMeal(id = 1L, dayOfMonth = 1, hour = 23, items = listOf(rice to 125.0)),
        previewMeal(id = 2L, dayOfMonth = 2, hour = 0, items = listOf(rice to 100.0)),
        previewMeal(id = 6L, dayOfMonth = 6, hour = 0, items = listOf(rice to 150.5, kimchi to 40.0)),
        previewMeal(id = 7L, dayOfMonth = 7, hour = 0, items = listOf(rice to 250.0)),
    )
}

private fun previewLast30DaysMeals(): List<MealHistoryRow> {
    val rice = previewRice()
    val kimchi = previewKimchi()
    return listOf(
        previewMeal(
            id = 1007L, month = Calendar.OCTOBER, dayOfMonth = 7, hour = 23,
            mealType = MealType.SNACK, items = listOf(rice to 200.0),
        ),
        previewMeal(
            id = 1008L, month = Calendar.OCTOBER, dayOfMonth = 8, hour = 0,
            mealType = MealType.BREAKFAST, items = listOf(rice to 100.0),
        ),
        previewMeal(
            id = 1015L, month = Calendar.OCTOBER, dayOfMonth = 15, hour = 12,
            mealType = MealType.LUNCH, items = listOf(rice to 150.5, kimchi to 40.0),
        ),
        previewMeal(
            id = 1102L, dayOfMonth = 2, hour = 18,
            mealType = MealType.DINNER, items = listOf(rice to 180.0),
        ),
        previewMeal(
            id = 1106L, dayOfMonth = 6, hour = 23,
            mealType = MealType.LUNCH, items = listOf(kimchi to 40.0),
        ),
        previewMeal(
            id = 1107L, dayOfMonth = 7, hour = 0,
            mealType = MealType.SNACK, items = listOf(rice to 250.0),
        ),
    )
}

private fun previewMeal(
    id: Long,
    dayOfMonth: Int,
    hour: Int,
    items: List<Pair<Food, Double>>,
    month: Int = Calendar.NOVEMBER,
    mealType: MealType = MealType.LUNCH,
): MealHistoryRow {
    val calculator = NutritionCalculator()
    val foods = items.map { (food, grams) ->
        val values = calculator.calculateNutrients(food, grams)
        MealFoodNutrition(
            name = food.name,
            amountG = grams,
            kcal = values.kcal,
            macros = MealMacros(
                carbsG = values.carb,
                proteinG = values.protein,
                fatG = values.fat,
            ),
        )
    }

    return MealHistoryRow(
        id = id,
        dateTimeMillis = seoulMealTimeMillis(
            previewDate(dayOfMonth, month),
            hour = hour,
            minute = 30,
        ),
        mealType = mealType,
        totalKcal = foods.sumOf { it.kcal },
        nutrition = MealDetailNutrition(
            totalMacros = MealMacros(
                carbsG = foods.sumOf { it.macros.carbsG },
                proteinG = foods.sumOf { it.macros.proteinG },
                fatG = foods.sumOf { it.macros.fatG },
            ),
            foods = foods,
        ),
    )
}

private fun previewDate(dayOfMonth: Int, month: Int = Calendar.NOVEMBER): Long =
    Calendar.getInstance(StatisticsUtcTimeZone, Locale.ROOT).apply {
        clear()
        set(2026, month, dayOfMonth)
    }.timeInMillis