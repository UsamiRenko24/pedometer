package com.demo.app.launcher.ads;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.demo.app.R;

/** 对齐 RC100 {@code SplashFeedView}：进入内置页前的信息流过渡占位。 */
public final class SplashFeedPlaceholder {

    private static final long DEFAULT_MS = 650L;
    private static final long FIRST_GUIDE_MS = 850L;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private SplashFeedPlaceholder() {
    }

    public static void run(@NonNull Activity activity, @NonNull Runnable onNavigate) {
        run(activity, null, onNavigate);
    }

    /** 引导完成：对齐 RC100 {@code SplashFeedView.startForFirstSplash}。 */
    public static void runForFirstGuide(@NonNull Activity activity, @NonNull Runnable onNavigate) {
        runInternal(activity, null, onNavigate, FIRST_GUIDE_MS);
    }

    public static void runFromContext(
            @NonNull Context context,
            @Nullable CharSequence title,
            @NonNull Runnable onNavigate) {
        Activity activity = findActivity(context);
        if (activity == null) {
            onNavigate.run();
            return;
        }
        run(activity, title, onNavigate);
    }

    public static void runFromContext(@NonNull Context context, @NonNull Runnable onNavigate) {
        runFromContext(context, null, onNavigate);
    }

    @Nullable
    private static Activity findActivity(@NonNull Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) {
                return (Activity) current;
            }
            current = ((ContextWrapper) current).getBaseContext();
        }
        return current instanceof Activity ? (Activity) current : null;
    }

    public static void run(
            @NonNull Activity activity,
            @Nullable CharSequence title,
            @NonNull Runnable onNavigate) {
        runInternal(activity, title, onNavigate, DEFAULT_MS);
    }

    private static void runInternal(
            @NonNull Activity activity,
            @Nullable CharSequence title,
            @NonNull Runnable onNavigate,
            long delayMs) {
        if (!LauncherAds.shouldShowPlaceholder() || activity.isFinishing()) {
            onNavigate.run();
            return;
        }
        View content = LayoutInflater.from(activity).inflate(R.layout.overlay_splash_feed, null, false);
        TextView titleView = content.findViewById(R.id.splash_feed_title);
        if (title != null && title.length() > 0) {
            titleView.setText(title);
            titleView.setVisibility(View.VISIBLE);
        }
        View nativeSlot = content.findViewById(R.id.splash_feed_native);
        if (nativeSlot instanceof LauncherNativeAdPlaceholderView) {
            ((LauncherNativeAdPlaceholderView) nativeSlot).refreshVisibility();
        }

        Dialog dialog = new Dialog(activity, R.style.SplashFeedOverlayDialogStyle);
        dialog.setContentView(content);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
        dialog.show();

        MAIN.postDelayed(() -> {
            if (!activity.isFinishing()) {
                dialog.dismiss();
            }
            if (activity instanceof FragmentActivity && ((FragmentActivity) activity).isDestroyed()) {
                return;
            }
            if (!activity.isFinishing()) {
                onNavigate.run();
            }
        }, delayMs);
    }
}
