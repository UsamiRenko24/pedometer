package com.demo.app.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class ActivityUtils {

    /**
     * Return whether the activity exists in activity's stack.
     *
     * @param clz The activity class.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityExistsInStack(@NonNull final Class<? extends Activity> clz) {
        List<Activity> activities = UtilsBridge.getActivityList();
        for (Activity aActivity : activities) {
            if (aActivity.getClass().equals(clz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finish the activity.
     *
     * @param activity The activity.
     */
    public static void finishActivity(@NonNull final Activity activity) {
        finishActivity(activity, true);
    }

    /**
     * Finish the activity.
     *
     * @param activity   The activity.
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    public static void finishActivity(@NonNull final Activity activity, final boolean isLoadAnim) {
        activity.finish();
        if (!isLoadAnim) {
            activity.overridePendingTransition(0, 0);
        }
    }

    /**
     * Return the name of launcher activity.
     *
     * @param pkg The name of the package.
     * @return the name of launcher activity
     */
    public static String getLauncherActivity(@NonNull final String pkg) {
        if (UtilsBridge.isSpace(pkg)) return "";
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setPackage(pkg);
            PackageManager pm = Utils.getApp().getPackageManager();
            List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
            if (info == null || info.size() == 0) {
                return "";
            }
            return info.get(0).activityInfo.name;
        } catch (Throwable ignored) {
        }
        return "";
    }

    /**
     * Start home activity.
     */
    public static void startHomeActivity() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    /**
     * Start the activity.
     *
     * @param clz The activity class.
     */
    public static void startActivity(@NonNull final Class<? extends Activity> clz) {
        Context context = getTopActivityOrApp();
        startActivity(context, null, context.getPackageName(), clz.getName(), null);
    }

    /**
     * Start the activity.
     *
     * @param extras The Bundle of extras to add to this intent.
     * @param clz    The activity class.
     */
    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Class<? extends Activity> clz) {
        Context context = getTopActivityOrApp();
        startActivity(context, extras, context.getPackageName(), clz.getName(), null);
    }

    private static void startActivity(final Context context,
                                      final Bundle extras,
                                      final String pkg,
                                      final String cls,
                                      @Nullable final Bundle options) {
        Intent intent = new Intent();
        if (extras != null) intent.putExtras(extras);
        intent.setComponent(new ComponentName(pkg, cls));
        startActivity(intent, context, options);
    }

    public static void startActivity(@NonNull final Bundle extras,
                                     @NonNull final Intent intent) {
        Context context = getTopActivityOrApp();
        startActivity(context, extras, intent, null);
    }

    private static void startActivity(final Context context,
                                      final Bundle extras,
                                      final Intent intent,
                                      @Nullable final Bundle options) {
        if (extras != null) intent.putExtras(extras);
        startActivity(intent, context, options);
    }

    /**
     * Start the activity.
     *
     * @param intent The description of the activity to start.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean startActivity(@NonNull final Intent intent) {
        return startActivity(intent, getTopActivityOrApp(), null);
    }

    private static boolean startActivity(final Intent intent,
                                         final Context context,
                                         final Bundle options) {
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            if (options != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                context.startActivity(intent, options);
            } else {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Finish all of activities.
     */
    public static void finishAllActivities() {
        finishAllActivities(false);
    }

    public static void exitApp() {
        List<Activity> activityList = UtilsBridge.getActivityList();
        for (Activity act : activityList) {
            // sActivityList remove the index activity at onActivityDestroyed
            act.finishAffinity();
            act.overridePendingTransition(0, 0);
        }
    }

    /**
     * Finish all of activities.
     *
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    public static void finishAllActivities(final boolean isLoadAnim) {
        List<Activity> activityList = UtilsBridge.getActivityList();
        for (Activity act : activityList) {
            // sActivityList remove the index activity at onActivityDestroyed
            act.finish();
            if (!isLoadAnim) {
                act.overridePendingTransition(0, 0);
            }
        }
    }

    /**
     * Return the list of activity.
     *
     * @return the list of activity
     */
    public static List<Activity> getActivityList() {
        return UtilsBridge.getActivityList();
    }

    /**
     * Return whether the activity is alive.
     *
     * @param activity The activity.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityAlive(final Activity activity) {
        return activity != null && !activity.isFinishing()
                && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
    }

    /**
     * Return the activity by context.
     *
     * @param context The context.
     * @return the activity by context.
     */
    @Nullable
    public static Activity getActivityByContext(@Nullable Context context) {
        if (context == null) return null;
        Activity activity = getActivityByContextInner(context);
        if (!isActivityAlive(activity)) return null;
        return activity;
    }

    @Nullable
    private static Activity getActivityByContextInner(@Nullable Context context) {
        if (context == null) return null;
        List<Context> list = new ArrayList<>();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            Activity activity = getActivityFromDecorContext(context);
            if (activity != null) return activity;
            list.add(context);
            context = ((ContextWrapper) context).getBaseContext();
            if (context == null) {
                return null;
            }
            if (list.contains(context)) {
                // loop context
                return null;
            }
        }
        return null;
    }

    @Nullable
    private static Activity getActivityFromDecorContext(@Nullable Context context) {
        if (context == null) return null;
        if (context.getClass().getName().equals("com.android.internal.policy.DecorContext")) {
            try {
                Field mActivityContextField = context.getClass().getDeclaredField("mActivityContext");
                mActivityContextField.setAccessible(true);
                //noinspection ConstantConditions,unchecked
                return ((WeakReference<Activity>) mActivityContextField.get(context)).get();
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    /**
     * Return the top activity in activity's stack.
     *
     * @return the top activity in activity's stack
     */
    public static Activity getTopActivity() {
        return UtilsBridge.getTopActivity();
    }

    private static Context getTopActivityOrApp() {
        if (UtilsBridge.isAppForeground()) {
            Activity topActivity = getTopActivity();
            return topActivity == null ? Utils.getApp() : topActivity;
        } else {
            return Utils.getApp();
        }
    }
}