package com.name.pedometer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.name.pedometer.data.entity.FoodEntity
import com.name.pedometer.data.entity.FoodRecordEntity
import com.name.pedometer.data.enums.MealType
import com.name.pedometer.repository.FoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

class FoodViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    // ✅ 1. foods
    val foods: StateFlow<List<FoodEntity>> =
        repository.getAllFoods()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // ✅ 2. todayRecords
    val todayRecords: StateFlow<List<FoodRecordEntity>> =
        repository.getTodayRecords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // ✅ 3. combine（最后）
    val mealCalories: StateFlow<Map<MealType, Float>> =
        combine(todayRecords, foods) { records, foods ->

            records.groupBy { it.mealType }.mapValues { entry ->

                entry.value.sumOf { record ->

                    val food = foods.find { it.id == record.foodId }
                    val calPer100g = food?.caloriesPer100g ?: 0f

                    (record.grams * calPer100g / 100f).toDouble()
                }.toFloat()
            }

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun addFoodRecord(
        food: FoodEntity,
        grams: Float,
        mealType: MealType
    ) {
        viewModelScope.launch {
            repository.insertRecord(food, grams, mealType)
        }
    }
    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteRecord(id)
        }
    }

}