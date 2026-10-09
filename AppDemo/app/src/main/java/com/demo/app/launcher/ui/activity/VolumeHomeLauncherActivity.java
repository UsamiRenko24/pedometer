package com.demo.app.launcher.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.MainActivity;
import com.demo.app.R;
import com.demo.app.databinding.ActivityHomeLauncherBinding;
import com.demo.app.launcher.ads.LauncherSplashPlaceholder;
import com.demo.app.ui.activity.BaseActivity;

/**
 * 0 屏进入本体：对齐 RC100 {@code HomeLauncherActivity} 的开屏容器与 Banner 位（占位）。
 */
public final class VolumeHomeLauncherActivity extends BaseActivity<ActivityHomeLauncherBinding> {

    private static final long PROGRESS_MS = 1200L;

    private ValueAnimator progressAnimator;
    private boolean opened;

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, VolumeHomeLauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityHomeLauncherBinding getViewBinding() {
        return ActivityHomeLauncherBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initViews() {
        LauncherSplashPlaceholder.show(this, R.id.pdfLauncher_flSplash);
        // 屏蔽首页过渡页底部 banner 占位（不展示底部广告）
        View banner = findViewById(R.id.main_flBanner);
        if (banner != null) {
            banner.setVisibility(View.GONE);
        }

        progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(PROGRESS_MS);
        progressAnimator.addUpdateListener(a ->
                binding.pdfLauncherIndicator.setProgress((int) a.getAnimatedValue()));
        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                openMain();
            }
        });
        progressAnimator.start();
    }

    private void openMain() {
        if (opened || isFinishing()) {
            return;
        }
        opened = true;
        LauncherSplashPlaceholder.dismiss(this, R.id.pdfLauncher_flSplash);
        startActivity(new Intent(this, MainActivity.class));
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
