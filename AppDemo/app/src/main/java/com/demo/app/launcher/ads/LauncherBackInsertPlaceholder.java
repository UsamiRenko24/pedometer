package com.demo.app.launcher.ads;

import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.demo.app.R;

/** 对齐 RC100 {@code AdvertBackHelper} / {@code TopOnUtils.showBackInsertAdvert} 占位。 */
public final class LauncherBackInsertPlaceholder {

    private static final long SHOW_MS = 750L;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private LauncherBackInsertPlaceholder() {
    }

    public static void preload(@NonNull LifecycleOwner lifecycleOwner) {
        if (!LauncherAds.shouldShowPlaceholder()) {
            return;
        }
        // Reserved for TopOnUtils.preloadBackInsert.
    }

    public static boolean isReady() {
        return LauncherAds.shouldShowPlaceholder();
    }

    public static void show(@NonNull AppCompatActivity activity, @NonNull Runnable onClose) {
        if (!isReady() || activity.isFinishing()) {
            onClose.run();
            return;
        }
        View content = LayoutInflater.from(activity).inflate(R.layout.overlay_back_insert, null, false);
        Dialog dialog = new Dialog(activity, R.style.FullNativeAdvertOverlayDialogStyle);
        dialog.setContentView(content);
        dialog.setCancelable(true);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
        final boolean[] closed = {false};
        Runnable finishOnce = () -> {
            if (closed[0]) {
                return;
            }
            closed[0] = true;
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (!activity.isFinishing()) {
                onClose.run();
            }
        };
        content.setOnClickListener(v -> finishOnce.run());
        dialog.show();

        MAIN.postDelayed(finishOnce::run, SHOW_MS);
    }

    public static void finishWithBackInsert(@NonNull AppCompatActivity activity) {
        show(activity, activity::finish);
    }
}
