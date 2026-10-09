package com.name.pedometer

import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Empty activity to handle the Health Connect permissions intent.
 * This is required by the Health Connect SDK.
 */
class HealthConnectPermissionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This activity doesn't need to do anything, it just needs to exist
        // to handle the ACTION_HEALTH_PERMISSIONS_REQUEST intent.
        finish()
    }
}