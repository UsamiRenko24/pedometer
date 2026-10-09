package com.demo.app.launcher.ui.adapter.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;

/** 日程行：往左滑才露出右侧删除图标，上下滑交给列表。 */
public final class CalendarEventRow extends FrameLayout {

    public interface Callback {
        void onSettled(boolean revealed);
    }

    private View card;
    private float downX;
    private float downY;
    private float startX;
    private boolean dragging;
    private int slop;
    @Nullable
    private Callback callback;

    public CalendarEventRow(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        slop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    public void reset() {
        View target = card();
        target.animate().cancel();
        target.setTranslationX(0f);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        View target = card();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                startX = target.getTranslationX();
                dragging = false;
                return false;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - downX;
                float dy = event.getRawY() - downY;
                if (!dragging && Math.abs(dx) > slop && Math.abs(dx) > Math.abs(dy) && dx < 0f) {
                    dragging = true;
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                }
                if (!dragging && startX < 0f && Math.abs(dx) > slop && dx > 0f && Math.abs(dx) > Math.abs(dy)) {
                    dragging = true;
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        View target = card();
        float reveal = reveal();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - downX;
                target.setTranslationX(Math.max(-reveal, Math.min(0f, startX + dx)));
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                boolean open = target.getTranslationX() < -reveal / 2f;
                target.animate().translationX(open ? -reveal : 0f).setDuration(160L).start();
                if (callback != null) {
                    callback.onSettled(open);
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @NonNull
    private View card() {
        if (card == null) {
            card = findViewById(R.id.calendar_event_card);
        }
        return card;
    }

    private float reveal() {
        return 54f * getResources().getDisplayMetrics().density;
    }
}
