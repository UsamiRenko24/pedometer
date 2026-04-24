package com.name.pedometer.repository

import com.name.pedometer.data.dao.FoodDao
import com.name.pedometer.data.entity.FoodEntity
import com.name.pedometer.data.entity.FoodRecordEntity
import com.name.pedometer.data.enums.MealType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FoodRepository(
    private val foodDao: FoodDao
) {

    fun getAllFoods(): Flow<List<FoodEntity>> {
        return foodDao.getAllFoods()
    }

    // ⭐ 添加食物记录
    suspend fun insertRecord(
        food: FoodEntity,
        grams: Float,
        mealType: MealType
    ) {
        val record = FoodRecordEntity(
            foodId = food.id,
            grams = grams,
            mealType = mealType,
            date = System.currentTimeMillis()
        )

        foodDao.insertRecord(record)
    }
    fun getTodayRecords(): Flow<List<FoodRecordEntity>> {
        val start = getStartOfDay()
        val end = getEndOfDay()
        return foodDao.getTodayRecords(start, end)
    }
    fun getStartOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        return cal.timeInMillis
    }

    suspend fun deleteRecord(id: Int) {
        foodDao.deleteRecord(id)
    }
}