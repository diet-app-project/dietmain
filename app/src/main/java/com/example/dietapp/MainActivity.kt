package com.example.dietapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dietapp.ui.navigation.DietNavGraph
import com.example.dietapp.ui.theme.DietAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dietApplication = application as DietApplication

        setContent {
            DietAppTheme {
                DietNavGraph(
                    foodRepository = dietApplication.foodRepository,
                    mealRepository = dietApplication.mealRepository,
                    foodCatalogStatus = dietApplication.foodCatalogStatus,
                )
            }
        }
    }
}