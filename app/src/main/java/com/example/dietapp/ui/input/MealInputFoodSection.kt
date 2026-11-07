package com.example.dietapp.ui.input

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dietapp.FoodCatalogStatus
import com.example.dietapp.data.model.Food
import com.example.dietapp.data.repository.FoodRepository
import com.example.dietapp.domain.CalculatedNutrients
import com.example.dietapp.domain.NutritionCalculator
import com.example.dietapp.ui.components.DietCard
import com.example.dietapp.ui.components.MealFoodIcon
import com.example.dietapp.ui.history.formatIntakeValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private sealed interface MealInputSearchState {
    data object Initializing : MealInputSearchState
    data object CatalogError : MealInputSearchState
    data object Loading : MealInputSearchState
    data object Error : MealInputSearchState
    data class Ready(val foods: List<Food>) : MealInputSearchState
}

private val MealPreviewCloud = GenericShape { size, _ ->
    val inset = minOf(size.width * 0.035f, size.height * 0.11f)
    val last = size.width - inset
    val bottom = size.height - inset
    val scallops = 8
    val step = (last - inset) / scallops
    moveTo(inset, inset)
    repeat(scallops) { index ->
        val x = inset + step * index
        quadraticTo(x + step / 2, -inset * 0.55f, x + step, inset)
    }
    cubicTo(size.width, inset, size.width, bottom, last, bottom)
    repeat(scallops) { index ->
        val x = last - step * index
        quadraticTo(x - step / 2, size.height + inset * 0.55f, x - step, bottom)
    }
    cubicTo(0f, bottom, 0f, inset, inset, inset)
    close()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MealInputFoodSection(
    query: String,
    selectedFoods: List<MealInputFoodAmount>,
    foodRepository: FoodRepository,
    catalogStatus: FoodCatalogStatus,
    onQueryChange: (String) -> Unit,
    onFoodAdd: (Food) -> Unit,
    onGramsChange: (String, String) -> Unit,
    mealTypeSelected: Boolean,
    saveState: MealSaveState,
    onSave: (Map<String, Food>) -> Unit,
    modifier: Modifier = Modifier,
    headerContent: @Composable () -> Unit,
) {
    val searchTerm = query.trim()
    var searchState by remember(foodRepository, catalogStatus, searchTerm) {
        mutableStateOf<MealInputSearchState>(
            when (catalogStatus) {
                FoodCatalogStatus.INITIALIZING -> MealInputSearchState.Initializing
                FoodCatalogStatus.ERROR -> MealInputSearchState.CatalogError
                FoodCatalogStatus.READY -> MealInputSearchState.Loading
            },
        )
    }
    var foodsByCode by remember(foodRepository, catalogStatus) {
        mutableStateOf<Map<String, Food>>(emptyMap())
    }

    LaunchedEffect(foodRepository, catalogStatus, searchTerm) {
        if (catalogStatus != FoodCatalogStatus.READY) return@LaunchedEffect
        try {
            val catalog = foodRepository.getAllFoods()
            val search = if (searchTerm.isEmpty()) {
                catalog.map { foods -> foods to foods }
            } else {
                catalog.combine(foodRepository.searchFoods(searchTerm)) { foods, matches ->
                    foods to matches
                }
            }
            search.collect { (foods, matches) ->
                currentCoroutineContext().ensureActive()
                foodsByCode = foods.associateBy { it.foodCode }
                searchState = MealInputSearchState.Ready(matches.distinctBy { it.foodCode })
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            searchState = MealInputSearchState.Error
        }
    }

    val editable = !saveState.locksInput
    val selectedCodes = remember(selectedFoods) { selectedFoods.map { it.foodCode }.toSet() }
    val validInputs = remember(selectedFoods, foodsByCode) {
        validMealInputsByCode(selectedFoods, foodsByCode)
    }
    val calculator = remember { NutritionCalculator() }
    val colors = MaterialTheme.colorScheme
    val largeText = LocalDensity.current.fontScale >= 1.5f

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            headerContent()
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                enabled = editable,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("음식·반찬 검색") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.secondaryContainer,
                    unfocusedContainerColor = colors.secondaryContainer,
                    disabledContainerColor = colors.secondaryContainer,
                    unfocusedBorderColor = colors.outlineVariant,
                    focusedLeadingIconColor = colors.primary,
                    unfocusedLeadingIconColor = colors.primary,
                ),
            )

            Text("검색 결과", style = MaterialTheme.typography.titleSmall)
            when (val result = searchState) {
                MealInputSearchState.Initializing -> Text("음식 카탈로그 초기화 중…")
                MealInputSearchState.CatalogError -> Text(
                    text = "음식 카탈로그를 준비하지 못했어요",
                    color = colors.error,
                )
                MealInputSearchState.Loading -> Text("음식을 검색하는 중…")
                MealInputSearchState.Error -> Text(
                    text = "음식 검색 결과를 불러오지 못했어요",
                    color = colors.error,
                )
                is MealInputSearchState.Ready -> {
                    if (result.foods.isEmpty()) {
                        Text("검색 결과가 없어요")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(result.foods, key = { it.foodCode }) { food ->
                                MealInputFoodResult(
                                    food = food,
                                    selected = food.foodCode in selectedCodes,
                                    editable = editable,
                                    onClick = { onFoodAdd(food) },
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "선택한 음식",
                color = colors.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (selectedFoods.isEmpty()) {
                DietCard(modifier = Modifier.fillMaxWidth()) {
                    Text("추가한 음식이 없어요", color = colors.onSurfaceVariant)
                }
            }

            selectedFoods.forEach { amount ->
                key(amount.foodCode) {
                    val food = foodsByCode[amount.foodCode]
                    val validInput = validInputs[amount.foodCode]
                    val amountError = when {
                        amount.gramsText.isBlank() -> "섭취량을 입력하세요"
                        validFoodGrams(amount.gramsText) == null -> "0보다 큰 숫자를 입력하세요"
                        else -> null
                    }

                    DietCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                itemVerticalAlignment = Alignment.CenterVertically,
                                maxItemsInEachRow = if (largeText) 1 else 2,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).widthIn(min = 112.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    MealFoodIcon(foodName = amount.displayName, foodCode = amount.foodCode)
                                    Text(
                                        text = amount.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                MealInputGramsField(
                                    amount = amount,
                                    editable = editable,
                                    isError = amountError != null,
                                    onGramsChange = { onGramsChange(amount.foodCode, it) },
                                    modifier = if (largeText) Modifier.fillMaxWidth() else Modifier.width(144.dp),
                                )
                            }
                            if (amountError != null) {
                                Text(
                                    text = amountError,
                                    color = colors.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            if (validInput != null) {
                                HorizontalDivider(color = colors.outlineVariant)
                                MealInputNutritionValues(
                                    calculator.calculateNutrients(validInput.food, validInput.grams),
                                    modifier = Modifier.testTag("food-nutrition-${amount.foodCode}"),
                                )
                            } else if (food != null && !food.supportsGramInput) {
                                Text("단위 확인 필요", color = colors.error)
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().testTag("nutrition-preview"),
                shape = MealPreviewCloud,
                color = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(colors.surface.copy(alpha = 0.22f), colors.primaryContainer),
                            ),
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "한 끼 미리보기",
                        color = colors.primary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    when {
                        selectedFoods.isEmpty() -> Text("음식을 추가하면 영양값을 볼 수 있어요")
                        validInputs.isEmpty() -> Text("유효한 섭취량을 입력하면 합계를 볼 수 있어요")
                        else -> {
                            MealInputNutritionValues(
                                calculator.calculateTotalNutrients(validInputs.values.toList()),
                                emphasized = true,
                            )
                            if (validInputs.size != selectedFoods.size) {
                                Text("일부 음식만 반영됐어요 (${validInputs.size}/${selectedFoods.size}개)")
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = colors.outlineVariant)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Button(
                onClick = { onSave(foodsByCode) },
                enabled = editable && mealTypeSelected &&
                    areMealFoodsValid(validInputs.values, selectedFoods.size),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                ),
            ) {
                Text(
                    when {
                        saveState.locksInput -> "저장 중…"
                        else -> "저장하기"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (saveState is MealSaveState.Failed) {
                Text(
                    text = saveState.message,
                    color = colors.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun MealInputGramsField(
    amount: MealInputFoodAmount,
    editable: Boolean,
    isError: Boolean,
    onGramsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPresets by remember(editable) { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        OutlinedTextField(
            value = amount.gramsText,
            onValueChange = onGramsChange,
            enabled = editable,
            modifier = Modifier.fillMaxWidth().testTag("food-grams-${amount.foodCode}"),
            label = { Text("섭취량") },
            suffix = { Text("g") },
            trailingIcon = {
                IconButton(onClick = { showPresets = true }, enabled = editable) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "${amount.displayName} 섭취량 선택",
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            isError = isError,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = colors.outlineVariant),
        )
        DropdownMenu(expanded = showPresets, onDismissRequest = { showPresets = false }) {
            for (grams in listOf("50", "100", "150", "200")) {
                DropdownMenuItem(
                    text = { Text("$grams g") },
                    onClick = {
                        showPresets = false
                        onGramsChange(grams)
                    },
                )
            }
        }
    }
}

@Composable
private fun MealInputFoodResult(
    food: Food,
    selected: Boolean,
    editable: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = editable && !selected && food.supportsGramInput,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (selected) colors.secondaryContainer else colors.surface,
        contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MealFoodIcon(foodName = food.name, modifier = Modifier.size(26.dp), foodCode = food.foodCode)
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.titleSmall)
                if (!food.supportsGramInput) {
                    Text(food.basis, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                text = when {
                    !food.supportsGramInput -> "단위 확인 필요"
                    selected -> "추가됨"
                    else -> "추가"
                },
                modifier = Modifier.widthIn(max = 100.dp),
                color = if (food.supportsGramInput) colors.primary else colors.error,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealInputNutritionValues(
    values: CalculatedNutrients,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        maxItemsInEachRow = if (LocalDensity.current.fontScale >= 1.5f) 1 else 2,
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${formatIntakeValue(values.kcal)} kcal",
            color = if (emphasized) colors.primary else LocalContentColor.current,
            style = if (emphasized) typography.headlineLarge else typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "탄수화물 ${formatIntakeValue(values.carb)} g · " +
                    "단백질 ${formatIntakeValue(values.protein)} g · 지방 ${formatIntakeValue(values.fat)} g",
            modifier = Modifier.weight(1f).widthIn(min = 128.dp),
            style = typography.bodySmall,
        )
    }
}
