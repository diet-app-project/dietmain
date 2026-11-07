package com.example.dietapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
internal fun MealFoodIcon(
    foodName: String,
    modifier: Modifier = Modifier,
    foodCode: String = "",
) {
    val drawable = remember(foodCode, foodName) {
        catalogFoodIconResource(foodCode, foodName)
    }
    val iconModifier = modifier.size(34.dp)

    if (drawable != null) {
        Image(
            painter = painterResource(drawable),
            contentDescription = null,
            modifier = iconModifier,
        )
    } else {
        val colors = MaterialTheme.colorScheme
        Surface(
            modifier = iconModifier,
            shape = CircleShape,
            color = colors.surfaceContainerHigh,
            contentColor = colors.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}
