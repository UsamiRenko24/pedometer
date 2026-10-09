package com.demo.app.launcher.workspace;

import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.demo.app.MainActivity;
import com.demo.app.R;
import com.demo.app.launcher.widget.Screen0PredictedAppsCardView;
import com.demo.app.launcher.widget.Screen0PredictedAppsWidget;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 0 屏空白处长按：主屏幕设置 / 小组件。预测行第一格长按：应用设置 / 小组件。整行不能移除。 */
public final class Screen0DesktopMenu {

    private static final int MRU_SPAN_X = 4;
    private static final int MRU_SPAN_Y = 1;

    @Nullable
    private static WeakReference<Dialog> current;
    @Nullable
    private static Class<?> itemClass;
    @Nullable
    private static Class<?> launcherClass;
    @Nullable
    private static Method removeMethod;

    private Screen0DesktopMenu() {
    }

    public static void attachWorkspace(@NonNull FragmentActivity activity, @NonNull View workspace) {
        final float[] touch = new float[2];
        workspace.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                int[] location = new int[2];
                v.getLocationInWindow(location);
                touch[0] = location[0] + event.getX();
                touch[1] = location[1] + event.getY();
            }
            return false;
        });
        workspace.setLongClickable(true);
        workspace.setOnLongClickListener(v -> {
            showOptions(activity, touch[0], touch[1]);
            return true;
        });
    }

    public static void attachPredictedRow(@NonNull Activity activity,
                                          @NonNull View host,
                                          @NonNull Screen0PredictedAppsCardView card) {
        if (!(activity instanceof FragmentActivity)) {
            return;
        }
        FragmentActivity hostActivity = (FragmentActivity) activity;
        host.setOnLongClickListener(null);
        host.setLongClickable(false);
        card.setVolumeControlLongClickListener(v -> {
            int[] location = new int[2];
            v.getLocationInWindow(location);
            showOptions(hostActivity, location[0] + v.getWidth() / 2f, location[1] + v.getHeight() / 2f);
            return true;
        });
    }

    public static void dismiss() {
        Dialog dialog = current == null ? null : current.get();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        current = null;
    }

    private static void showOptions(@NonNull FragmentActivity activity, float touchX, float touchY) {
        if (activity.isFinishing()) {
            return;
        }
        dismiss();
        FrameLayout root = new FrameLayout(activity);
        View menu = activity.getLayoutInflater().inflate(R.layout.popup_screen0_desktop, root, false);
        root.addView(menu);
        Dialog dialog = fullscreen(activity, root);
        dialog.findViewById(R.id.screen0_menu_home_settings).setOnClickListener(v -> {
            dismiss();
            openAppSettings(activity);
        });
        dialog.findViewById(R.id.screen0_menu_widgets).setOnClickListener(v -> {
            dismiss();
            showWidgets(activity);
        });
        menu.setVisibility(View.INVISIBLE);
        dialog.show();
        current = new WeakReference<>(dialog);
        menu.post(() -> {
            placeNear(menu, touchX, touchY);
            menu.setVisibility(View.VISIBLE);
        });
    }

    private static void showRemove(@NonNull Activity activity, @NonNull View host, @NonNull View anchor) {
        if (activity.isFinishing()) {
            return;
        }
        dismiss();
        Dialog dialog = fullscreen(activity, R.layout.popup_screen0_remove);
        View content = dialog.findViewById(android.R.id.content);
        View menu = content instanceof ViewGroup && ((ViewGroup) content).getChildCount() > 0
                ? ((ViewGroup) content).getChildAt(0)
                : content;
        menu.setOnClickListener(v -> {
            dismiss();
            removeHost(activity, host);
        });
        menu.setVisibility(View.INVISIBLE);
        dialog.show();
        current = new WeakReference<>(dialog);
        menu.post(() -> {
            placeAbove(menu, anchor);
            menu.setVisibility(View.VISIBLE);
        });
    }

    private static void showWidgets(@NonNull FragmentActivity activity) {
        if (activity.isFinishing()) {
            return;
        }
        dismiss();
        Dialog dialog = fullscreen(activity, R.layout.dialog_screen0_widgets);
        View scrim = dialog.findViewById(R.id.screen0_widgets_scrim);
        if (scrim != null) {
            scrim.setOnClickListener(v -> dismiss());
        }
        LinearLayout iconRow = dialog.findViewById(R.id.screen0_widgets_apps);
        LinearLayout list = dialog.findViewById(R.id.screen0_widgets_list);
        if (iconRow == null || list == null) {
            return;
        }
        List<AppWidgets> apps = new ArrayList<>();
        AppWidgets self = appOf(activity.getPackageManager(), activity.getPackageName());
        if (self.icon != null) {
            apps.add(self);
        }
        int[] selected = new int[]{-1};
        List<AppWidgetProviderInfo> providers = new ArrayList<>();
        Runnable bindSelection = () -> {
            if (apps.isEmpty()) {
                list.removeAllViews();
                return;
            }
            int index = selected[0];
            if (index < 0 || index >= apps.size()) {
                index = 0;
                selected[0] = 0;
            }
            markSelected(iconRow, index);
            AppWidgets app = apps.get(index);
            ensureChoices(activity, app, providers);
            bindChoices(list, app);
        };
        bindAppIcons(activity, iconRow, apps, index -> {
            if (index == selected[0]) {
                return;
            }
            selected[0] = index;
            bindSelection.run();
        });
        if (!apps.isEmpty()) {
            selected[0] = 0;
            bindSelection.run();
        }
        dialog.show();
        current = new WeakReference<>(dialog);
        iconRow.post(() -> {
            if (activity.isFinishing() || !dialog.isShowing()) {
                return;
            }
            try {
                providers.addAll(AppWidgetManager.getInstance(activity).getInstalledProviders());
            } catch (RuntimeException ignored) {
            }
            apps.clear();
            apps.addAll(loadApps(activity, providers));
            bindAppIcons(activity, iconRow, apps, index -> {
                if (index == selected[0]) {
                    return;
                }
                selected[0] = index;
                bindSelection.run();
            });
            if (apps.isEmpty()) {
                list.removeAllViews();
                return;
            }
            if (selected[0] >= apps.size()) {
                selected[0] = 0;
            }
            bindSelection.run();
        });
    }

    private static void bindAppIcons(@NonNull Activity activity,
                                     @NonNull LinearLayout row,
                                     @NonNull List<AppWidgets> apps,
                                     @NonNull IndexClick clicks) {
        row.removeAllViews();
        int inset = dp(activity, 4);
        for (int i = 0; i < apps.size(); i++) {
            AppWidgets app = apps.get(i);
            LinearLayout item = new LinearLayout(activity);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER_HORIZONTAL);
            int pad = dp(activity, 8);
            item.setPadding(pad, pad, pad, pad);
            ImageView icon = new ImageView(activity);
            int size = dp(activity, 44);
            icon.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            icon.setPadding(inset, inset, inset, inset);
            icon.setImageDrawable(app.icon);
            TextView label = new TextView(activity);
            label.setText(app.label);
            label.setTextSize(11);
            label.setTextColor(activity.getColor(R.color.color_1C1C1E));
            label.setMaxLines(1);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    dp(activity, 64), ViewGroup.LayoutParams.WRAP_CONTENT);
            labelLp.topMargin = dp(activity, 4);
            label.setLayoutParams(labelLp);
            item.addView(icon);
            item.addView(label);
            int index = i;
            item.setOnClickListener(v -> clicks.onClick(index));
            row.addView(item);
        }
    }

    private static void markSelected(@NonNull LinearLayout row, int selected) {
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (!(child instanceof ViewGroup group) || group.getChildCount() == 0) {
                continue;
            }
            View icon = group.getChildAt(0);
            icon.setBackgroundResource(i == selected ? R.drawable.bg_screen0_widget_app : 0);
        }
    }

    private static void bindChoices(@NonNull LinearLayout list, @NonNull AppWidgets app) {
        Activity activity = (Activity) list.getContext();
        list.removeAllViews();
        for (WidgetChoice choice : app.choices) {
            LinearLayout block = new LinearLayout(activity);
            block.setOrientation(LinearLayout.VERTICAL);
            block.setGravity(Gravity.CENTER_HORIZONTAL);
            int pad = dp(activity, 12);
            block.setPadding(pad, pad, pad, pad);
            block.addView(line(activity, choice.title, 16, true, R.color.color_1C1C1E));
            block.addView(line(activity, choice.span, 14, false, R.color.color_8E8E93));
            if (choice.subtitle != null && !choice.subtitle.isEmpty()) {
                block.addView(line(activity, choice.subtitle, 14, false, R.color.color_1C1C1E));
            }
            block.setOnClickListener(v -> {
                dismiss();
                choice.place.run();
            });
            list.addView(block);
        }
        capList(list);
    }

    private static void capList(@NonNull LinearLayout list) {
        if (!(list.getParent() instanceof View scroll)) {
            return;
        }
        int max = dp(list.getContext(), 260);
        Runnable measure = new Runnable() {
            @Override
            public void run() {
                if (scroll.getWidth() <= 0) {
                    scroll.post(this);
                    return;
                }
                ViewGroup.LayoutParams params = scroll.getLayoutParams();
                if (params == null) {
                    return;
                }
                list.measure(
                        View.MeasureSpec.makeMeasureSpec(scroll.getWidth(), View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                params.height = Math.min(list.getMeasuredHeight(), max);
                scroll.setLayoutParams(params);
            }
        };
        scroll.post(measure);
    }

    @NonNull
    private static TextView line(@NonNull Activity activity, @NonNull String text, int sp, boolean bold, int color) {
        TextView view = new TextView(activity);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(activity.getColor(color));
        view.setGravity(Gravity.CENTER);
        if (bold) {
            view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        }
        return view;
    }

    @NonNull
    private static List<AppWidgets> loadApps(@NonNull Activity activity,
                                             @NonNull List<AppWidgetProviderInfo> providers) {
        PackageManager packages = activity.getPackageManager();
        String self = activity.getPackageName();
        Map<String, AppWidgets> grouped = new LinkedHashMap<>();
        AppWidgets mine = appOf(packages, self);
        grouped.put(self, mine);
        for (AppWidgetProviderInfo provider : providers) {
            ComponentName component = provider.provider;
            if (component == null || self.equals(component.getPackageName())
                    || grouped.containsKey(component.getPackageName())) {
                continue;
            }
            AppWidgets group = appOf(packages, component.getPackageName());
            if (group.icon == null) {
                continue;
            }
            grouped.put(component.getPackageName(), group);
        }
        List<AppWidgets> apps = new ArrayList<>();
        for (AppWidgets group : grouped.values()) {
            if (group.icon != null) {
                apps.add(group);
            }
        }
        if (apps.size() > 1) {
            AppWidgets first = apps.get(0);
            List<AppWidgets> rest = new ArrayList<>(apps.subList(1, apps.size()));
            Collections.sort(rest, (a, b) -> a.label.compareToIgnoreCase(b.label));
            apps = new ArrayList<>();
            apps.add(first);
            apps.addAll(rest);
        }
        return apps;
    }

    private static void ensureChoices(@NonNull Activity activity,
                                      @NonNull AppWidgets app,
                                      @NonNull List<AppWidgetProviderInfo> providers) {
        if (app.choices != null) {
            return;
        }
        app.choices = new ArrayList<>();
        if (activity.getPackageName().equals(app.packageName)) {
            app.choices.add(mruChoice(activity));
            return;
        }
        AppWidgetManager widgets = AppWidgetManager.getInstance(activity);
        PackageManager packages = activity.getPackageManager();
        for (AppWidgetProviderInfo provider : providers) {
            ComponentName component = provider.provider;
            if (component != null && app.packageName.equals(component.getPackageName())) {
                app.choices.add(providerChoice(activity, widgets, packages, provider));
            }
        }
    }

    @NonNull
    private static WidgetChoice mruChoice(@NonNull Activity activity) {
        WidgetChoice choice = new WidgetChoice();
        choice.title = activity.getString(R.string.screen0_mru);
        choice.span = activity.getString(R.string.screen0_widget_span, MRU_SPAN_X, MRU_SPAN_Y);
        choice.subtitle = activity.getString(R.string.screen0_mru_desc);
        choice.place = () -> Screen0PredictedAppsWidget.getInstance().ensurePlaced(activity);
        return choice;
    }

    @NonNull
    private static WidgetChoice providerChoice(@NonNull Activity activity,
                                               @NonNull AppWidgetManager widgets,
                                               @NonNull PackageManager packages,
                                               @NonNull AppWidgetProviderInfo provider) {
        WidgetChoice choice = new WidgetChoice();
        CharSequence label = provider.loadLabel(packages);
        choice.title = label == null ? "" : label.toString();
        float density = activity.getResources().getDisplayMetrics().density;
        int spanX = spanOf(provider.minWidth, density);
        int spanY = spanOf(provider.minHeight, density);
        if (Build.VERSION.SDK_INT >= 31) {
            if (provider.targetCellWidth > 0) {
                spanX = provider.targetCellWidth;
            }
            if (provider.targetCellHeight > 0) {
                spanY = provider.targetCellHeight;
            }
        }
        choice.span = activity.getString(R.string.screen0_widget_span, spanX, spanY);
        choice.subtitle = "";
        if (Build.VERSION.SDK_INT >= 31) {
            CharSequence description = provider.loadDescription(activity);
            if (description != null) {
                choice.subtitle = description.toString();
            }
        }
        choice.place = () -> {
            if (Build.VERSION.SDK_INT >= 26 && widgets.isRequestPinAppWidgetSupported()) {
                widgets.requestPinAppWidget(provider.provider, null, null);
            }
        };
        return choice;
    }

    @NonNull
    private static AppWidgets appOf(@NonNull PackageManager packages, @NonNull String packageName) {
        AppWidgets app = new AppWidgets();
        app.packageName = packageName;
        app.label = packageName;
        try {
            ApplicationInfo info = packages.getApplicationInfo(packageName, 0);
            app.icon = packages.getApplicationIcon(info);
            CharSequence label = packages.getApplicationLabel(info);
            if (label != null) {
                app.label = label.toString();
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            app.icon = null;
        }
        return app;
    }

    private static int spanOf(int minPx, float density) {
        int dp = density <= 0f ? minPx : Math.round(minPx / density);
        if (dp >= 250) {
            return 4;
        }
        if (dp >= 180) {
            return 3;
        }
        if (dp >= 110) {
            return 2;
        }
        return 1;
    }

    private static void openAppSettings(@NonNull Activity activity) {
        activity.startActivity(new android.content.Intent(activity, MainActivity.class)
                .putExtra(MainActivity.EXTRA_OPEN_SETTINGS, true)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private static void removeHost(@NonNull Activity activity, @NonNull View host) {
        try {
            if (removeMethod == null) {
                itemClass = Class.forName("yukh9.F16");
                launcherClass = Class.forName("r09o.JUp49");
                removeMethod = Class.forName("wx4.E0H5Us").getMethod(
                        "removeWorkspaceOrFolderItem", launcherClass, itemClass, View.class);
            }
            View itemView = null;
            Object info = null;
            View current = host;
            while (current != null) {
                Object tag = current.getTag();
                if (itemClass.isInstance(tag)) {
                    info = tag;
                    itemView = current;
                    break;
                }
                if (!(current.getParent() instanceof View)) {
                    break;
                }
                current = (View) current.getParent();
            }
            if (info == null || itemView == null || !launcherClass.isInstance(activity)) {
                return;
            }
            removeMethod.invoke(null, activity, info, itemView);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @NonNull
    private static Dialog fullscreen(@NonNull Activity activity, int layout) {
        return fullscreen(activity, activity.getLayoutInflater().inflate(layout, null));
    }

    @NonNull
    private static Dialog fullscreen(@NonNull Activity activity, @NonNull View content) {
        Dialog dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        View scrim = content.findViewById(R.id.screen0_widgets_scrim);
        if (scrim == null && content instanceof ViewGroup group && group.getChildCount() == 1) {
            scrim = group.getChildAt(0).findViewById(R.id.screen0_widgets_scrim);
        }
        if (scrim == null) {
            View root = dialog.findViewById(android.R.id.content);
            if (root != null) {
                root.setBackgroundResource(R.color.color_40000000);
                root.setOnClickListener(v -> dismiss());
            }
        }
        return dialog;
    }

    private static void placeNear(@NonNull View menu, float touchX, float touchY) {
        View parent = (View) menu.getParent();
        if (parent == null) {
            return;
        }
        int[] window = new int[2];
        parent.getLocationInWindow(window);
        int left = Math.round(touchX - window[0] - menu.getWidth() / 2f);
        int top = Math.round(touchY - window[1] - menu.getHeight() - dp(menu.getContext(), 12));
        left = clamp(left, dp(menu.getContext(), 12), parent.getWidth() - menu.getWidth() - dp(menu.getContext(), 12));
        top = clamp(top, dp(menu.getContext(), 12), parent.getHeight() - menu.getHeight() - dp(menu.getContext(), 12));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.leftMargin = left;
        params.topMargin = top;
        menu.setLayoutParams(params);
    }

    private static void placeAbove(@NonNull View menu, @NonNull View anchor) {
        View parent = (View) menu.getParent();
        if (parent == null) {
            return;
        }
        int[] parentLoc = new int[2];
        int[] anchorLoc = new int[2];
        parent.getLocationInWindow(parentLoc);
        anchor.getLocationInWindow(anchorLoc);
        int left = anchorLoc[0] - parentLoc[0] + (anchor.getWidth() - menu.getWidth()) / 2;
        int top = anchorLoc[1] - parentLoc[1] - menu.getHeight() - dp(menu.getContext(), 8);
        left = clamp(left, dp(menu.getContext(), 12), parent.getWidth() - menu.getWidth() - dp(menu.getContext(), 12));
        top = clamp(top, dp(menu.getContext(), 12), parent.getHeight() - menu.getHeight() - dp(menu.getContext(), 12));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.leftMargin = left;
        params.topMargin = top;
        menu.setLayoutParams(params);
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }

    private static int dp(@NonNull android.content.Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private interface IndexClick {
        void onClick(int index);
    }

    private static final class AppWidgets {
        String packageName = "";
        @Nullable
        Drawable icon;
        String label = "";
        @Nullable
        List<WidgetChoice> choices;
    }

    private static final class WidgetChoice {
        String title = "";
        String span = "";
        String subtitle = "";
        Runnable place = () -> {
        };
    }
}
