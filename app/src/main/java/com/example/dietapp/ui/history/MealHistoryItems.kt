package com.example.dietapp.ui.history

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dietapp.data.local.resolveMealPhotoFile
import com.example.dietapp.data.model.MealType
import com.example.dietapp.ui.components.DietCard
import java.io.IOException
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal sealed interface MealHistoryPhoto {
    data class Saved(val storedPath: String) : MealHistoryPhoto
    data class Sample(val drawableRes: Int) : MealHistoryPhoto
}

internal data class MealMacros(
    val carbsG: Double,
    val proteinG: Double,
    val fatG: Double,
)

internal data class MealFoodNutrition(
    val name: String,
    val amountG: Double,
    val kcal: Double,
    val macros: MealMacros,
)

internal data class MealDetailNutrition(
    val totalMacros: MealMacros,
    val foods: List<MealFoodNutrition>,
)

internal data class MealHistoryRow(
    val id: Long,
    val dateTimeMillis: Long,
    val mealType: MealType,
    val totalKcal: Double,
    val photo: MealHistoryPhoto? = null,
    val nutrition: MealDetailNutrition? = null,
)

internal sealed interface MealHistoryListState {
    data object WaitingForIntegration : MealHistoryListState
    data object Loading : MealHistoryListState
    data object Error : MealHistoryListState
    data class Ready(val rows: List<MealHistoryRow>) : MealHistoryListState
}

internal data class MealHistoryData(
    val historyState: MealHistoryListState,
    val intakeForDate: (Long) -> DailyIntakeCardState,
)

internal fun LazyListScope.mealHistoryItems(
    state: MealHistoryListState,
    visibleRows: List<MealHistoryRow>,
    onMealClick: (Long) -> Unit,
) {
    item(key = "meal-history-heading") {
        Text(
            text = "식사 목록",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }

    val message = when (state) {
        MealHistoryListState.WaitingForIntegration -> "식사 목록 연동 대기"
        MealHistoryListState.Loading -> "식사 기록을 불러오는 중…"
        MealHistoryListState.Error -> "식사 기록을 불러오지 못했어요."
        is MealHistoryListState.Ready ->
            if (visibleRows.isEmpty()) "이 날짜의 식사 기록이 없어요" else null
    }

    if (message != null) {
        item(key = "meal-history-status") {
            val colors = MaterialTheme.colorScheme
            if (state is MealHistoryListState.Ready) {
                DietCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = colors.tertiaryContainer,
                    contentColor = colors.onTertiaryContainer,
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "위에서 끼니를 선택해 기록을 추가해 보세요.",
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onBackground,
                )
            }
        }
    } else {
        items(
            items = visibleRows,
            key = { row -> row.id },
            contentType = { "meal-history-row" },
        ) { row ->
            MealHistoryRowCard(row = row, onClick = { onMealClick(row.id) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealHistoryRowCard(
    row: MealHistoryRow,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dateTimeLabel = remember(row.dateTimeMillis) {
        formatMealHistoryDateTime(row.dateTimeMillis)
    }
    val foods = row.nutrition?.foods.orEmpty()
    val foodSummary = foods.firstOrNull()?.let { firstFood ->
        if (foods.size == 1) firstFood.name else "${firstFood.name} 외 ${foods.size - 1}개"
    }

    DietCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .testTag("meal-row-${row.id}")
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MealHistoryThumbnail(photo = row.photo)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = dateTimeLabel,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )

                if (foodSummary != null) {
                    Text(
                        text = foodSummary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    MealHistoryBadge(mealType = row.mealType, emphasized = true)
                    Text(
                        text = "${formatIntakeValue(row.totalKcal)} kcal",
                        color = colors.primary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = "›",
                modifier = Modifier.clearAndSetSemantics {},
                color = colors.primary,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Composable
internal fun MealHistoryBadge(mealType: MealType, emphasized: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (emphasized) colors.primary else colors.secondaryContainer,
        contentColor = if (emphasized) colors.onPrimary else colors.primary,
    ) {
        Text(
            text = mealType.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MealHistoryThumbnail(photo: MealHistoryPhoto?) {
    val colors = MaterialTheme.colorScheme
    val painter = rememberMealHistoryPhotoPainter(photo)

    Box(
        modifier = Modifier
            .size(width = 74.dp, height = 54.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = "식사 사진",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_gallery),
                contentDescription = "식사 사진 대체 이미지",
                modifier = Modifier.size(24.dp),
                tint = colors.onSecondaryContainer,
            )
        }
    }
}

@Composable
internal fun rememberMealHistoryPhotoPainter(
    photo: MealHistoryPhoto?,
    maxEdgePx: Int = 512,
): Painter? = when (photo) {
    is MealHistoryPhoto.Saved -> rememberSavedMealPhotoPainter(photo.storedPath, maxEdgePx)
    is MealHistoryPhoto.Sample -> painterResource(photo.drawableRes)
    null -> null
}

@Composable
private fun rememberSavedMealPhotoPainter(
    storedPath: String,
    maxEdgePx: Int,
): Painter? {
    if (LocalInspectionMode.current) return null

    val context = LocalContext.current
    var bitmap by remember(storedPath, context, maxEdgePx) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(storedPath, context, maxEdgePx) {
        bitmap = withContext(Dispatchers.IO) {
            readMealPhoto(context, storedPath, maxEdgePx)
        }
    }

    val photo = bitmap ?: return null
    return remember(photo) { BitmapPainter(photo) }
}

private fun readMealPhoto(
    context: Context,
    storedPath: String,
    maxEdgePx: Int,
): ImageBitmap? {
    return try {
        val source = if (storedPath.startsWith("content://")) {
            ImageDecoder.createSource(context.contentResolver, Uri.parse(storedPath))
        } else {
            val file = resolveMealPhotoFile(context.filesDir, storedPath) ?: return null
            ImageDecoder.createSource(file)
        }

        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val width = info.size.width
            val height = info.size.height
            val longestEdge = maxOf(width, height)
            if (longestEdge > maxEdgePx) {
                val scale = maxEdgePx.toDouble() / longestEdge
                decoder.setTargetSize(
                    (width * scale).roundToInt().coerceAtLeast(1),
                    (height * scale).roundToInt().coerceAtLeast(1),
                )
            }
        }.asImageBitmap()
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}
