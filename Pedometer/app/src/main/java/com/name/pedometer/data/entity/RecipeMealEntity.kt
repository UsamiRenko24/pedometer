package com.name.pedometer.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.name.pedometer.data.enums.MealType
import androidx.room.Index

@Entity(
    tableName = "recipe_meal",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId", "dayIndex", "mealType"], unique = true),
        Index(value = ["recipeId"])
    ]
)
data class RecipeMealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val recipeId: Int,
    val dayIndex: Int,       // 1~7
    val mealType: MealType   // 早餐/午餐/晚餐
)