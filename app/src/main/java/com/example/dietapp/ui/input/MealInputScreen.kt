package com.example.dietapp.ui.input

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dietapp.FoodCatalogStatus
import com.example.dietapp.R
import com.example.dietapp.data.model.MealType
import com.example.dietapp.data.repository.FoodRepository
import com.example.dietapp.data.repository.MealRepository
import com.example.dietapp.ui.components.MealGinghamDecoration
import com.example.dietapp.ui.components.MealScreenHeader
import com.example.dietapp.ui.history.MealDatePickerDialog
import com.example.dietapp.ui.history.seoulDateAsUtcMillis
import com.example.dietapp.ui.history.seoulHourAndMinute
import com.example.dietapp.ui.history.seoulMealTimeMillis
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class MealInputPicker { DATE, TIME }

private data class MealInputDraft(
    val dateTimeMillis: Long,
    val mealType: MealType?,
    val photoUri: Uri? = null,
    val foodQuery: String = "",
    val selectedFoods: List<MealInputFoodAmount> = emptyList(),
)

private const val SavedFoodAmountsVersion = 1

private val MealInputDraftSaver = listSaver<MealInputDraft, Any>(
    save = { draft ->
        buildList {
            add(draft.dateTimeMillis)
            add(draft.mealType?.name.orEmpty())
            add(draft.photoUri?.toString().orEmpty())
            add(draft.foodQuery)
            add(SavedFoodAmountsVersion)
            draft.selectedFoods.forEach { food ->
                add(food.foodCode)
                add(food.displayName)
                add(food.gramsText)
            }
        }
    },
    restore = { saved ->
        val mealTypeName = saved[1] as String
        val photoUri = (saved[2] as String).takeIf { it.isNotEmpty() }?.let(Uri::parse)
        val selectedFoods = if (saved.getOrNull(4) == SavedFoodAmountsVersion) {
            saved.drop(5).chunked(3).map { fields ->
                MealInputFoodAmount(
                    foodCode = fields[0] as String,
                    displayName = fields[1] as String,
                    gramsText = fields[2] as String,
                )
            }
        } else {
            emptyList()
        }
        MealInputDraft(
            dateTimeMillis = saved[0] as Long,
            mealType = MealType.entries.firstOrNull { it.name == mealTypeName },
            photoUri = photoUri,
            foodQuery = saved[3] as String,
            selectedFoods = selectedFoods,
        )
    },
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MealInputScreen(
    initialDateUtcMillis: Long,
    initialMealType: MealType?,
    saveViewModel: MealInputSaveViewModel,
    saveState: MealSaveState,
    foodRepository: FoodRepository,
    mealRepository: MealRepository,
    catalogStatus: FoodCatalogStatus,
    onBack: () -> Unit,
) {
    val window = LocalActivity.current?.window
    DisposableEffect(window) {
        if (window == null) return@DisposableEffect onDispose {}
        val previousMode = window.attributes.softInputMode
        val resizeMode = (previousMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST.inv()) or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        window.setSoftInputMode(resizeMode)
        onDispose {
            if (window.attributes.softInputMode == resizeMode) {
                window.setSoftInputMode(previousMode)
            }
        }
    }

    var draft by rememberSaveable(stateSaver = MealInputDraftSaver) {
        val (hour, minute) = seoulHourAndMinute(System.currentTimeMillis())
        mutableStateOf(
            MealInputDraft(
                dateTimeMillis = seoulMealTimeMillis(initialDateUtcMillis, hour, minute),
                mealType = initialMealType,
            ),
        )
    }
    var activePicker by rememberSaveable { mutableStateOf<MealInputPicker?>(null) }
    var photoSelectionVersion by remember { mutableIntStateOf(0) }

    val focusManager = LocalFocusManager.current
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null && !saveViewModel.isLocked) {
            draft = draft.copy(photoUri = uri)
            photoSelectionVersion += 1
        }
    }

    val editable = !saveState.locksInput
    val selectedDateUtcMillis = seoulDateAsUtcMillis(draft.dateTimeMillis)
    val (selectedHour, selectedMinute) = seoulHourAndMinute(draft.dateTimeMillis)
    val dateLabel = remember(selectedDateUtcMillis) {
        SimpleDateFormat("M월 d일", Locale.KOREAN).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(selectedDateUtcMillis))
    }
    val timeLabel = String.format(Locale.KOREAN, "%02d:%02d", selectedHour, selectedMinute)
    val colors = MaterialTheme.colorScheme
    val selectedPhoto = draft.photoUri
    val largeText = LocalDensity.current.fontScale >= 1.5f

    MealInputFoodSection(
        modifier = Modifier.fillMaxSize().imePadding(),
        headerContent = {
            MealScreenHeader(title = "식사 입력", onBack = onBack, backEnabled = editable) {
                MealGinghamDecoration()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MealInputDateTimeButton(
                    label = "날짜",
                    value = dateLabel,
                    icon = Icons.Filled.DateRange,
                    enabled = editable,
                    onClick = { if (!saveViewModel.isLocked) activePicker = MealInputPicker.DATE },
                    modifier = Modifier.weight(1f),
                )
                MealInputDateTimeButton(
                    label = "시간",
                    value = timeLabel,
                    icon = Icons.Filled.AccessTime,
                    enabled = editable,
                    onClick = { if (!saveViewModel.isLocked) activePicker = MealInputPicker.TIME },
                    modifier = Modifier.weight(1f),
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxItemsInEachRow = if (largeText) 2 else 4,
            ) {
                MealType.entries.forEach { mealType ->
                    val selected = draft.mealType == mealType
                    FilterChip(
                        selected = selected,
                        onClick = {
                            if (!saveViewModel.isLocked) draft = draft.copy(mealType = mealType)
                        },
                        enabled = editable,
                        modifier = Modifier.weight(1f).widthIn(min = 72.dp),
                        label = {
                            Text(
                                mealType.displayName,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.surface,
                            labelColor = colors.onSurface,
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = colors.onPrimary,
                        ),
                    )
                }
            }

            MealInputPhotoSection(
                photoUri = selectedPhoto,
                selectionVersion = photoSelectionVersion,
                editable = editable,
                onPick = {
                    if (!saveViewModel.isLocked) {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    }
                },
                onRemove = {
                    if (!saveViewModel.isLocked) draft = draft.copy(photoUri = null)
                },
            )
        },
        query = draft.foodQuery,
        selectedFoods = draft.selectedFoods,
        foodRepository = foodRepository,
        catalogStatus = catalogStatus,
        onQueryChange = { query ->
            if (!saveViewModel.isLocked) draft = draft.copy(foodQuery = query)
        },
        onFoodAdd = { food ->
            if (!saveViewModel.isLocked && food.supportsGramInput &&
                draft.selectedFoods.none { it.foodCode == food.foodCode }
            ) {
                draft = draft.copy(
                    selectedFoods = draft.selectedFoods + MealInputFoodAmount(
                        foodCode = food.foodCode,
                        displayName = food.name,
                    ),
                )
            }
        },
        onGramsChange = { foodCode, gramsText ->
            if (!saveViewModel.isLocked) {
                draft = draft.copy(
                    selectedFoods = draft.selectedFoods.map { food ->
                        if (food.foodCode == foodCode) food.copy(gramsText = gramsText) else food
                    },
                )
            }
        },
        mealTypeSelected = draft.mealType != null,
        saveState = saveState,
        onSave = save@{ foodsByCode ->
            if (saveViewModel.isLocked) return@save
            val captured = draft
            val mealType = captured.mealType ?: return@save
            val validInputs = validMealInputsByCode(captured.selectedFoods, foodsByCode)
            if (!areMealFoodsValid(validInputs.values, captured.selectedFoods.size)) {
                return@save
            }

            saveViewModel.save(
                request = MealSaveRequest(
                    dateTimeMillis = captured.dateTimeMillis,
                    mealType = mealType,
                    photoUri = captured.photoUri,
                    foods = validInputs.values.toList(),
                ),
                repository = mealRepository,
            )
            focusManager.clearFocus()
        },
    )

    if (editable) {
        when (activePicker) {
            MealInputPicker.DATE -> MealDatePickerDialog(
                selectedDateUtcMillis = selectedDateUtcMillis,
                onConfirm = { newDate ->
                    if (!saveViewModel.isLocked) {
                        draft = draft.copy(
                            dateTimeMillis = seoulMealTimeMillis(newDate, selectedHour, selectedMinute),
                        )
                    }
                    activePicker = null
                },
                onDismiss = { activePicker = null },
            )
            MealInputPicker.TIME -> MealInputTimePickerDialog(
                initialHour = selectedHour,
                initialMinute = selectedMinute,
                onConfirm = { hour, minute ->
                    if (!saveViewModel.isLocked) {
                        draft = draft.copy(
                            dateTimeMillis = seoulMealTimeMillis(selectedDateUtcMillis, hour, minute),
                        )
                    }
                    activePicker = null
                },
                onDismiss = { activePicker = null },
            )
            null -> Unit
        }
    }
}

@Composable
private fun MealInputDateTimeButton(
    label: String,
    value: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp).semantics { contentDescription = "$label 선택 $value" },
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        border = BorderStroke(1.dp, colors.outlineVariant),
        elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 1.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = colors.surface,
            contentColor = colors.primary,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            if (LocalDensity.current.fontScale < 1.5f) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MealInputPhotoSection(
    photoUri: Uri?,
    selectionVersion: Int,
    editable: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (photoUri == null) {
            Surface(
                modifier = Modifier.weight(1.45f),
                shape = MaterialTheme.shapes.large,
                color = colors.surfaceContainerLow,
                border = BorderStroke(1.dp, colors.outlineVariant),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                ) {
                    Icon(
                        Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Text("사진 미리보기", style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
                    Text(
                        "사진은 선택 사항이에요",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            MealInputPhotoPreview(
                photoUri = photoUri,
                selectionVersion = selectionVersion,
                modifier = Modifier.weight(1.45f),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.bori_photo_peek),
                contentDescription = null,
                modifier = Modifier.widthIn(max = 112.dp).fillMaxWidth().aspectRatio(2f),
                contentScale = ContentScale.Fit,
            )
            FilledTonalButton(
                onClick = onPick,
                enabled = editable,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colors.tertiaryContainer,
                    contentColor = colors.onTertiaryContainer,
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (photoUri == null) "사진 선택" else "사진 변경",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            if (photoUri != null) {
                TextButton(onClick = onRemove, enabled = editable) {
                    Text("사진 제거", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealInputTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("시간 선택") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimePicker(state = state, layoutType = TimePickerLayoutType.Vertical)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("확인") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
        shape = MaterialTheme.shapes.large,
    )
}

private sealed interface MealInputPhotoState {
    data class Message(val text: String) : MealInputPhotoState
    data class Ready(val bitmap: ImageBitmap) : MealInputPhotoState
}

@Composable
private fun MealInputPhotoPreview(
    photoUri: Uri,
    selectionVersion: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val resolver = LocalContext.current.contentResolver

    BoxWithConstraints(modifier = modifier) {
        val maxEdgePx = with(LocalDensity.current) { maxWidth.roundToPx().coerceIn(1, 1600) }
        var photoState by remember(resolver, photoUri, selectionVersion, maxEdgePx) {
            mutableStateOf<MealInputPhotoState>(
                MealInputPhotoState.Message("사진을 불러오는 중…"),
            )
        }

        LaunchedEffect(resolver, photoUri, selectionVersion, maxEdgePx) {
            val bitmap = withContext(Dispatchers.IO) {
                readMealInputPhoto(resolver, photoUri, maxEdgePx)
            }
            photoState = if (bitmap == null) {
                MealInputPhotoState.Message("사진을 읽을 수 없어요. 다시 선택해 주세요.")
            } else {
                MealInputPhotoState.Ready(bitmap)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = colors.surface,
            contentColor = colors.onSurface,
        ) {
            when (val state = photoState) {
                is MealInputPhotoState.Message -> Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.text,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
                is MealInputPhotoState.Ready -> Image(
                    bitmap = state.bitmap,
                    contentDescription = "선택한 식사 사진",
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.65f),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

private fun readMealInputPhoto(
    resolver: ContentResolver,
    uri: Uri,
    maxEdgePx: Int,
): ImageBitmap? {
    return try {
        val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val width = info.size.width
                val height = info.size.height
                val longestSide = maxOf(width, height)
                if (longestSide > maxEdgePx) {
                    val scale = maxEdgePx.toDouble() / longestSide
                    decoder.setTargetSize(
                        (width * scale).roundToInt().coerceAtLeast(1),
                        (height * scale).roundToInt().coerceAtLeast(1),
                    )
                }
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val longestSide = maxOf(bounds.outWidth, bounds.outHeight)
            var sampleSize = 1
            while (longestSide / sampleSize > maxEdgePx) {
                sampleSize *= 2
            }
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        }
        bitmap?.asImageBitmap()
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}
