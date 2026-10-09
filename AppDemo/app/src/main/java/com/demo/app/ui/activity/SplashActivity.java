package com.demo.app.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.demo.app.MainActivity;
import com.demo.app.R;
import com.demo.app.databinding.ActivitySplashBinding;
import com.demo.app.ui.widget.VolumeKnobView;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.CacheUtils;

import m9e778y.LauncherSDK;

import java.lang.ref.WeakReference;

/** 启动页：底部进度条走完后，按是否看过引导进入引导、桌面 0 屏或主页。 */
public class SplashActivity extends BaseActivity<ActivitySplashBinding> {

    private static final long SPLASH_DURATION_MS = 5000L;

    private static WeakReference<SplashActivity> running;

    private ValueAnimator progressAnimator;
    private ObjectAnimator glowAnimator;
    private boolean navigating;

    @Nullable
    @Override
    protected ActivitySplashBinding getViewBinding() {
        return ActivitySplashBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        super.initWindow(rootView);
    }

    @Override
    protected void initViews() {
        running = new WeakReference<>(this);
        new Thread(() -> VolumeKnobView.preload(getApplicationContext()), "knob-preload").start();
        binding.soundWave.setRunning(true);
        startGlowBreath();
        startProgress();
    }

    public static void finishIfRunning() {
        SplashActivity splash = running != null ? running.get() : null;
        if (splash == null || splash.isFinishing()) {
            return;
        }
        splash.finish();
        splash.overridePendingTransition(0, 0);
    }

    /** 光圈从暗到亮，再从亮到暗，循环呼吸。图标本身不动。 */
    private void startGlowBreath() {
        View glow = binding.ivSplashIconGlow;
        glow.setAlpha(0.35f);
        glowAnimator = ObjectAnimator.ofFloat(glow, View.ALPHA, 0.35f, 1f, 0.35f);
        glowAnimator.setDuration(2800L);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        glowAnimator.start();
    }

    private void startProgress() {
        progressAnimator = ValueAnimator.ofInt(0, 1000);
        progressAnimator.setDuration(SPLASH_DURATION_MS);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            if (binding != null) {
                binding.splashProgress.setProgress((int) animation.getAnimatedValue());
            }
        });
        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!navigating && !isFinishing()) {
                    goMain();
                }
            }
        });
        progressAnimator.start();
    }

    private void goMain() {
        if (navigating || isFinishing()) {
            return;
        }
        navigating = true;
        stopSplashMotion();
        Class<?> next;
        if (CacheUtils.isShowGuidePage()) {
            next = GuideActivity.class;
        } else if (CacheUtils.isShowGestureGuide()) {
            AppLog.i("SplashActivity", "go launcher home");
            LauncherSDK.startHomeActivity(this);
            overridePendingTransition(R.anim.splash_page_in, R.anim.splash_page_out);
            return;
        } else {
            next = MainActivity.class;
        }
        AppLog.i("SplashActivity", "go " + next.getSimpleName());
        Intent intent = new Intent(this, next);
        if (next == GuideActivity.class) {
            intent.putExtra(GuideActivity.EXTRA_FROM_SPLASH, true);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.splash_page_in, R.anim.splash_page_out);
    }

    private void stopSplashMotion() {
        if (binding != null) {
            binding.soundWave.setRunning(false);
        }
        if (glowAnimator != null) {
            glowAnimator.cancel();
            glowAnimator = null;
        }
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopSplashMotion();
        if (running != null && running.get() == this) {
            running = null;
        }
        super.onDestroy();
    }
}
