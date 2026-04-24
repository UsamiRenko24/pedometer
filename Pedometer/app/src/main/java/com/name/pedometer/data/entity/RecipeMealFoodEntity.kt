package com.name.pedometer.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_meal_food",
    foreignKeys = [
        ForeignKey(
            entity = RecipeMealEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeMealId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"]
        )
    ],
    indices = [
        Index(value = ["recipeMealId", "foodId"], unique = true),
        Index(value = ["recipeMealId"]),
        Index(value = ["foodId"])
    ]

)
data class RecipeMealFoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val recipeMealId: Int,
    val foodId: Int,

    val weight: Float
)