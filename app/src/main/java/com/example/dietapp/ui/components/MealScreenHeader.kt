package com.example.dietapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun MealScreenHeader(
    title: String,
    onBack: () -> Unit,
    backEnabled: Boolean = true,
    trailing: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            onClick = onBack,
            enabled = backEnabled,
            modifier = Modifier.semantics { contentDescription = "뒤로 가기" },
            shape = CircleShape,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
            ),
        ) {
            Text(
                text = "←",
                modifier = Modifier.clearAndSetSemantics {},
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f).semantics { heading() },
            color = colors.primary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        trailing()
    }
}

@Composable
internal fun MealGinghamDecoration(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val base = colors.secondaryContainer
    val stripe = colors.primary.copy(alpha = 0.24f)

    Canvas(
        modifier = modifier
            .padding(horizontal = 5.dp, vertical = 7.dp)
            .size(width = 70.dp, height = 22.dp)
            .rotate(12f),
    ) {
        val teeth = 6
        val toothWidth = 1.8.dp.toPx()
        val toothHeight = size.height / teeth
        val tape = Path().apply {
            moveTo(toothWidth, 0f)
            lineTo(size.width - toothWidth, 0f)
            for (index in 1..teeth) {
                val x = if (index % 2 == 0) size.width - toothWidth else size.width
                lineTo(x, index * toothHeight)
            }
            lineTo(toothWidth, size.height)
            for (index in teeth - 1 downTo 0) {
                val x = if (index % 2 == 0) toothWidth else 0f
                lineTo(x, index * toothHeight)
            }
            close()
        }
        translate(top = 1.dp.toPx()) {
            drawPath(tape, colors.onSurface.copy(alpha = 0.14f))
        }
        clipPath(tape) {
            drawRect(base)
            val cell = size.width / 8
            repeat(4) { index ->
                drawRect(stripe, Offset(index * 2 * cell, 0f), Size(cell, size.height))
            }
            for (index in 0..(size.height / (2 * cell)).toInt()) {
                drawRect(stripe, Offset(0f, index * 2 * cell), Size(size.width, cell))
            }
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.30f), Color.Transparent),
                ),
            )
        }
    }
}