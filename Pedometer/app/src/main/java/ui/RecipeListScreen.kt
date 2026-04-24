package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

data class Recipe(
    val id: Int,
    val name: String,
    val desc: String
)
@Composable
fun RecipeListScreen(navController: NavHostController) {

    val fakeRecipes = listOf(
        Recipe(1, "减脂餐", "高蛋白 · 低脂"),
        Recipe(2, "增肌餐", "高蛋白 · 高碳水"),
        Recipe(3, "均衡饮食", "营养均衡"),
        Recipe(4, "轻食", "低热量 · 清爽")
    )
    val recipes = fakeRecipes   // 之后换成数据库

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ⭐ 标题
        Text(
            text = "食谱列表",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ⭐ 列表
        LazyColumn {

            items(recipes) { recipe ->

                RecipeItem(
                    recipe = recipe,
                    onClick = {
                        navController.navigate("recipe_detail/${recipe.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun RecipeItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Row(
            modifier = Modifier.padding(12.dp)
        ) {

            // 👉 左：图片占位
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 👉 右：文字
            Column(
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = recipe.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}