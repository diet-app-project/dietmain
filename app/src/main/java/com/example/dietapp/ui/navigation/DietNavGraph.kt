package com.example.dietapp.ui.navigation

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dietapp.FoodCatalogStatus
import com.example.dietapp.data.repository.FoodRepository
import com.example.dietapp.data.repository.MealRepository
import com.example.dietapp.ui.history.MealDetailScreen
import com.example.dietapp.ui.history.MealHistoryEntry
import com.example.dietapp.data.model.MealType
import com.example.dietapp.ui.history.detailHeaderFor
import com.example.dietapp.ui.history.rememberMealHistoryData
import com.example.dietapp.ui.history.todayInSeoulAsUtcMillis
import com.example.dietapp.ui.input.MealInputSaveViewModel
import com.example.dietapp.ui.input.MealInputScreen
import com.example.dietapp.ui.input.MealSaveState
import com.example.dietapp.ui.statistics.StatisticsPeriod
import com.example.dietapp.ui.statistics.StatisticsScreen
import kotlinx.coroutines.flow.StateFlow

private const val MealDetailPath = "meal-detail"
private const val MealIdArgument = "mealId"
private const val MealDetailRoute = "$MealDetailPath/{$MealIdArgument}"

private const val MealInputPath = "meal-input"
private const val MealTypeArgument = "mealType"
private const val MealInputDateArgument = "dateUtcMillis"
private const val NoMealInputDate = Long.MIN_VALUE
private const val MealInputRoute =
    "$MealInputPath/{$MealTypeArgument}?$MealInputDateArgument={$MealInputDateArgument}"

private const val MealSavedResultKey = "b018.savedMeal"

private enum class DietTab(val route: String, val label: String) {
    RECORD("record", "기록"),
    STATISTICS("statistics", "통계"),
}

private val dietTabs = listOf(DietTab.RECORD, DietTab.STATISTICS)

