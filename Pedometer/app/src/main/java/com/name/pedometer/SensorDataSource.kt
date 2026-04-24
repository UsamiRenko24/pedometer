package com.name.pedometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.name.pedometer.repository.StepRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SensorDataSource(private val context: Context) : StepRepository.StepDataSource {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    // ✅ 业内标准：使用 callbackFlow 封装回调接口
    override fun getStepsFlow(): Flow<Long> = callbackFlow {
        var steps = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
                    steps++
                    // 往水管里发数据
                    trySend(steps)
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }

        // 只有当有人开始 collect 这个 Flow 时，传感器才会注册
        sensor?.let {
            sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_FASTEST)
        }

        // ✅ 核心：当观察者（如 Service）销毁时，自动关闭传感器，永不泄露
        awaitClose {
            sensorManager?.unregisterListener(listener)
        }
    }

    override suspend fun sync() { /* 硬件数据不需要同步 */ }
}