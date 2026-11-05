package com.example.dietapp.ui.entry

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dietapp.DietApplication
import com.example.dietapp.data.model.MealType
import com.example.dietapp.ui.components.FoodSelectItem
import com.example.dietapp.ui.components.NutritionSummaryBar
import com.example.dietapp.ui.components.PhotoPicker
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealEntryScreen(
    modifier: Modifier = Modifier,
    viewModel: MealEntryViewModel = viewModel(
        factory = MealEntryViewModel.Factory(
            (LocalContext.current.applicationContext as DietApplication).foodRepository,
            (LocalContext.current.applicationContext as DietApplication).mealRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            viewModel.resetSavedState()
        }
    }

    val datePickerDialog = remember(uiState.dateTime) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDateTime = uiState.dateTime
                    .withYear(year)
                    .withMonth(month + 1)
                    .withDayOfMonth(dayOfMonth)
                viewModel.onDateTimeChanged(newDateTime)
            },
            uiState.dateTime.year,
            uiState.dateTime.monthValue - 1,
            uiState.dateTime.dayOfMonth
        )
    }

    val timePickerDialog = remember(uiState.dateTime) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newDateTime = uiState.dateTime
                    .withHour(hourOfDay)
                    .withMinute(minute)
                viewModel.onDateTimeChanged(newDateTime)
            },
            uiState.dateTime.hour,
            uiState.dateTime.minute,
            false
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("식사 입력하기", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 날짜 및 시간 선택
            item {
                Text(
                    text = "1. 날짜 및 시간 선택",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerDialog.show() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = uiState.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { timePickerDialog.show() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = uiState.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 2. 끼니 종류 선택
            item {
                Text(
                    text = "2. 끼니 종류 선택",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MealType.entries.forEach { mealType ->
                        FilterChip(
                            selected = uiState.mealType == mealType,
                            onClick = { viewModel.onMealTypeChanged(mealType) },
                            label = { Text(mealType.displayName) }
                        )
                    }
                }
            }

            // 3. 사진 선택
            item {
                Text(
                    text = "3. 음식 사진 입력",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                PhotoPicker(
                    photoUri = uiState.photoUri,
                    onPhotoSelected = { viewModel.onPhotoSelected(it) }
                )
            }

            // 4. 음식 검색 및 추가
            item {
                Text(
                    text = "4. 음식 검색 및 추가",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("음식 이름을 검색하세요 (예: 쌀밥, 제육볶음)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                if (uiState.searchResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column {
                            uiState.searchResults.take(5).forEach { food ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.addFoodToSelection(food) }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = food.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "100g당 ${food.kcal} kcal | ${food.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { viewModel.addFoodToSelection(food) }) {
                                        Icon(Icons.Default.Add, contentDescription = "추가")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. 선택된 음식 및 섭취량(g) 목록
            if (uiState.selectedFoods.isNotEmpty()) {
                item {
                    Text(
                        text = "5. 선택된 음식 및 섭취량(g) 입력",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                itemsIndexed(uiState.selectedFoods) { index, item ->
                    FoodSelectItem(
                        item = item,
                        onGramsChanged = { viewModel.updateFoodGrams(index, it) },
                        onRemove = { viewModel.removeFoodFromSelection(index) }
                    )
                }
            }

            // 6. 실시간 영양성분 미리보기 카드
            item {
                Spacer(modifier = Modifier.height(8.dp))
                NutritionSummaryBar(nutrients = uiState.calculatedNutrients)
            }

            // 7. 저장하기 버튼
            item {
                Button(
                    onClick = { viewModel.saveMeal() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isSaving && uiState.selectedFoods.isNotEmpty()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(
                            text = "식사 기록 저장하기",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
