package com.example.dietapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Entry : Screen("entry", "음식 입력", Icons.Default.Edit)
    object List : Screen("list", "식사 목록", Icons.Default.List)
    object Analysis : Screen("analysis", "식사 분석", Icons.Default.Analytics)
    object Info : Screen("info", "정보/출처", Icons.Default.Info)

    companion object {
        val bottomNavItems = listOf(Entry, List, Analysis, Info)
    }
}
