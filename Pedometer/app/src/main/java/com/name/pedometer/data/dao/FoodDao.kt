package com.name.pedometer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.name.pedometer.data.entity.FoodEntity
import com.name.pedometer.data.entity.FoodRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    // UI 用（Flow）
    @Query("SELECT * FROM food_items")
    fun getAllFoods(): Flow<List<FoodEntity>>

    // 初始化用（同步）
    @Query("SELECT * FROM food_items")
    suspend fun getAll(): List<FoodEntity>

    // 根据名字查（核心）
    @Query("SELECT * FROM food_items WHERE name = :name LIMIT 1")
    suspend fun getFoodByName(name: String): FoodEntity?

    // 搜索
    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%'")
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodEntity>)

    // 单条插入（关键）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: FoodEntity): Long

    @Insert
    suspend fun insertRecord(record: FoodRecordEntity)

    @Query("SELECT * FROM FoodRecordEntity WHERE date BETWEEN :start AND :end")
    fun getTodayRecords(start: Long, end: Long): Flow<List<FoodRecordEntity>>

    @Query("SELECT * FROM FoodRecordEntity")
    fun getAllRecords(): Flow<List<FoodRecordEntity>>

    @Query("DELETE FROM FoodRecordEntity WHERE id = :id")
    suspend fun deleteRecord(id: Int)
}