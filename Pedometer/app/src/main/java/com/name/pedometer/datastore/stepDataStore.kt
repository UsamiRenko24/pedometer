package com.name.pedometer.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.* // 确保导入的是这个 core 下的全部
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 定义 DataStore 的扩展属性
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "step_settings")

class StepDataStore(private val context: Context) {
    private var stepsSinceLastSave = 0
    // 1. 定义 Key（就像数据库的列名）
    companion object {
        val CURRENT_STEPS = longPreferencesKey("current_steps")
        val INITIAL_STEPS = longPreferencesKey("initial_steps")
        val LAST_DATE = stringPreferencesKey("last_date")
    }

    // 2. 提供给外部的“观察流” (ViewModel 会用到)
    // 只要硬盘里的数据变了，这个 Flow 就会自动发出新值
    val stepsFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_STEPS] ?: 0L
    }

    // 3. 存储步数的函数
    suspend fun saveSteps(steps: Long) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_STEPS] = steps
        }
    }

    // 4. 存储基准值的函数 (处理重启逻辑)
    suspend fun saveInitialSteps(steps: Long) {
        context.dataStore.edit { prefs ->
            prefs[INITIAL_STEPS] = steps
        }
    }

    // 5. 获取上次保存的日期 (用于零点清零)
    val lastDateFlow: Flow<String> = context.dataStore.data.map { it[LAST_DATE] ?: "" }

    suspend fun saveDate(date: String) {
        context.dataStore.edit { it[LAST_DATE] = date }
    }

}