package com.name.pedometer.data.initializer

import com.name.pedometer.data.dao.FoodDao
import com.name.pedometer.data.entity.FoodEntity
import com.name.pedometer.data.enums.FoodCategory

class FoodInitializer(
    private val foodDao: FoodDao
) {

    suspend fun initIfNeeded() {

        // 1️⃣ 判断数据库是否已有数据
        val existing = foodDao.getAll()
        if (existing.isNotEmpty()) return

        // 2️⃣ 构建初始数据
        val foods = listOf(

            // CARB
            FoodEntity(name = "米饭", caloriesPer100g = 116f, category = FoodCategory.CARB),
            FoodEntity(name = "面条", caloriesPer100g = 138f, category = FoodCategory.CARB),
            FoodEntity(name = "燕麦", caloriesPer100g = 389f, category = FoodCategory.CARB),
            FoodEntity(name = "土豆", caloriesPer100g = 77f, category = FoodCategory.CARB),
            FoodEntity(name = "红薯", caloriesPer100g = 86f, category = FoodCategory.CARB),

            // PROTEIN
            FoodEntity(name = "鸡胸肉", caloriesPer100g = 165f, category = FoodCategory.PROTEIN),
            FoodEntity(name = "牛肉", caloriesPer100g = 250f, category = FoodCategory.PROTEIN),
            FoodEntity(name = "鸡蛋", caloriesPer100g = 155f, category = FoodCategory.PROTEIN),
            FoodEntity(name = "三文鱼", caloriesPer100g = 208f, category = FoodCategory.PROTEIN),

            // VEGETABLE
            FoodEntity(name = "西兰花", caloriesPer100g = 34f, category = FoodCategory.VEGETABLE),
            FoodEntity(name = "菠菜", caloriesPer100g = 23f, category = FoodCategory.VEGETABLE),
            FoodEntity(name = "胡萝卜", caloriesPer100g = 41f, category = FoodCategory.VEGETABLE),
            FoodEntity(name = "生菜", caloriesPer100g = 15f, category = FoodCategory.VEGETABLE),

            // FRUIT
            FoodEntity(name = "苹果", caloriesPer100g = 52f, category = FoodCategory.FRUIT),
            FoodEntity(name = "香蕉", caloriesPer100g = 89f, category = FoodCategory.FRUIT),

            // FAT
            FoodEntity(name = "坚果", caloriesPer100g = 607f, category = FoodCategory.FAT),

            // OTHER
            FoodEntity(name = "牛奶", caloriesPer100g = 60f, category = FoodCategory.OTHER),
            FoodEntity(name = "酸奶", caloriesPer100g = 59f, category = FoodCategory.OTHER)
        )

        // 3️⃣ 插入数据库
        foodDao.insertAll(foods)
    }
}