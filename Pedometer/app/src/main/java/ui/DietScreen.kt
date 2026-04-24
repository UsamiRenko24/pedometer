package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.name.pedometer.data.entity.FoodRecordEntity
import com.name.pedometer.data.entity.FoodEntity
import androidx.navigation.NavController
import com.name.pedometer.data.enums.MealType
import com.name.pedometer.viewmodel.FoodViewModel
import androidx.compose.foundation.combinedClickable

@Composable
fun DietScreen(
    navController: NavController,
    foodViewModel: FoodViewModel,
) {
    val mealCalories by foodViewModel.mealCalories.collectAsState()
    val totalCalories = mealCalories.values.sum()

    val todayRecords by foodViewModel.todayRecords.collectAsState()
    val foods by foodViewModel.foods.collectAsState()

    var expandedMeal by remember { mutableStateOf<MealType?>(null) }
    var pendingDeleteIds by remember { mutableStateOf(setOf<Int>()) }

    // ⭐ 优化：O(1) 查找
    val foodMap = remember(foods) {
        foods.associateBy { it.id }
    }

    // ⭐ 切换餐时清空删除缓存
    LaunchedEffect(expandedMeal) {
        pendingDeleteIds = emptySet()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        TotalCaloriesCard(totalCalories)

        MealRow(
            navController = navController,
            foodViewModel = foodViewModel,
            onMealClick = { meal ->
                expandedMeal = if (expandedMeal == meal) null else meal
            }
        )

        RecipeSection(
            onMoreClick = {
                navController.navigate("recipe_list")
            },
            onMealClick = {
                navController.navigate("recipe_detail/1")
            }
        )

        // ⭐ 展开面板
        expandedMeal?.let { meal ->

            val currentMealRecords = todayRecords.filter {
                it.mealType == meal
            }

            TodayFoodPanel(
                records = currentMealRecords,
                foodMap = foodMap,
                pendingDeleteIds = pendingDeleteIds,
                onDelete = { id ->
                    pendingDeleteIds = pendingDeleteIds + id
                },
                onConfirmDelete = {
                    pendingDeleteIds.forEach {
                        foodViewModel.deleteRecord(it)
                    }
                    pendingDeleteIds = emptySet()
                }
            )
        }
    }
}
@Composable
fun TotalCaloriesCard(total: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = "今日摄入：${total.toInt()} kcal",
            modifier = Modifier.padding(16.dp)
        )
    }
}

// 2️⃣ 四个餐
@Composable
fun MealRow(
    navController: NavController,
    foodViewModel: FoodViewModel,
    onMealClick: (MealType) -> Unit
) {
    val mealCalories by foodViewModel.mealCalories.collectAsState()

    val meals = listOf(
        MealType.BREAKFAST to "早餐",
        MealType.LUNCH to "午餐",
        MealType.DINNER to "晚餐",
        MealType.SNACK to "加餐"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        meals.forEach { (type, label) ->

            MealItem(
                title = label,
                calories = mealCalories[type] ?: 0f,
                onClick = {
                    // ⭐ 直接跳转（更符合用户习惯）
                    navController.navigate("meal/" + type.name)
                },
                onLongClick = {
                    // ⭐ 长按展开（高级功能）
                    onMealClick(type)
                }
            )
        }
    }
}
@Composable
fun MealItem(
    title: String,
    calories: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title)

            Text(
                text = "${calories.toInt()} kcal",
                color = Color.Gray
            )
        }
    }
}
fun getCurrentMeal(): MealType {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..10 -> MealType.BREAKFAST
        in 11..15 -> MealType.LUNCH
        in 17..21 -> MealType.DINNER
        else -> MealType.SNACK
    }
}

@Composable
fun RecipeSection(
    onMoreClick: () -> Unit,
    onMealClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "每日食谱",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "更多 >",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onMoreClick() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 👇 单餐展示
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            // 👉 左：圆环（先占位）
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("3/7") // TODO 之后换成进度环
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 👉 右：三餐卡片
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                MealCard("早餐") {
                    onMealClick()
                }

                MealCard("午餐") {
                    onMealClick()
                }

                MealCard("晚餐") {
                    onMealClick()
                }
            }
        }
    }
}

@Composable
fun MealCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun SingleMealContent(mealType: MealType) {

    val (title, content) = when (mealType) {
        MealType.BREAKFAST -> "早餐" to "燕麦 + 鸡蛋 + 牛奶"
        MealType.LUNCH -> "午餐" to "鸡胸肉 + 米饭 + 西兰花"
        MealType.DINNER -> "晚餐" to "牛肉 + 土豆"
        MealType.SNACK -> "加餐" to "酸奶 + 坚果"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = content,
            color = Color.Gray
        )
    }
}
@Composable
fun TodayFoodPanel(
    records: List<FoodRecordEntity>,
    foods: List<FoodEntity>,
    pendingDeleteIds: Set<Int>,
    onDelete: (Int) -> Unit
) {
    Surface(
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(12.dp)
        ) {

            Text("今日已选", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {

                items(records) { record ->

                    val food = foods.find { it.id == record.foodId }
                    val cal = ((record.grams * (food?.caloriesPer100g ?: 0f)) / 100f)

                    val isDeleted = pendingDeleteIds.contains(record.id)

                    if (!isDeleted) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Column {
                                Text(food?.name ?: "未知")
                                Text("${cal.toInt()} kcal", color = Color.Gray)
                            }

                            Text(
                                text = "删除",
                                color = Color.Red,
                                modifier = Modifier.clickable {
                                    onDelete(record.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}