@Composable
internal fun DietNavGraph(
    foodRepository: FoodRepository,
    mealRepository: MealRepository,
    foodCatalogStatus: StateFlow<FoodCatalogStatus>,
) {
    val navController = rememberNavController()
    var statisticsPeriod by rememberSaveable { mutableStateOf(StatisticsPeriod.DAILY) }
    val historyData by rememberUpdatedState(rememberMealHistoryData(mealRepository))
    val currentEntry by navController.currentBackStackEntryAsState()
    val selectedRoute = currentEntry?.destination?.route ?: DietTab.RECORD.route

    fun openFromRecord(route: String) {
        if (navController.currentDestination?.route == DietTab.RECORD.route) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = tabBar@{
            if (selectedRoute == MealDetailRoute || selectedRoute == MealInputRoute) return@tabBar
            DietBottomBar(
                selectedRoute = selectedRoute,
                onTabClick = { tab ->
                    if (navController.currentDestination?.route != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(DietTab.RECORD.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DietTab.RECORD.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable(DietTab.RECORD.route) { recordEntry ->
                val savedMeal by remember(recordEntry) {
                    recordEntry.savedStateHandle.getStateFlow<Bundle?>(MealSavedResultKey, null)
                }.collectAsState()

                MealHistoryEntry(
                    historyData = historyData,
                    isRecordActive = currentEntry === recordEntry,
                    onMealClick = { mealId -> openFromRecord("$MealDetailPath/$mealId") },
                    onAddMealClick = { mealType, dateUtcMillis ->
                        openFromRecord(
                            "$MealInputPath/${mealType.name}?$MealInputDateArgument=$dateUtcMillis",
                        )
                    },
                    savedDateUtcMillis = savedMeal?.getLong(MealInputDateArgument),
                    onSavedDateHandled = {
                        recordEntry.savedStateHandle.set<Bundle?>(MealSavedResultKey, null)
                    },
                )
            }

            composable(
                route = MealInputRoute,
                arguments = listOf(
                    navArgument(MealTypeArgument) {
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument(MealInputDateArgument) {
                        type = NavType.LongType
                        defaultValue = NoMealInputDate
                    },
                ),
            ) { inputEntry ->
                val mealTypeName = inputEntry.arguments?.getString(MealTypeArgument)
                val initialMealType = MealType.entries.firstOrNull { it.name == mealTypeName }
                val initialDateUtcMillis = remember(inputEntry) {
                    inputEntry.arguments
                        ?.getLong(MealInputDateArgument, NoMealInputDate)
                        ?.takeUnless { it == NoMealInputDate }
                        ?: todayInSeoulAsUtcMillis()
                }
                val saveViewModel: MealInputSaveViewModel =
                    viewModel(viewModelStoreOwner = inputEntry)
                val saveState by saveViewModel.state.collectAsState()
                val catalogStatus by foodCatalogStatus.collectAsState()
                val isInputActive = currentEntry === inputEntry
                val inputLifecycleState by inputEntry.lifecycle.currentStateFlow.collectAsState()

                val onBack: () -> Unit = {
                    if (!saveViewModel.isLocked && navController.currentBackStackEntry === inputEntry) {
                        navController.popBackStack()
                    }
                }
                BackHandler(enabled = isInputActive, onBack = onBack)

                LaunchedEffect(saveState, isInputActive, inputLifecycleState) {
                    val success = saveState as? MealSaveState.Succeeded ?: return@LaunchedEffect
                    if (!isInputActive || inputEntry.lifecycle.currentState != Lifecycle.State.RESUMED) {
                        return@LaunchedEffect
                    }
                    if (navController.currentBackStackEntry !== inputEntry) return@LaunchedEffect
                    val recordEntry = navController.previousBackStackEntry ?: return@LaunchedEffect
                    if (recordEntry.destination.route != DietTab.RECORD.route) return@LaunchedEffect
                    if (!saveViewModel.consumeSuccess(success.result)) return@LaunchedEffect

                    recordEntry.savedStateHandle[MealSavedResultKey] = Bundle().apply {
                        putLong(MealIdArgument, success.result.mealId)
                        putLong(MealInputDateArgument, success.result.dateUtcMillis)
                    }
                    navController.popBackStack()
                }

                MealInputScreen(
                    initialDateUtcMillis = initialDateUtcMillis,
                    initialMealType = initialMealType,
                    saveViewModel = saveViewModel,
                    saveState = saveState,
                    foodRepository = foodRepository,
                    mealRepository = mealRepository,
                    catalogStatus = catalogStatus,
                    onBack = onBack,
                )
            }

            composable(
                route = MealDetailRoute,
                arguments = listOf(
                    navArgument(MealIdArgument) {
                        type = NavType.LongType
                        nullable = false
                    },
                ),
            ) { detailEntry ->
                val arguments = requireNotNull(detailEntry.arguments)
                require(arguments.containsKey(MealIdArgument))
                val mealId = arguments.getLong(MealIdArgument)

                MealDetailScreen(
                    state = historyData.historyState.detailHeaderFor(mealId),
                    onBack = { navController.popBackStack() },
                )
            }

            composable(DietTab.STATISTICS.route) {
                StatisticsScreen(
                    selectedPeriod = statisticsPeriod,
                    onPeriodSelected = { statisticsPeriod = it },
                    historyState = historyData.historyState,
                )
            }
        }
    }
}

@Composable
private fun DietBottomBar(
    selectedRoute: String,
    onTabClick: (DietTab) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = CircleShape,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            dietTabs.forEach { tab ->
                val selected = selectedRoute == tab.route
                val containerColor = if (selected) colors.primary else colors.surface
                val contentColor = if (selected) colors.onPrimary else colors.primary
                val icon = when (tab) {
                    DietTab.RECORD -> Icons.Filled.Restaurant
                    DietTab.STATISTICS -> Icons.Filled.BarChart
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .background(containerColor)
                        .selectable(
                            selected = selected,
                            onClick = { onTabClick(tab) },
                            role = Role.Tab,
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = contentColor,
                    )
                    Text(
                        text = tab.label,
                        color = contentColor,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}