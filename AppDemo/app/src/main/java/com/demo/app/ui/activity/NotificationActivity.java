package com.demo.app.ui.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.demo.app.databinding.ActivityNotificationBinding;
import com.demo.app.utils.AppLog;

/** 通知说明页。开启按钮申请通知权限，系统开关关闭时进入通知设置。 */
public class NotificationActivity extends BaseActivity<ActivityNotificationBinding> {

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted ->
                    AppLog.i("NotificationActivity", granted ? "notification granted" : "notification denied"));

    @Nullable
    @Override
    protected ActivityNotificationBinding getViewBinding() {
        return ActivityNotificationBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initViews() {
        binding.tvNotifyHeadline.post(this::applyHeadlineGradient);
    }

    private void applyHeadlineGradient() {
        float width = binding.tvNotifyHeadline.getWidth();
        if (width <= 0f) {
            return;
        }
        Shader shader = new LinearGradient(
                0f,
                0f,
                width,
                0f,
                0xFF0066FF,
                0xFF0098FF,
                Shader.TileMode.CLAMP);
        binding.tvNotifyHeadline.getPaint().setShader(shader);
        binding.tvNotifyHeadline.invalidate();
    }

    @Override
    protected void initEvents() {
        binding.btnNotifyBack.setOnClickListener(v -> finish());
        binding.btnEnableNotifications.setOnClickListener(v -> enableNotifications());
    }

    private void enableNotifications() {
        if (needsNotificationPermission()) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
                AppLog.i("NotificationActivity", "notification settings not found");
            }
        }
    }

    private boolean needsNotificationPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED;
    }
}
