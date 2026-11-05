package com.example.dietapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dietapp.ui.analysis.MealAnalysisScreen
import com.example.dietapp.ui.entry.MealEntryScreen
import com.example.dietapp.ui.info.InfoScreen
import com.example.dietapp.ui.list.MealListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Entry.route,
        modifier = modifier
    ) {
        composable(Screen.Entry.route) {
            MealEntryScreen()
        }
        composable(Screen.List.route) {
            MealListScreen()
        }
        composable(Screen.Analysis.route) {
            MealAnalysisScreen()
        }
        composable(Screen.Info.route) {
            InfoScreen()
        }
    }
}
