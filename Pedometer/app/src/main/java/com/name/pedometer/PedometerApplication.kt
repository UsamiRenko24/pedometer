package com.name.pedometer

import android.app.Application
import com.name.pedometer.data.database.AppDatabase
import com.name.pedometer.data.initializer.RecipeInitializer
import com.name.pedometer.data.initializer.FoodInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PedometerApplication : Application() {
    // 使用 lazy 确保数据库只在需要时初始化，且在整个应用中只有一个实例
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        // 这里可以进行其他全局初始化操作
        CoroutineScope(Dispatchers.IO).launch {

            val db = AppDatabase.getDatabase(this@PedometerApplication)

            FoodInitializer(db.foodDao()).initIfNeeded()

            if (db.recipeDao().getRecipeCount() == 0) {
                RecipeInitializer(
                    db.recipeDao(),
                    db.foodDao()
                ).insertFatLossRecipe()
            }
        }
    }
}
