package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.name.pedometer.data.entity.FoodEntity
import com.name.pedometer.data.enums.FoodCategory
import com.name.pedometer.data.enums.MealType
import com.name.pedometer.viewmodel.FoodViewModel
import kotlin.collections.List
import kotlin.collections.Set
import kotlin.collections.chunked
import kotlin.collections.emptyList
import kotlin.collections.emptySet
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.plus
import kotlin.collections.setOf
import kotlin.collections.sumOf
import com.name.pedometer.data.entity.FoodRecordEntity
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSelectorScreen(
    foods: List<FoodEntity>,
    mealType: MealType,
    foodViewModel: FoodViewModel,
    navController: NavController   // ✅ 必须加
) {
    var selectedCategory by remember { mutableStateOf(FoodCategory.CARB) }
    var selectedFood by remember { mutableStateOf<FoodEntity?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    // ⭐ 临时新增
    var pendingRecords by remember { mutableStateOf(listOf<Pair<FoodEntity, Float>>()) }

    // ⭐ 临时删除
    var pendingDeleteIds by remember { mutableStateOf(setOf<Int>()) }

    // ⭐ 展开面板
    var expanded by remember { mutableStateOf(false) }

    val todayRecords by foodViewModel.todayRecords.collectAsState()

    val currentMealRecords = todayRecords.filter { it.mealType == mealType }

    // ⭐ 本次卡路里
    val pendingCalories = pendingRecords.sumOf { (food, grams) ->
        (food.caloriesPer100g * grams / 100f).toDouble()
    }.toFloat()

    Column(modifier = Modifier.fillMaxSize()) {

        MealHeader(mealType)

        SearchBar()

        Row(modifier = Modifier.weight(1f)) {

            CategorySidebar(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            FoodList(
                foods = foods.filter { it.category == selectedCategory },
                onAddClick = {
                    selectedFood = it
                    showSheet = true
                }
            )
        }

        // ⭐ 展开面板
        if (expanded) {
            TodayFoodPanel(
                records = currentMealRecords,
                foods = foods,
                pendingDeleteIds = pendingDeleteIds,
                onDelete = { id ->
                    pendingDeleteIds = pendingDeleteIds + id
                }
            )
        }

        // ⭐⭐ 底部栏（核心）
        BottomBar(
            calories = pendingCalories,
            onExpandClick = { expanded = !expanded },
            onConfirm = {

                // ⭐ 删除旧记录
                pendingDeleteIds.forEach {
                    foodViewModel.deleteRecord(it)
                }

                // ⭐ 添加新记录
                pendingRecords.forEach { (food, grams) ->
                    foodViewModel.addFoodRecord(
                        food = food,
                        grams = grams,
                        mealType = mealType
                    )
                }

                // ⭐ 清空
                pendingRecords = emptyList()
                pendingDeleteIds = emptySet()

                // ⭐ 返回
                navController.popBackStack()
            }
        )
    }

    // ⭐ 输入克数弹窗
    if (showSheet && selectedFood != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false }
        ) {
            FoodInputSheet(
                food = selectedFood!!,
                onConfirm = { grams ->
                    pendingRecords = pendingRecords + (selectedFood!! to grams)
                    showSheet = false
                }
            )
        }
    }
}
@Composable
fun MealHeader(mealType: MealType) {

    val title = when (mealType) {
        MealType.BREAKFAST -> "早餐"
        MealType.LUNCH -> "午餐"
        MealType.DINNER -> "晚餐"
        MealType.SNACK -> "加餐"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "今天 · $title",
            modifier = Modifier.padding(16.dp)
        )
    }
}
// ⭐ 搜索栏
@Composable
fun SearchBar() {
    TextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("搜索食物（暂未实现）") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        enabled = false
    )
}

// ⭐ 左侧分类
@Composable
fun CategorySidebar(
    selectedCategory: FoodCategory,
    onCategorySelected: (FoodCategory) -> Unit
) {
    val categories = FoodCategory.values()

    LazyColumn(
        modifier = Modifier
            .width(100.dp)
            .fillMaxHeight()
    ) {
        items(categories) { category ->

            val isSelected = category == selectedCategory

            Text(
                text = category.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategorySelected(category) }
                    .background(
                        if (isSelected) Color.LightGray else Color.Transparent
                    )
                    .padding(12.dp),
                color = if (isSelected) Color.Black else Color.Gray
            )
        }
    }
}

// ⭐ 右侧食物列表
@Composable
fun FoodList(
    foods: List<FoodEntity>,
    onAddClick: (FoodEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(foods) { food ->

            FoodItem(
                food = food,
                onAddClick = onAddClick
            )
        }
    }
}
@Composable
fun FoodItem(
    food: FoodEntity,
    onAddClick: (FoodEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(text = food.name)
                Text(
                    text = "${food.caloriesPer100g} kcal/100g",
                    color = Color.Gray
                )
            }

            Button(
                onClick = { onAddClick(food) }
            ) {
                Text("+")
            }
        }
    }
}
@Composable
fun FoodInputSheet(
    food: FoodEntity,
    onConfirm: (Float) -> Unit
) {
    var input by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        // 顶部
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(food.name)
                Text("${food.caloriesPer100g} kcal/100g", color = Color.Gray)
            }

            Text(
                text = "确定",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onConfirm(input.toFloatOrNull() ?: 0f)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 中间显示
        Text(
            text = "$input g",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 数字键盘
        val buttons = listOf(
            "1","2","3",
            "4","5","6",
            "7","8","9",
            ".","0","del"
        )

        Column {
            buttons.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { label ->
                        KeyButton(label) {
                            input = handleInput(input, label)
                        }
                    }
                }
            }
        }
    }
}
fun handleInput(current: String, input: String): String {
    return when (input) {
        "del" -> if (current.length > 1) current.dropLast(1) else "0"
        "." -> if (current.contains(".")) current else "$current."
        else -> {
            if (current == "0") input else current + input
        }
    }
}
@Composable
fun KeyButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(4.dp)
            .size(80.dp)
    ) {
        Text(text)
    }
}
@Composable
fun BottomBar(
    calories: Float,
    onConfirm: () -> Unit,
    onExpandClick: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "本次摄入：${calories.toInt()} kcal",
                modifier = Modifier.clickable { onExpandClick() } // ✅ 只让左侧可点
            )

            Button(
                onClick = onConfirm,
                enabled = calories > 0f
            ) {
                Text("完成")
            }
        }
    }
}
@Composable
fun TodayFoodPanel(
    records: List<FoodRecordEntity>,
    foodMap: Map<Int, FoodEntity>,
    pendingDeleteIds: Set<Int>,
    onDelete: (Int) -> Unit,
    onConfirmDelete: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .padding(12.dp)
        ) {

            Text("今日已选", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {

                items(records) { record ->

                    val food = foodMap[record.foodId]
                    val cal = (record.grams * (food?.caloriesPer100g ?: 0f)) / 100f

                    if (!pendingDeleteIds.contains(record.id)) {
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

            // ⭐ 完成按钮（真正删除）
            Button(
                onClick = onConfirmDelete,
                modifier = Modifier.fillMaxWidth(),
                enabled = pendingDeleteIds.isNotEmpty()
            ) {
                Text("完成删除")
            }
        }
    }
}