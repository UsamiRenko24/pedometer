package com.name.pedometer.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class StepRepository(private val context: Context) {
    private val sensorSource = SensorDataSource(context)
    private val healthSource = HealthConnectDataSource(context)

    interface StepDataSource {
        fun getStepsFlow(): Flow<Long>
        suspend fun sync() // 用于同步外部数据
    }


    class SensorDataSource(private val context: Context) : StepDataSource {
        private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        private val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        override fun getStepsFlow(): Flow<Long> = callbackFlow {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
                        trySend(1L)
                    }
                }

                override fun onAccuracyChanged(s: Sensor?, a: Int) {}
            }
            sensor?.let {
                sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_FASTEST)
            }
            // ✅ 工业级核心：当外部停止 collect 时，自动注销传感器，彻底防止泄露
            awaitClose {
                sensorManager?.unregisterListener(listener)
            }
        }
        override suspend fun sync() { }
    }

    // 2. Health Connect 专家（留给以后实现）
    class HealthConnectDataSource(private val context: Context) : StepDataSource {
        override fun getStepsFlow(): Flow<Long> = flow { /* ... */ }
        override suspend fun sync() { /* 同步逻辑 */ }
    }

    fun getBestStepsFlow(): Flow<Long> {
        // 业内高级做法：这里可以用 merge 或 combine 将两个数据源合并
        // 现在我们先简单返回传感器的
        return sensorSource.getStepsFlow()
    }
}