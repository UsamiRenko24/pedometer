package com.demo.app.launcher.ui.activity.flashlight;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.demo.app.R;
import com.demo.app.databinding.ActivityFlashlightBinding;
import com.demo.app.launcher.ads.LauncherBackInsertPlaceholder;
import com.demo.app.launcher.ads.LauncherBackPressAds;
import com.demo.app.launcher.ads.LauncherBuiltInAds;
import com.demo.app.launcher.flashlight.FlashlightTorch;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

/** 手电筒主页：关闭是灰按钮，打开后亮光柱和黄色开关。 */
public class FlashlightActivity extends BaseActivity<ActivityFlashlightBinding> {

    private final ActivityResultLauncher<String> cameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    turnOn();
                }
            });

    private FlashlightTorch torch;

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, FlashlightActivity.class);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityFlashlightBinding getViewBinding() {
        return ActivityFlashlightBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.flashlightRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypes());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), false, false);
    }

    @Override
    protected void initViews() {
        torch = new FlashlightTorch(this);
        torch.prepare();
        showState(false);
        LauncherBuiltInAds.show(binding.flashlightAd);
        LauncherBackInsertPlaceholder.preload(this);
        LauncherBackPressAds.install(this, this::finish);
    }

    @Override
    protected void initEvents() {
        binding.flashlightSwitch.setOnClickListener(v -> {
            if (torch.isOn()) {
                torch.turnOff();
                showState(false);
            } else if (hasCameraPermission()) {
                turnOn();
            } else {
                cameraPermission.launch(Manifest.permission.CAMERA);
            }
        });
    }

    @Override
    protected void onPause() {
        if (torch != null && torch.isOn()) {
            torch.turnOff();
            showState(false);
        }
        super.onPause();
    }

    private void turnOn() {
        if (!torch.hasFlash()) {
            Toast.makeText(this, R.string.flashlight_unavailable, Toast.LENGTH_SHORT).show();
            showState(false);
            return;
        }
        try {
            torch.turnOn();
            showState(true);
        } catch (CameraAccessException | RuntimeException ignored) {
            torch.turnOff();
            showState(false);
            Toast.makeText(this, R.string.flashlight_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void showState(boolean on) {
        binding.flashlightBeam.setVisibility(on ? View.VISIBLE : View.GONE);
        binding.flashlightSwitch.setImageResource(on
                ? R.drawable.icon_home_app_flashlight_on
                : R.drawable.icon_home_app_flashlight_off);
        binding.flashlightState.setText(on ? R.string.flashlight_on : R.string.flashlight_off);
        binding.flashlightState.setTextColor(ContextCompat.getColor(this,
                on ? R.color.color_FEB91A : R.color.color_B0B4BE));
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }
}
