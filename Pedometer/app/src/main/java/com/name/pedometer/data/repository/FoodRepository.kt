// D:/Project/Pedometer/app/src/main/java/com/name/pedometer/data/FoodRepository.kt
package com.name.pedometer.data.repository

import com.name.pedometer.data.dao.FoodDao
import com.name.pedometer.data.entity.FoodEntity
import com.name.pedometer.data.enums.FoodCategory
import kotlinx.coroutines.flow.Flow

class FoodInitializer(private val foodDao: FoodDao) {
    fun getAllFoods(): Flow<List<FoodEntity>> {
        return foodDao.getAllFoods()
    }

    fun searchFoods(query: String): Flow<List<FoodEntity>> {
        return foodDao.searchFoods(query)
    }

    suspend fun getFoodByName(name: String): FoodEntity? {
        return foodDao.getFoodByName(name)
    }

}