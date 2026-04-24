package com.name.pedometer.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.name.pedometer.data.enums.FoodCategory

@Entity(tableName = "food_items")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,         // 食物名称
    val caloriesPer100g: Float, // 每100克热量
    val category: FoodCategory      // 分类
)