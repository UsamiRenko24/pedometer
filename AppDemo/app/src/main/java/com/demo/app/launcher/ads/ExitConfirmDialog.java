package com.demo.app.launcher.ads;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.app.R;

/** 对齐 RC100 退出确认 + Quit Native 占位。 */
public final class ExitConfirmDialog {

    private ExitConfirmDialog() {
    }

    public static void show(@NonNull AppCompatActivity activity, @NonNull Runnable onExit) {
        if (!LauncherAds.shouldShowPlaceholder()) {
            onExit.run();
            return;
        }
        View content = LayoutInflater.from(activity).inflate(R.layout.dialog_exit_confirm, null, false);
        View ad = content.findViewById(R.id.exitConfirm_adView);
        if (ad instanceof LauncherNativeAdPlaceholderView) {
            ((LauncherNativeAdPlaceholderView) ad).refreshVisibility();
        }

        Dialog dialog = new Dialog(activity, R.style.FullNativeAdvertOverlayDialogStyle);
        dialog.setContentView(content);
        dialog.setCancelable(true);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        content.findViewById(R.id.exitConfirm_btnCancel).setOnClickListener(v -> dialog.dismiss());
        content.findViewById(R.id.exitConfirm_btnExit).setOnClickListener(v -> {
            dialog.dismiss();
            onExit.run();
        });
        dialog.show();
    }
}
