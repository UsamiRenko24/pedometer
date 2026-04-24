package com.name.pedometer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.name.pedometer.MainActivity
import com.name.pedometer.datastore.StepDataStore
import com.name.pedometer.repository.StepRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class StepService : Service() {
    private lateinit var repository: StepRepository
    private lateinit var dataStore: StepDataStore
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var stepJob: Job? = null

    private val _steps = MutableStateFlow(0L)
    val steps: StateFlow<Long> = _steps.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTrackingState = _isTracking.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val durationState = _duration.asStateFlow()

    private var startTime: Long = 0L
    private val CHANNEL_ID = "step_service_channel"
    private val NOTIFICATION_ID = 1
    private val binder = StepBinder()

    inner class StepBinder : Binder() {
        fun getService(): StepService = this@StepService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        repository = StepRepository(applicationContext)
        dataStore = StepDataStore(applicationContext)
        createNotificationChannel()

        serviceScope.launch {
            val currentDate = LocalDate.now().toString()
            val savedDate = dataStore.lastDateFlow.first()
            val savedSteps = dataStore.stepsFlow.first()

            if (currentDate != savedDate) {
                dataStore.saveSteps(0L)
                dataStore.saveDate(currentDate)
                _steps.value = 0L
            } else {
                _steps.value = savedSteps
            }
        }

        observeTimer()
    }

    private fun startCounting() {
        stepJob?.cancel()
        stepJob = serviceScope.launch {
            repository.getBestStepsFlow().collect { increment ->
                _steps.value += increment
                if (_steps.value % 10 == 0L) {
                    saveToDataStore()
                }
                updateNotification(_steps.value)
            }
        }
    }

    private fun stopCounting() {
        stepJob?.cancel()
        stepJob = null
    }

    private fun observeTimer() {
        serviceScope.launch {
            while (true) {
                if (_isTracking.value) {
                    _duration.value = (System.currentTimeMillis() - startTime) / 1000
                    updateNotification(_steps.value)
                }
                delay(1000)
            }
        }
    }

    fun toggleTracking() {
        _isTracking.value = !_isTracking.value

        if (_isTracking.value) {
            startTime = System.currentTimeMillis() - (_duration.value * 1000)
            startCounting()
        } else {
            stopCounting()
            saveToDataStore()
        }

        updateNotification(_steps.value)
    }

    private fun saveToDataStore() {
        serviceScope.launch { dataStore.saveSteps(_steps.value) }
    }

    private fun updateNotification(steps: Long) {
        val isTrackingNow = _isTracking.value
        val actionText = if (isTrackingNow) "Stop" else "Start"
        // 注意：这里确保你项目里有对应的图标，如果没有，请使用 android.R.drawable.ic_media_pause
        val actionIcon = if (isTrackingNow) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, StepService::class.java).apply {
            action = "ACTION_TOGGLE_STEPS"
        }
        val togglePendingIntent = PendingIntent.getService(
            this, 0, toggleIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val timeStr = String.format("%02d:%02d", _duration.value / 60, _duration.value % 60)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isTrackingNow) "Steps: $steps" else "Pedometer Paused")
            .setContentText("Duration: $timeStr")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .addAction(actionIcon, actionText, togglePendingIntent)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(isTrackingNow)
            .setWhen(startTime)
            .setShowWhen(isTrackingNow)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "Pedometer", NotificationManager.IMPORTANCE_LOW)
            // ✅ 修复：直接使用 getSystemService，不再带 Context. 前缀
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialNotification = createInitialNotification()
        startForeground(NOTIFICATION_ID, initialNotification)

        if (intent?.action == "ACTION_TOGGLE_STEPS") {
            toggleTracking()
        }

        updateNotification(_steps.value)
        return START_STICKY
    }

    private fun createInitialNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pedometer Running")
            .setContentText("Ready to count steps")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveToDataStore()
    }
}
