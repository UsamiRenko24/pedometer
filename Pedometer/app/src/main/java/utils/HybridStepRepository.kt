package utils

// utils/HybridStepRepository.kt
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HybridStepRepository(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    // 步数状态流
    private val _steps = MutableStateFlow(0L)
    val steps: StateFlow<Long> = _steps

    // 算法组件
    private lateinit var accurateDetector: AccurateStepDetector
    private var systemSteps = 0L
    private var initialSteps = 0L

    // 置信度参数
    private var systemConfidence = 1.0f   // 系统传感器置信度
    private var lastSystemStepCount = 0L
    private var lastAccelStepCount = 0

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        initHybridDetection()
    }

    private fun initHybridDetection() {
        // 1. 初始化加速度算法检测器
        accurateDetector = AccurateStepDetector { accelSteps ->
            scope.launch {
                // 收到加速度算法检测到的步数
                val currentSystemSteps = systemSteps
                val finalSteps = mergeSteps(currentSystemSteps, accelSteps.toLong())
                _steps.emit(finalSteps)
            }
        }

        // 2. 注册系统计步传感器（主数据源）
        stepCounterSensor?.let {
            sensorManager.registerListener(
                systemStepListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        // 3. 注册加速度传感器（辅助验证）
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(
                accurateDetector,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /**
     * 系统传感器监听器
     */
    private val systemStepListener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent?) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    val totalSteps = it.values[0].toLong()

                    if (initialSteps == 0L) {
                        initialSteps = totalSteps
                    } else {
                        systemSteps = totalSteps - initialSteps

                        // 计算置信度（基于步频是否合理）
                        systemConfidence = calculateConfidence()

                        // 与加速度算法结果融合
                        val finalSteps = mergeSteps(systemSteps, accurateDetector.getCurrentSteps().toLong())
                        scope.launch {
                            _steps.emit(finalSteps)
                        }
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) = Unit
    }

    /**
     * 步数融合算法（核心）
     */
    private fun mergeSteps(system: Long, accel: Long): Long {
        return when {
            // 高置信度：完全信任系统传感器
            systemConfidence > 0.8f -> system

            // 中置信度：加权平均
            systemConfidence > 0.5f -> {
                ((system * 0.7) + (accel * 0.3)).toLong()
            }

            // 低置信度：信任加速度算法
            else -> {
                maxOf(system, accel)
            }
        }
    }

    /**
     * 计算系统传感器的置信度
     */
    private fun calculateConfidence(): Float {
        val systemIncrement = systemSteps - lastSystemStepCount
        lastSystemStepCount = systemSteps

        // 如果步数增量在合理范围内（每秒0.5-3步），高置信度
        return if (systemIncrement in 1..3) 0.9f else 0.5f
    }

    fun start() {
        // 已在 init 中注册，无需额外操作
    }

    fun stop() {
        sensorManager.unregisterListener(systemStepListener)
        sensorManager.unregisterListener(accurateDetector)
        scope.cancel()
    }
}