package com.demo.app.launcher.ui.activity.weather;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.demo.app.databinding.ActivityWeatherLauncherBinding;
import com.demo.app.launcher.ads.LauncherSplashActivities;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

/** 天气启动页。进度走完后进入首页。 */
public class WeatherLauncherActivity extends BaseActivity<ActivityWeatherLauncherBinding> {

    private ValueAnimator progressAnimator;
    private boolean openedHome;

    public static void start(@NonNull Context context) {
        context.startActivity(createIntent(context));
    }

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        Intent intent = new Intent(context, WeatherLauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Nullable
    @Override
    protected ActivityWeatherLauncherBinding getViewBinding() {
        return ActivityWeatherLauncherBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.weatherSplashContent, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), true, true);
    }

    @Override
    protected void initViews() {
        LauncherSplashActivities.onCreate(this);
        progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(1400L);
        progressAnimator.addUpdateListener(animation ->
                binding.weatherSplashProgress.setProgress((int) animation.getAnimatedValue()));
        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                openHome();
            }
        });
        progressAnimator.start();
    }

    private void openHome() {
        if (openedHome || isFinishing()) {
            return;
        }
        openedHome = true;
        LauncherSplashActivities.beforeOpenHome(this);
        startActivity(new Intent(this, WeatherActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (progressAnimator != null) {
            progressAnimator.removeAllListeners();
            progressAnimator.cancel();
        }
        super.onDestroy();
    }
}
