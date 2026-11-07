package com.example.dietapp.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dietapp.R
import com.example.dietapp.data.model.MealType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val SeoulTimeZone = TimeZone.getTimeZone("Asia/Seoul")
private val UtcTimeZone = TimeZone.getTimeZone("UTC")

@Composable
internal fun MealListRoute(
    onMealClick: (Long) -> Unit,
    onAddMealClick: (MealType, Long) -> Unit,
    isRecordActive: Boolean,
    intakeForDate: (Long) -> DailyIntakeCardState = {
        DailyIntakeCardState.WaitingForIntegration
    },
    historyForDate: (Long) -> MealHistoryListState = {
        MealHistoryListState.WaitingForIntegration
    },
    savedDateUtcMillis: Long? = null,
    onSavedDateHandled: () -> Unit = {},
) {
    var selectedDateUtcMillis by rememberSaveable {
        mutableStateOf(todayInSeoulAsUtcMillis())
    }
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var returnScrollIndex by rememberSaveable { mutableStateOf(-1) }
    var returnScrollOffset by rememberSaveable { mutableStateOf(0) }

    fun saveScrollPosition() {
        returnScrollIndex = listState.firstVisibleItemIndex
        returnScrollOffset = listState.firstVisibleItemScrollOffset
    }

    LaunchedEffect(isRecordActive, savedDateUtcMillis, listState) {
        if (!isRecordActive) return@LaunchedEffect

        if (savedDateUtcMillis != null && savedDateUtcMillis != selectedDateUtcMillis) {
            selectedDateUtcMillis = savedDateUtcMillis
            returnScrollIndex = -1
            listState.requestScrollToItem(0)
        } else if (returnScrollIndex >= 0) {
            listState.requestScrollToItem(returnScrollIndex, returnScrollOffset)
            returnScrollIndex = -1
        }

        if (savedDateUtcMillis != null) onSavedDateHandled()
    }

    MealListScreen(
        selectedDateUtcMillis = selectedDateUtcMillis,
        dailyIntakeState = intakeForDate(selectedDateUtcMillis),
        historyState = historyForDate(selectedDateUtcMillis),
        listState = listState,
        onMealClick = { mealId ->
            saveScrollPosition()
            onMealClick(mealId)
        },
        onAddMealClick = { mealType ->
            saveScrollPosition()
            onAddMealClick(mealType, selectedDateUtcMillis)
        },
        onPreviousDay = { selectedDateUtcMillis = moveDateByDays(selectedDateUtcMillis, -1) },
        onToday = { selectedDateUtcMillis = todayInSeoulAsUtcMillis() },
        onNextDay = { selectedDateUtcMillis = moveDateByDays(selectedDateUtcMillis, 1) },
        onOpenCalendar = { showCalendar = true },
    )

    if (showCalendar) {
        MealDatePickerDialog(
            selectedDateUtcMillis = selectedDateUtcMillis,
            onConfirm = { pickedDateUtcMillis ->
                selectedDateUtcMillis = pickedDateUtcMillis
                showCalendar = false
            },
            onDismiss = { showCalendar = false },
        )
    }
}

@Composable
private fun MealListScreen(
    selectedDateUtcMillis: Long,
    dailyIntakeState: DailyIntakeCardState,
    historyState: MealHistoryListState,
    listState: LazyListState,
    onMealClick: (Long) -> Unit,
    onAddMealClick: (MealType) -> Unit,
    onPreviousDay: () -> Unit,
    onToday: () -> Unit,
    onNextDay: () -> Unit,
    onOpenCalendar: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dateLabel = remember(selectedDateUtcMillis) {
        SimpleDateFormat("M월 d일 EEEE", Locale.KOREAN).apply {
            timeZone = UtcTimeZone
        }.format(Date(selectedDateUtcMillis))
    }

    val visibleRows = remember(selectedDateUtcMillis, historyState) {
        if (historyState is MealHistoryListState.Ready) {
            mealsOnSeoulDate(historyState.rows, selectedDateUtcMillis)
        } else {
            emptyList()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "record-heading") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "식단 기록",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                )
                MealDateIconButton(
                    icon = MealDateIcon.CALENDAR,
                    actionName = "달력 열기",
                    onClick = onOpenCalendar,
                )
            }
        }

        item(key = "record-date-controls") {
            MealDateControls(dateLabel, onPreviousDay, onToday, onNextDay)
        }

        item(key = "record-daily-intake") {
            DailyIntakeCard(state = dailyIntakeState)
        }

        item(key = "record-add-meal") {
            MealAddSection(onAddMealClick = onAddMealClick)
        }

        mealHistoryItems(
            state = historyState,
            visibleRows = visibleRows,
            onMealClick = onMealClick,
        )
    }
}

