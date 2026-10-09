package com.demo.app.launcher.ui.activity.folder;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.demo.app.launcher.ads.LauncherSplashActivities;
import com.demo.app.databinding.ActivityFolderLauncherBinding;
import com.demo.app.utils.SystemUiHelper;

/** ??????????????????????????????*/
public class FolderLauncherActivity extends FolderPageActivity<ActivityFolderLauncherBinding> {

    private static final long SPLASH_MS = 2000L;

    private ValueAnimator progressAnimator;

    public static void start(Context context) {
        Intent intent = new Intent(context, FolderLauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityFolderLauncherBinding getViewBinding() {
        return ActivityFolderLauncherBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        super.initWindow(rootView);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets bars = insets.getInsets(SystemUiHelper.contentInsetTypes());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void initViews() {
        LauncherSplashActivities.onCreate(this);
        progressAnimator = ValueAnimator.ofInt(0, 1000);
        progressAnimator.setDuration(SPLASH_MS);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(animation ->
                binding.folderSplashProgress.setProgress((int) animation.getAnimatedValue()));
        progressAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (isFinishing()) {
                    return;
                }
                LauncherSplashActivities.beforeOpenHome(FolderLauncherActivity.this);
                FolderActivity.start(FolderLauncherActivity.this);
                finish();
            }
        });
        progressAnimator.start();
    }

    @Override
    protected void onDestroy() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        super.onDestroy();
    }
}
