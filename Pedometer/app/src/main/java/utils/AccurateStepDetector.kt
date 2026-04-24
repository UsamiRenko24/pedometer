package utils

// utils/AccurateStepDetector.kt
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.sqrt

/**
 * 高精度步数检测器
 * 移植自 accurate_step_counter 的核心算法
 */
class AccurateStepDetector(
    private val onStepDetected: (stepCount: Int) -> Unit
) : SensorEventListener {

    // ============ 算法参数 ============
    private val ALPHA = 0.8f              // 低通滤波系数
    private val STEP_THRESHOLD = 12.0f    // 步数检测阈值
    private val MIN_STEP_INTERVAL_MS = 300L   // 最小步间隔（毫秒）
    private val MAX_STEP_INTERVAL_MS = 1200L  // 最大步间隔

    // ============ 状态变量 ============
    private var filteredValue = 0f        // 滤波后的值
    private var lastStepTime = 0L         // 上一步的时间
    private var stepCount = 0              // 累计步数

    // 用于动态阈值调整
    private var peakValues = mutableListOf<Float>()
    private var valleyValues = mutableListOf<Float>()

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                // 1. 计算运动幅度（三轴合成）
                val magnitude = sqrt(x * x + y * y + z * z)

                // 2. 低通滤波（去除高频噪声）
                filteredValue = ALPHA * magnitude + (1 - ALPHA) * filteredValue

                // 3. 动态阈值调整（可选，提升精度）
                val dynamicThreshold = calculateDynamicThreshold()
                val effectiveThreshold = maxOf(STEP_THRESHOLD, dynamicThreshold)

                // 4. 波峰检测
                val now = System.currentTimeMillis()
                val timeSinceLastStep = now - lastStepTime

                if (filteredValue > effectiveThreshold &&
                    timeSinceLastStep > MIN_STEP_INTERVAL_MS) {

                    // 检测到一步
                    stepCount++
                    lastStepTime = now

                    // 记录峰值用于动态阈值
                    peakValues.add(filteredValue)
                    if (peakValues.size > 10) peakValues.removeAt(0)

                    // 回调通知
                    onStepDetected(stepCount)
                }

                // 记录谷值（可选）
                if (filteredValue < effectiveThreshold / 2) {
                    valleyValues.add(filteredValue)
                    if (valleyValues.size > 10) valleyValues.removeAt(0)
                }
            }
        }
    }

    /**
     * 计算动态阈值（根据最近的运动强度调整）
     */
    private fun calculateDynamicThreshold(): Float {
        if (peakValues.isEmpty()) return STEP_THRESHOLD
        // 使用最近峰值的平均值作为动态阈值
        val avgPeak = peakValues.average().toFloat()
        return avgPeak * 0.6f  // 阈值为峰值的60%
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun reset() {
        stepCount = 0
        lastStepTime = 0
        peakValues.clear()
        valleyValues.clear()
    }

    fun getCurrentSteps(): Int = stepCount
}