@Composable
private fun MealDateControls(
    dateLabel: String,
    onPreviousDay: () -> Unit,
    onToday: () -> Unit,
    onNextDay: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MealDateIconButton(MealDateIcon.PREVIOUS, "이전 날짜", onPreviousDay)
        Surface(
            modifier = Modifier.weight(1f),
            shape = CircleShape,
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outlineVariant),
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(start = 10.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = dateLabel,
                    modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                    color = colors.primary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onToday,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text("오늘", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        MealDateIconButton(MealDateIcon.NEXT, "다음 날짜", onNextDay)
    }
}

@Composable
private fun MealAddSection(onAddMealClick: (MealType) -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "식사 추가",
            style = MaterialTheme.typography.titleLarge,
            color = colors.primary,
            fontWeight = FontWeight.Bold,
        )

        MealType.entries.chunked(2).forEach { mealTypes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                mealTypes.forEach { mealType ->
                    MealAddButton(
                        mealType = mealType,
                        onClick = { onAddMealClick(mealType) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MealAddButton(
    mealType: MealType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < 0.5f
    val container = when (mealType) {
        MealType.BREAKFAST -> if (dark) Color(0xFF554018) else Color(0xFFFFE3A1)
        MealType.LUNCH -> colors.secondaryContainer
        MealType.DINNER -> colors.tertiaryContainer
        MealType.SNACK -> if (dark) Color(0xFF3F4829) else Color(0xFFE8EEC9)
    }
    val content = when (mealType) {
        MealType.BREAKFAST -> if (dark) Color(0xFFFFE5A6) else Color(0xFF563308)
        MealType.LUNCH -> colors.onSecondaryContainer
        MealType.DINNER -> colors.onTertiaryContainer
        MealType.SNACK -> if (dark) Color(0xFFE5EFC3) else Color(0xFF374719)
    }
    val icon = when (mealType) {
        MealType.BREAKFAST -> R.drawable.ic_bori_breakfast
        MealType.LUNCH -> R.drawable.ic_bori_rice
        MealType.DINNER -> R.drawable.ic_bori_moon
        MealType.SNACK -> R.drawable.ic_bori_drink
    }
    val largeText = LocalDensity.current.fontScale >= 1.5f

    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = if (largeText) 78.dp else 54.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = container,
            contentColor = content,
        ),
        border = BorderStroke(1.dp, content.copy(alpha = 0.24f)),
        elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 1.dp),
        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(27.dp),
            )
            Text(
                text = mealType.displayName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                shape = CircleShape,
                color = colors.surface,
                contentColor = content,
                border = BorderStroke(1.dp, content.copy(alpha = 0.20f)),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).padding(4.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MealDatePickerDialog(
    selectedDateUtcMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedYear = Calendar.getInstance(UtcTimeZone, Locale.KOREAN).apply {
        timeInMillis = selectedDateUtcMillis
    }.get(Calendar.YEAR)
    val defaultYears = DatePickerDefaults.YearRange
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateUtcMillis,
        initialDisplayedMonthMillis = selectedDateUtcMillis,
        yearRange = minOf(defaultYears.first, selectedYear)..maxOf(defaultYears.last, selectedYear),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { pickerState.selectedDateMillis?.let(onConfirm) },
                enabled = pickerState.selectedDateMillis != null,
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
        shape = MaterialTheme.shapes.large,
    ) {
        DatePicker(state = pickerState, showModeToggle = false)
    }
}

private enum class MealDateIcon { PREVIOUS, NEXT, CALENDAR }

@Composable
private fun MealDateIconButton(
    icon: MealDateIcon,
    actionName: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val imageVector = when (icon) {
        MealDateIcon.PREVIOUS -> Icons.Filled.KeyboardArrowLeft
        MealDateIcon.NEXT -> Icons.Filled.KeyboardArrowRight
        MealDateIcon.CALENDAR -> Icons.Outlined.CalendarToday
    }
    val isCalendar = icon == MealDateIcon.CALENDAR

    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp).semantics { role = Role.Button },
        shape = if (isCalendar) MaterialTheme.shapes.medium else CircleShape,
        color = if (isCalendar) colors.secondaryContainer else colors.surfaceContainerLow,
        contentColor = colors.primary,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector, contentDescription = actionName, modifier = Modifier.size(24.dp))
        }
    }
}

