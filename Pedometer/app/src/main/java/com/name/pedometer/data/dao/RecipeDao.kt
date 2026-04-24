package com.name.pedometer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.name.pedometer.data.entity.RecipeEntity
import com.name.pedometer.data.entity.RecipeMealEntity
import com.name.pedometer.data.entity.RecipeMealFoodEntity

@Dao
interface RecipeDao {

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert
    suspend fun insertMeal(meal: RecipeMealEntity): Long

    @Insert
    suspend fun insertMealFoods(mealFoods: List<RecipeMealFoodEntity>)

    @Query("SELECT COUNT(*) FROM recipe")
    suspend fun getRecipeCount(): Int
}