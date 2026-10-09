package com.demo.app.launcher.workspace;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import m9e778y.LauncherSDK;

/**
 * 0 屏上拉全部应用：蒙层为 50% 黑，搜索栏和关闭按钮叠在应用网格上面。
 */
final class AllAppsOverlay {

    private static final int SCRIM = 0x80000000;
    private static final String CLOSE_TAG = "all_apps_close";

    private AllAppsOverlay() {
    }

    static void restoreWhenClosed(@NonNull FragmentActivity activity) {
        restoreWorkspaceScrim(activity);
    }

    static void apply(@NonNull FragmentActivity activity, @Nullable View appsView) {
        if (appsView == null) {
            appsView = activity.findViewById(id(activity, "h4Nz"));
        }
        if (appsView == null) {
            return;
        }
        clearSheet(appsView);
        clearNavBar(activity, appsView);
        restoreWorkspaceScrim(activity);
        styleSearch(activity, appsView);
        ensureCloseButton(activity, appsView);
        tintLabels(activity, appsView);
    }

    private static void clearSheet(@NonNull View appsView) {
        try {
            Class<?> type = appsView.getClass();
            while (type != null) {
                try {
                    Field field = type.getDeclaredField("mBaseDrawable");
                    field.setAccessible(true);
                    field.set(appsView, new ColorDrawable(SCRIM));
                    break;
                } catch (NoSuchFieldException ignored) {
                    type = type.getSuperclass();
                }
            }
            Method reveal = appsView.getClass().getMethod("getRevealView");
            Object view = reveal.invoke(appsView);
            if (view instanceof View revealView) {
                revealView.setBackgroundColor(SCRIM);
            }
        } catch (ReflectiveOperationException ignored) {
            appsView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private static void clearNavBar(@NonNull FragmentActivity activity, @NonNull View appsView) {
        View bar = appsView.findViewById(id(activity, "h05Ts9"));
        if (bar != null) {
            bar.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /** 桌面其他屏共用这层渐变，恢复成原来的浅色，避免 0、1、-1 屏被染黑。 */
    private static void restoreWorkspaceScrim(@NonNull FragmentActivity activity) {
        View scrim = activity.findViewById(id(activity, "t4O"));
        if (scrim == null) {
            return;
        }
        Runnable paint = () -> {
            try {
                setInt(scrim, "mScrimColor", 0xE6FFFFFF);
                setInt(scrim, "mColor1", 0x99F5F5F5);
                setInt(scrim, "mColor2", 0x99EEEEEE);
                Method shader = scrim.getClass().getDeclaredMethod("createRadialShader");
                shader.setAccessible(true);
                shader.invoke(scrim);
                scrim.invalidate();
            } catch (ReflectiveOperationException ignored) {
                scrim.setBackgroundColor(Color.TRANSPARENT);
            }
        };
        if (scrim.getWidth() <= 0 || scrim.getHeight() <= 0) {
            scrim.post(paint);
        } else {
            paint.run();
        }
    }

    private static void styleSearch(@NonNull FragmentActivity activity, @NonNull View appsView) {
        View input = appsView.findViewById(id(activity, "oB6ECD"));
        if (!(input instanceof EditText field) || field.getTag(R.id.tag_all_apps_styled) != null) {
            return;
        }
        field.setTag(R.id.tag_all_apps_styled, Boolean.TRUE);
        field.setTextColor(Color.WHITE);
        field.setHintTextColor(0xB8FFFFFF);
        field.setHint(R.string.screen0_search_hint);
        field.setBackgroundResource(R.drawable.bg_gesture_search);
        field.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_gesture_search, 0, 0, 0);
        int pad = dp(field, 14);
        field.setCompoundDrawablePadding(dp(field, 8));
        field.setPadding(pad, field.getPaddingTop(), pad, field.getPaddingBottom());
        field.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
    }

    private static void ensureCloseButton(@NonNull FragmentActivity activity, @NonNull View appsView) {
        View search = appsView.findViewById(id(activity, "p94PS"));
        if (search == null || !(search.getParent() instanceof RelativeLayout parent)) {
            return;
        }
        ViewGroup.MarginLayoutParams searchLp = (ViewGroup.MarginLayoutParams) search.getLayoutParams();
        int space = dp(search, 56);
        if (searchLp.getMarginStart() < space) {
            searchLp.setMarginStart(space);
            search.setLayoutParams(searchLp);
        }
        if (parent.findViewWithTag(CLOSE_TAG) != null) {
            return;
        }
        ImageView close = new ImageView(activity);
        close.setTag(CLOSE_TAG);
        close.setImageResource(R.drawable.icon_home_app_screen0_down);
        close.setContentDescription(activity.getString(android.R.string.cancel));
        close.setOnClickListener(v -> LauncherSDK.closeAllAppsView(true));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dp(search, 36), dp(search, 36));
        lp.addRule(RelativeLayout.ALIGN_PARENT_START);
        lp.addRule(RelativeLayout.ALIGN_BOTTOM, search.getId());
        lp.setMarginStart(dp(search, 12));
        lp.bottomMargin = dp(search, 14);
        parent.addView(close, lp);
    }

    private static void tintLabels(@NonNull FragmentActivity activity, @NonNull View appsView) {
        View list = appsView.findViewById(id(activity, "ex16l"));
        if (!(list instanceof RecyclerView recycler) || recycler.getTag(R.id.tag_all_apps_styled) != null) {
            return;
        }
        recycler.setTag(R.id.tag_all_apps_styled, Boolean.TRUE);
        recycler.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                tintText(view);
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
            }
        });
        for (int i = 0; i < recycler.getChildCount(); i++) {
            tintText(recycler.getChildAt(i));
        }
    }

    private static void tintText(@NonNull View view) {
        if (view instanceof TextView text) {
            text.setTextColor(Color.WHITE);
        }
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                tintText(group.getChildAt(i));
            }
        }
    }

    private static void setInt(@NonNull View view, @NonNull String name, int value)
            throws ReflectiveOperationException {
        Class<?> type = view.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.setInt(view, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
    }

    private static int id(@NonNull FragmentActivity activity, @NonNull String name) {
        int value = activity.getResources().getIdentifier(name, "id", activity.getPackageName());
        if (value == 0) {
            value = activity.getResources().getIdentifier(name, "id", "com.demo.app");
        }
        return value;
    }

    private static int dp(@NonNull View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }
}
