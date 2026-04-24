package com.name.pedometer.data.initializer

import com.name.pedometer.data.dao.FoodDao
import com.name.pedometer.data.dao.RecipeDao
import com.name.pedometer.data.entity.RecipeEntity
import com.name.pedometer.data.entity.RecipeMealEntity
import com.name.pedometer.data.entity.RecipeMealFoodEntity
import com.name.pedometer.data.enums.MealType

class RecipeInitializer(
    private val recipeDao: RecipeDao,
    private val foodDao: FoodDao
) {
    val weeklyPlan = mapOf(

        1 to Triple(
            // 早餐
            listOf("燕麦" to 80f, "鸡蛋" to 100f, "牛奶" to 200f),
            // 午餐
            listOf("鸡胸肉" to 200f, "西兰花" to 200f, "米饭" to 100f),
            // 晚餐
            listOf("牛肉" to 150f, "土豆" to 150f)
        ),

        2 to Triple(
            listOf("燕麦" to 100f, "鸡蛋" to 100f),
            listOf("牛肉" to 200f, "西兰花" to 200f, "红薯" to 150f),
            listOf("鸡胸肉" to 150f, "生菜" to 150f)
        ),

        3 to Triple(
            listOf("鸡蛋" to 150f, "牛奶" to 200f),
            listOf("鸡胸肉" to 200f, "菠菜" to 200f, "米饭" to 150f),
            listOf("三文鱼" to 150f, "西兰花" to 150f)
        ),

        4 to Triple(
            listOf("燕麦" to 80f, "牛奶" to 200f),
            listOf("牛肉" to 200f, "胡萝卜" to 150f, "土豆" to 150f),
            listOf("鸡胸肉" to 150f, "生菜" to 150f)
        ),

        5 to Triple(
            listOf("鸡蛋" to 150f, "牛奶" to 200f),
            listOf("鸡胸肉" to 200f, "西兰花" to 200f, "红薯" to 150f),
            listOf("牛肉" to 150f, "菠菜" to 150f)
        ),

        6 to Triple(
            listOf("燕麦" to 100f, "鸡蛋" to 100f),
            listOf("三文鱼" to 200f, "西兰花" to 200f, "米饭" to 150f),
            listOf("鸡胸肉" to 150f, "生菜" to 150f)
        ),

        7 to Triple(
            listOf("鸡蛋" to 150f, "牛奶" to 200f),
            listOf("牛肉" to 200f, "胡萝卜" to 150f, "土豆" to 150f),
            listOf("三文鱼" to 150f, "西兰花" to 150f)
        )
    )

    // ⭐ 正确入口函数
    suspend fun insertFatLossRecipe() {

        // 1️⃣ 先插入 Recipe
        val recipeId = recipeDao.insertRecipe(
            RecipeEntity(name = "七天减脂餐")
        ).toInt()

        // 2️⃣ 再循环
        for ((day, meals) in weeklyPlan) {

            val (breakfast, lunch, dinner) = meals

            addMealWithFoods(recipeId, day, MealType.BREAKFAST, breakfast)
            addMealWithFoods(recipeId, day, MealType.LUNCH, lunch)
            addMealWithFoods(recipeId, day, MealType.DINNER, dinner)
        }
    }

    // ⭐ 插入一餐
    private suspend fun addMealWithFoods(
        recipeId: Int,
        day: Int,
        mealType: MealType,
        foods: List<Pair<String, Float>>
    ) {

        val mealId = recipeDao.insertMeal(
            RecipeMealEntity(
                recipeId = recipeId,
                dayIndex = day,
                mealType = mealType
            )
        ).toInt()

        val mealFoods = mutableListOf<RecipeMealFoodEntity>()

        for ((name, grams) in foods) {
            val food = foodDao.getFoodByName(name) ?: continue

            mealFoods.add(
                RecipeMealFoodEntity(
                    recipeMealId = mealId,
                    foodId = food.id,
                    weight = grams
                )
            )
        }

        if (mealFoods.isNotEmpty()) {
            recipeDao.insertMealFoods(mealFoods)
        }
    }
}