package com.name.pedometer.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.name.pedometer.service.StepService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StepViewModel(application: Application) : AndroidViewModel(application) {// 1. 本地变量：UI 直接观察这些
    private val _steps = MutableStateFlow(0L)
    val steps = _steps.asStateFlow()

    var isTracking by mutableStateOf(false) // Compose 观察这个
    var duration by mutableStateOf(0L)      // Compose 观察这个

    private var stepService: StepService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StepService.StepBinder
            stepService = binder.getService()
            observeServiceSteps()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            stepService = null
        }
    }

    fun bindService(context: Context) {
        val intent = Intent(context, StepService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeServiceSteps() {
        val service = stepService ?: return
        viewModelScope.launch {
            // 钩住步数水管
            launch { service.steps.collect { _steps.value = it } }
            // 钩住时长水管
            launch { service.durationState.collect { duration = it } }
            // 钩住开关水管
            launch { service.isTrackingState.collect { isTracking = it } }
        }
    }

    fun toggleTracking() {
        // ✅ 转发指令：ViewModel 自己不动手，让 Service 去切开关
        stepService?.toggleTracking()
    }

    // ✅ 把计算逻辑还给 ViewModel
    fun getCalories(steps: Long): Int = (steps * 0.04).toInt()
    fun getDistance(steps: Long): Float = steps * 0.7f / 1000f

    override fun onCleared() {
        super.onCleared()
        // ✅ 拔掉插头：防止内存泄漏
        try {
            application.unbindService(serviceConnection)
        } catch (e: Exception) { }
    }
}