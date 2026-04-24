package ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.name.pedometer.data.enums.MealType
import com.name.pedometer.viewmodel.FoodViewModel
import com.name.pedometer.viewmodel.StepViewModel
import ui.DietScreen
import ui.FoodSelectorScreen
import ui.RecipeDetailScreen
import ui.RecipeListScreen
import ui.StepCounterScreen

@Composable
fun MainScreen(
    viewModel: StepViewModel,
    foodViewModel: FoodViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
//    val hideBottomBarRoutes = listOf(
//        "recipe_list",
//        "recipe_detail/{recipeId}"
//    )
    fun shouldHideBottomBar(route: String?): Boolean {
        return route == "recipe_list" ||
                route == "step_detail" ||
                route?.startsWith("recipe_detail") == true ||
                route?.startsWith("meal") == true
    }
    val shouldShowBottomBar = !shouldHideBottomBar(currentRoute)
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomBar(navController)
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Step.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(Screen.Step.route) {
                StepScreenWrapper(viewModel, navController)
            }

            composable(Screen.Diet.route) {
                DietScreen(navController, foodViewModel)
            }

            composable("meal/{mealType}") { backStackEntry ->
                val type = backStackEntry.arguments?.getString("mealType")
                val mealType = try {
                    MealType.valueOf(type ?: "")
                } catch (e: Exception) {
                    MealType.BREAKFAST
                }

                val foods by foodViewModel.foods.collectAsState()

                FoodSelectorScreen(foods, mealType, foodViewModel, navController)
            }

            composable("step_detail") {

                val steps by viewModel.steps.collectAsState()

                StepCounterScreen(
                    steps = steps.toInt(),
                    calories = viewModel.getCalories(steps),
                    distance = viewModel.getDistance(steps),
                    durationSeconds = viewModel.duration,
                    isTracking = viewModel.isTracking,
                    onToggle = { viewModel.toggleTracking() }
                )
            }

            composable(Screen.Record.route) {
                Text("记录页面（占位）")
            }

            composable(Screen.Weight.route) {
                Text("体重页面（占位）")
            }

            composable(Screen.Profile.route) {
                Text("我的页面（占位）")
            }

            composable("recipe_list") {
                RecipeListScreen(navController)
            }

            composable("recipe_detail/{recipeId}") { backStackEntry ->
                val recipeId = backStackEntry.arguments
                    ?.getString("recipeId")
                    ?.toIntOrNull() ?: 0

                RecipeDetailScreen(recipeId)
            }
        }
    }
}

@Composable
fun StepScreenWrapper(
    viewModel: StepViewModel,
    navController: NavHostController
) {
    val steps by viewModel.steps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 标题
        Text(
            text = "今日活动",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("step_detail") {
                    launchSingleTop = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Text(
                text = "开始运动",
                fontSize = 20.sp
            )
        }
    }
}

