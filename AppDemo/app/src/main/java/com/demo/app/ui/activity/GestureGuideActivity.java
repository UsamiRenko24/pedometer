package com.demo.app.ui.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.demo.app.MainActivity;
import com.demo.app.R;
import com.demo.app.databinding.ActivityGestureGuideBinding;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.CacheUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** 手势说明：右滑进音量、左滑看常用应用、上滑看全部应用。看完后进入主页。 */
public class GestureGuideActivity extends BaseActivity<ActivityGestureGuideBinding> {

    private static final int PAGE_RIGHT = 0;
    private static final int PAGE_LEFT = 1;
    private static final int PAGE_UP = 2;

    private int page = PAGE_RIGHT;
    private float downX;
    private float downY;
    private ObjectAnimator arrowAnimator;

    @Nullable
    @Override
    protected ActivityGestureGuideBinding getViewBinding() {
        return ActivityGestureGuideBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initViews() {
        bindDate();
        showPage(PAGE_RIGHT);
        SplashActivity.finishIfRunning();
    }

    @Override
    protected void initEvents() {
        binding.btnGesture.setOnClickListener(v -> advance());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (page > PAGE_RIGHT) {
                    showPage(page - 1);
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });
    }

    private void bindDate() {
        Calendar calendar = Calendar.getInstance();
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        String month = new SimpleDateFormat("MMM", locale).format(calendar.getTime());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayText = String.valueOf(day);
        if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
            dayText = dayText + daySuffix(day);
        }
        binding.tvDate.setText(getString(R.string.gesture_date, month, dayText));
    }

    private void showPage(int index) {
        page = index;
        int title;
        int subtitle;
        int icon;
        float toX = 0f;
        float toY = 0f;
        float distance = 16f * getResources().getDisplayMetrics().density;
        if (page == PAGE_LEFT) {
            title = R.string.gesture_swipe_left_title;
            subtitle = R.string.gesture_swipe_left_desc;
            icon = R.drawable.gesture_guide_left;
            toX = -distance;
        } else if (page == PAGE_UP) {
            title = R.string.gesture_swipe_up_title;
            subtitle = R.string.gesture_swipe_up_desc;
            icon = R.drawable.gesture_guide_up;
            toY = -distance;
        } else {
            title = R.string.gesture_swipe_right_title;
            subtitle = R.string.gesture_swipe_right_desc;
            icon = R.drawable.gesture_guide_right;
            toX = distance;
        }
        binding.tvTitle.setText(title);
        binding.tvSubtitle.setText(subtitle);
        binding.btnGesture.setImageResource(icon);
        binding.btnGesture.setContentDescription(getString(title));
        startArrowNudge(toX, toY);
    }

    private void startArrowNudge(float toX, float toY) {
        if (arrowAnimator != null) {
            arrowAnimator.cancel();
        }
        binding.btnGesture.setTranslationX(0f);
        binding.btnGesture.setTranslationY(0f);
        View arrow = binding.btnGesture;
        arrowAnimator = ObjectAnimator.ofFloat(arrow, page == PAGE_UP ? View.TRANSLATION_Y : View.TRANSLATION_X, 0f, page == PAGE_UP ? toY : toX);
        arrowAnimator.setDuration(700L);
        arrowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        arrowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        arrowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        arrowAnimator.start();
    }

    private void advance() {
        AppLog.i("GestureGuideActivity", "advance from " + page);
        if (page >= PAGE_UP) {
            finishGestureGuide();
            return;
        }
        showPage(page + 1);
    }

    private void finishGestureGuide() {
        AppLog.i("GestureGuideActivity", "gesture guide finished");
        CacheUtils.setShowGestureGuide(false);
        Intent main = new Intent(this, MainActivity.class);
        main.putExtra(MainActivity.EXTRA_REFRESH_LANGUAGE, true);
        startActivity(main);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                float dx = ev.getX() - downX;
                float dy = ev.getY() - downY;
                if (matchesGesture(dx, dy)) {
                    advance();
                    return true;
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean matchesGesture(float dx, float dy) {
        float threshold = 72f * getResources().getDisplayMetrics().density;
        if (page == PAGE_RIGHT) {
            return dx > threshold && Math.abs(dx) > Math.abs(dy);
        }
        if (page == PAGE_LEFT) {
            return dx < -threshold && Math.abs(dx) > Math.abs(dy);
        }
        return dy < -threshold && Math.abs(dy) > Math.abs(dx);
    }

    private String daySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    @Override
    protected void onDestroy() {
        if (arrowAnimator != null) {
            arrowAnimator.cancel();
            arrowAnimator = null;
        }
        super.onDestroy();
    }
}
