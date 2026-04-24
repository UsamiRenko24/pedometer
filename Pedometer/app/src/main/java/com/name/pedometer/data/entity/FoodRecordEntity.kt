package com.name.pedometer.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.name.pedometer.data.enums.MealType

@Entity
data class FoodRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val foodId: Int,
    val grams: Float,
    val mealType: MealType,
    val date: Long
)