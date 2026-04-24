package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RecipeDetailScreen(recipeId: Int) {

    var selectedDay by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        // 1️⃣ 食谱标题
        Text(
            text = "减脂食谱",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2️⃣ 天数选择
        DaySelector(selectedDay) {
            selectedDay = it
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3️⃣ 当天内容
        DayMealContent(day = selectedDay)
    }
}
@Composable
fun DaySelector(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (1..7).forEach { day ->

            val isSelected = day == selectedDay

            Text(
                text = "Day $day",
                modifier = Modifier
                    .clickable { onDaySelected(day) }
                    .padding(8.dp),
                color = if (isSelected) Color.Black else Color.Gray
            )
        }
    }
}
@Composable
fun DayMealContent(day: Int) {

    Column {

        MealBlock("早餐", "燕麦 80g + 鸡蛋 100g", "350 kcal")
        MealBlock("午餐", "鸡胸肉 200g + 米饭 100g", "500 kcal")
        MealBlock("晚餐", "牛肉 150g + 土豆 150g", "450 kcal")

    }
}
@Composable
fun MealBlock(
    title: String,
    foods: String,
    calories: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(title, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))

            Text(foods, color = Color.Gray)

            Text(calories, color = Color.Gray)
        }
    }
}