internal fun todayInSeoulAsUtcMillis(): Long =
    seoulDateAsUtcMillis(System.currentTimeMillis())

internal fun seoulDateAsUtcMillis(dateTimeMillis: Long): Long {
    val seoulDay = Calendar.getInstance(SeoulTimeZone, Locale.KOREAN).apply {
        timeInMillis = dateTimeMillis
    }
    return Calendar.getInstance(UtcTimeZone, Locale.KOREAN).apply {
        clear()
        set(
            seoulDay.get(Calendar.YEAR),
            seoulDay.get(Calendar.MONTH),
            seoulDay.get(Calendar.DAY_OF_MONTH),
        )
    }.timeInMillis
}

internal fun seoulHourAndMinute(dateTimeMillis: Long): Pair<Int, Int> {
    val seoulTime = Calendar.getInstance(SeoulTimeZone, Locale.KOREAN).apply {
        timeInMillis = dateTimeMillis
    }
    return seoulTime.get(Calendar.HOUR_OF_DAY) to seoulTime.get(Calendar.MINUTE)
}

internal fun moveDateByDays(dateUtcMillis: Long, days: Int): Long =
    Calendar.getInstance(UtcTimeZone, Locale.KOREAN).apply {
        timeInMillis = dateUtcMillis
        add(Calendar.DAY_OF_MONTH, days)
    }.timeInMillis

internal fun formatMealHistoryDateTime(dateTimeMillis: Long): String =
    SimpleDateFormat("M월 d일 '·' HH:mm", Locale.KOREAN).apply {
        timeZone = SeoulTimeZone
    }.format(Date(dateTimeMillis))

internal fun seoulMealTimeMillis(dateUtcMillis: Long, hour: Int, minute: Int): Long {
    val utcDay = Calendar.getInstance(UtcTimeZone, Locale.KOREAN).apply {
        timeInMillis = dateUtcMillis
    }
    return Calendar.getInstance(SeoulTimeZone, Locale.KOREAN).apply {
        clear()
        set(
            utcDay.get(Calendar.YEAR),
            utcDay.get(Calendar.MONTH),
            utcDay.get(Calendar.DAY_OF_MONTH),
            hour,
            minute,
            0,
        )
    }.timeInMillis
}

private fun mealsOnSeoulDate(
    rows: List<MealHistoryRow>,
    selectedDateUtcMillis: Long,
): List<MealHistoryRow> {
    val dayStart = seoulMealTimeMillis(selectedDateUtcMillis, hour = 0, minute = 0)
    val nextDate = moveDateByDays(selectedDateUtcMillis, 1)
    val nextDayStart = seoulMealTimeMillis(nextDate, hour = 0, minute = 0)

    return rows
        .filter { row -> row.dateTimeMillis in dayStart until nextDayStart }
        .sortedWith(
            compareByDescending<MealHistoryRow> { it.dateTimeMillis }
                .thenByDescending { it.id },
        )
}
