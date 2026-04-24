package com.name.pedometer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.name.pedometer.data.database.AppDatabase
import com.name.pedometer.data.initializer.FoodInitializer
import com.name.pedometer.data.initializer.RecipeInitializer
import com.name.pedometer.repository.FoodRepository
import com.name.pedometer.service.StepService
import com.name.pedometer.viewmodel.FoodViewModel
import com.name.pedometer.viewmodel.StepViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.navigation.MainScreen

class MainActivity : ComponentActivity() {

    private lateinit var stepViewModel: StepViewModel
    private lateinit var foodViewModel: FoodViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startStepService()
        } else {
            Toast.makeText(this, "需要权限才能准确计步", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)

        // ✅ FoodViewModel
        foodViewModel = FoodViewModel(
            FoodRepository(db.foodDao())
        )

        // ✅ StepViewModel
        stepViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StepViewModel(application) as T
            }
        })[StepViewModel::class.java]

        // ✅ 权限 & 启动服务
        checkPermissionAndStart()

        // ✅ 只保留 UI 入口（关键）
        setContent {
            MainScreen(
                viewModel = stepViewModel,
                foodViewModel = foodViewModel
            )
        }

        // ✅ 初始化数据库（后台）
        lifecycleScope.launch(Dispatchers.IO) {

            // 初始化食物
            FoodInitializer(db.foodDao()).initIfNeeded()

            // 初始化食谱
            if (db.recipeDao().getRecipeCount() == 0) {
                RecipeInitializer(
                    db.recipeDao(),
                    db.foodDao()
                ).insertFatLossRecipe()
            }
        }
    }

    private fun startStepService() {
        val intent = Intent(this, StepService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        stepViewModel.bindService(this)
    }

    private fun checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED) {
                startStepService()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        } else {
            startStepService()
        }
    }
}