package com.name.pedometer.data.database

import android.content.Context
import androidx.room.*
import com.name.pedometer.data.entity.*
import com.name.pedometer.data.dao.*

@Database(
    entities = [
        FoodEntity::class,
        RecipeEntity::class,
        RecipeMealEntity::class,
        RecipeMealFoodEntity::class,
        FoodRecordEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().build()

                INSTANCE = instance
                instance
            }
        }
    }
}