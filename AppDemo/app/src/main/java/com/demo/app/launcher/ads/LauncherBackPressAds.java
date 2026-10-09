package com.demo.app.launcher.ads;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/** 内置页返回：退出确认 + Quit Native 占位。 */
public final class LauncherBackPressAds {

    private LauncherBackPressAds() {
    }

    public static void install(@NonNull AppCompatActivity activity, @NonNull Runnable finishAction) {
        activity.getOnBackPressedDispatcher().addCallback(activity, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ExitConfirmDialog.show(activity, finishAction);
            }
        });
    }
}
