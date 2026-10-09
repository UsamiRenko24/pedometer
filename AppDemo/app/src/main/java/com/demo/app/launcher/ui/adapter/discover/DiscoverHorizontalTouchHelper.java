package com.demo.app.launcher.ui.adapter.discover;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 横向 RecyclerView / ViewPager2 内层列表：判定为横向滑动时阻止外层竖向列表拦截。
 */
final class DiscoverHorizontalTouchHelper implements RecyclerView.OnItemTouchListener {

    private static final Object INSTALLED = new Object();

    static void installOnce(@NonNull RecyclerView recyclerView) {
        if (recyclerView.getTag() == INSTALLED) {
            return;
        }
        recyclerView.addOnItemTouchListener(new DiscoverHorizontalTouchHelper());
        recyclerView.setTag(INSTALLED);
    }

    private float downX;
    private float downY;
    private boolean directionDecided;
    private boolean horizontalScroll;

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        int slop = ViewConfiguration.get(rv.getContext()).getScaledTouchSlop();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = e.getX();
                downY = e.getY();
                directionDecided = false;
                horizontalScroll = false;
                disallowParentIntercept(rv, false);
                break;
            case MotionEvent.ACTION_MOVE:
                if (directionDecided) {
                    if (horizontalScroll) {
                        disallowParentIntercept(rv, true);
                    }
                    break;
                }
                float dx = Math.abs(e.getX() - downX);
                float dy = Math.abs(e.getY() - downY);
                if (dx > slop || dy > slop) {
                    int direction = e.getX() - downX > 0 ? -1 : 1;
                    horizontalScroll = dx > dy && rv.canScrollHorizontally(direction);
                    disallowParentIntercept(rv, horizontalScroll);
                    directionDecided = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                disallowParentIntercept(rv, false);
                directionDecided = false;
                horizontalScroll = false;
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        // 仅协助父级拦截，不消费事件
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    private static void disallowParentIntercept(@NonNull View view, boolean disallow) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
            parent = parent.getParent();
        }
    }
}
