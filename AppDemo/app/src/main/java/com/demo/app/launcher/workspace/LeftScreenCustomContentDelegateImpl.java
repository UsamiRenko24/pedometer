package com.demo.app.launcher.workspace;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.demo.app.R;

import m9e778y.LauncherSDK;

/** 负一屏：只放音量旋钮，不带主界面的底栏和设置抽屉。 */
public final class LeftScreenCustomContentDelegateImpl
        implements LauncherSDK.LeftScreenCustomContentDelegate {

    @Nullable
    private LeftScreenHomeController homeController;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View createContentView(@NonNull FragmentActivity activity) {
        return LayoutInflater.from(activity).inflate(R.layout.custom_content_left_screen, null);
    }

    @Override
    public void onViewCreated(@NonNull FragmentActivity activity, @NonNull View view) {
        homeController = new LeftScreenHomeController(activity);
        homeController.bind(view);
    }

    @NonNull
    @Override
    public String getPageDescription(@NonNull FragmentActivity activity) {
        return LeftScreenHomeController.class.getSimpleName();
    }

    @Override
    public void onPageSelected(@NonNull FragmentActivity activity) {
    }

    @Override
    public void onPageDeselected(@NonNull FragmentActivity activity) {
    }

    @Override
    public void onResume(@NonNull FragmentActivity activity) {
        if (homeController != null) {
            homeController.onResume();
        }
    }

    @Override
    public void onPause(@NonNull FragmentActivity activity) {
        if (homeController != null) {
            homeController.onPause();
        }
    }

    @Override
    public void onScrollProgressChanged(float progress) {
    }

    @Override
    public void onDestroy(@NonNull FragmentActivity activity) {
        if (homeController != null) {
            homeController.destroy();
            homeController = null;
        }
    }

    @Override
    public boolean isScrollingAllowed() {
        return true;
    }

    @Override
    public boolean isDrawerSwipeUpAllowed() {
        return false;
    }

    @Override
    public boolean isTopPanelSwipeDownAllowed() {
        return false;
    }

    @Override
    public boolean handleBackPressed(@NonNull FragmentActivity activity) {
        return homeController != null && homeController.handleBackPressed();
    }
}
