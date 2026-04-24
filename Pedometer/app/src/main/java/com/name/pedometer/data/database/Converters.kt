package com.name.pedometer.data.database

import androidx.room.TypeConverter
import com.name.pedometer.data.enums.FoodCategory
import com.name.pedometer.data.enums.MealType

class Converters {

    @TypeConverter
    fun fromCategory(category: FoodCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): FoodCategory {
        return FoodCategory.valueOf(value)
    }
}