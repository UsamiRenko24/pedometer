package com.demo.app.launcher.guide;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;
import com.demo.app.utils.CacheUtils;

import m9e778y.LauncherSDK;

/**
 * 0 屏上的三步手势蒙层：右滑进音量、左滑看下一屏、上滑看全部应用。
 * 每一步关掉后才切页，回到 0 屏再出现下一步。
 */
public final class Screen0GestureGuides {

    private static final int STEP_RIGHT = 0;
    private static final int STEP_LEFT = 1;
    private static final int STEP_UP = 2;

    @Nullable
    private View overlay;
    @Nullable
    private ObjectAnimator nudge;
    private boolean showing;

    public void scheduleIfNeeded(@NonNull View workspace, @NonNull ViewGroup host) {
        if (showing || overlay != null || !CacheUtils.isShowGestureGuide()) {
            return;
        }
        int step = CacheUtils.getGestureGuideStep();
        if (step > STEP_UP) {
            CacheUtils.setShowGestureGuide(false);
            return;
        }
        workspace.post(() -> {
            if (showing || !workspace.isAttachedToWindow() || workspace.getWidth() <= 0) {
                return;
            }
            show(host, CacheUtils.getGestureGuideStep());
        });
    }

    public void release() {
        cancelNudge();
        if (overlay != null) {
            ViewGroup parent = (ViewGroup) overlay.getParent();
            if (parent != null) {
                parent.removeView(overlay);
            }
            overlay = null;
        }
        showing = false;
    }

    private void show(@NonNull ViewGroup host, int step) {
        if (step > STEP_UP || overlay != null) {
            return;
        }
        View view = LayoutInflater.from(host.getContext())
                .inflate(R.layout.layout_screen0_gesture_overlay, host, false);
        ImageView icon = view.findViewById(R.id.screen0_gesture_icon);
        TextView title = view.findViewById(R.id.screen0_gesture_title);
        TextView desc = view.findViewById(R.id.screen0_gesture_desc);
        if (step == STEP_LEFT) {
            icon.setImageResource(R.drawable.gesture_guide_left);
            title.setText(R.string.gesture_swipe_left_title);
            desc.setText(R.string.gesture_swipe_left_desc);
        } else if (step == STEP_UP) {
            icon.setImageResource(R.drawable.gesture_guide_up);
            title.setText(R.string.gesture_swipe_up_title);
            desc.setText(R.string.gesture_swipe_up_desc);
        } else {
            icon.setImageResource(R.drawable.gesture_guide_right);
            title.setText(R.string.gesture_swipe_right_title);
            desc.setText(R.string.gesture_swipe_right_desc);
        }
        view.setOnTouchListener(new View.OnTouchListener() {
            private float downX;
            private float downY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    downX = event.getX();
                    downY = event.getY();
                } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    float dx = event.getX() - downX;
                    float dy = event.getY() - downY;
                    float threshold = 72f * v.getResources().getDisplayMetrics().density;
                    float tap = 24f * v.getResources().getDisplayMetrics().density;
                    boolean gestured = Math.hypot(dx, dy) < tap || matches(step, dx, dy, threshold);
                    if (gestured) {
                        complete(step);
                    }
                }
                return true;
            }
        });
        overlay = view;
        showing = true;
        host.addView(view);
        startNudge(icon, step);
    }

    private void complete(int step) {
        release();
        if (step == STEP_RIGHT) {
            CacheUtils.setGestureGuideStep(STEP_LEFT);
            LauncherSDK.openLeftScreen(true);
        } else if (step == STEP_LEFT) {
            CacheUtils.setGestureGuideStep(STEP_UP);
            LauncherSDK.openNextWorkspacePage(true);
        } else {
            CacheUtils.setGestureGuideStep(STEP_UP + 1);
            CacheUtils.setShowGestureGuide(false);
            LauncherSDK.openAllAppsView(true);
        }
    }

    private boolean matches(int step, float dx, float dy, float threshold) {
        if (step == STEP_RIGHT) {
            return dx > threshold && Math.abs(dx) > Math.abs(dy);
        }
        if (step == STEP_LEFT) {
            return dx < -threshold && Math.abs(dx) > Math.abs(dy);
        }
        return dy < -threshold && Math.abs(dy) > Math.abs(dx);
    }

    private void startNudge(@NonNull View icon, int step) {
        cancelNudge();
        float distance = 16f * icon.getResources().getDisplayMetrics().density;
        float to = step == STEP_RIGHT ? distance : -distance;
        nudge = step == STEP_UP
                ? ObjectAnimator.ofFloat(icon, View.TRANSLATION_Y, 0f, to)
                : ObjectAnimator.ofFloat(icon, View.TRANSLATION_X, 0f, to);
        nudge.setDuration(700L);
        nudge.setRepeatCount(ValueAnimator.INFINITE);
        nudge.setRepeatMode(ValueAnimator.REVERSE);
        nudge.setInterpolator(new AccelerateDecelerateInterpolator());
        nudge.start();
    }

    private void cancelNudge() {
        if (nudge != null) {
            nudge.cancel();
            nudge = null;
        }
    }
}
