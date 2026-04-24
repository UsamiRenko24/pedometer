package com.name.pedometer.data.enums

import androidx.annotation.StringRes
import com.name.pedometer.R

enum class FoodCategory {
    CARB,        // 主食/碳水
    PROTEIN,     // 蛋白质
    FAT,         // 脂肪类
    VEGETABLE,   // 蔬菜
    FRUIT,       // 水果
    OTHER        // 其他
}

/**
 * 扩展函数：将枚举映射为字符串资源 ID
 */
@StringRes
fun FoodCategory.toResId(): Int {
    return when (this) {
        FoodCategory.CARB -> R.string.food_category_carb
        FoodCategory.PROTEIN -> R.string.food_category_protein
        FoodCategory.FAT -> R.string.food_category_fat
        FoodCategory.VEGETABLE -> R.string.food_category_vegetable
        FoodCategory.FRUIT -> R.string.food_category_fruit
        FoodCategory.OTHER -> R.string.food_category_other
    }
}
