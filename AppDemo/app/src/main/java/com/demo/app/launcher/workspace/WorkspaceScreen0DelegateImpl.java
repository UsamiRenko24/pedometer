package com.demo.app.launcher.workspace;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.demo.app.launcher.ads.LauncherAds;
import com.demo.app.launcher.guide.Screen0GestureGuides;

import m9e778y.LauncherSDK;

/** 0 屏：首次布局，以及回到这一页时的手势蒙层。 */
public final class WorkspaceScreen0DelegateImpl implements LauncherSDK.WorkspaceScreenDelegate {

    private static final WorkspaceScreen0DelegateImpl INSTANCE = new WorkspaceScreen0DelegateImpl();

    public static WorkspaceScreen0DelegateImpl getInstance() {
        return INSTANCE;
    }

    @Nullable
    private FragmentActivity boundActivity;
    @Nullable
    private View workspaceView;
    @Nullable
    private Runnable scheduleGuidesRunnable;
    private boolean screen0Selected;
    private final Screen0GestureGuides gestureGuides = new Screen0GestureGuides();

    private WorkspaceScreen0DelegateImpl() {
    }

    @Override
    public long getScreenId() {
        return 0;
    }

    @Override
    public void ensurePlaced(@NonNull Context launcherContext) {
        Screen0Installer.run(launcherContext);
    }

    @Override
    public void onViewCreated(@NonNull FragmentActivity activity, @NonNull View view) {
        boundActivity = activity;
        workspaceView = view;
        Screen0DesktopMenu.attachWorkspace(activity, view);
    }

    @Override
    public void onPageSelected(@NonNull FragmentActivity activity) {
        boundActivity = activity;
        screen0Selected = true;
        LauncherAds.onScreen0Selected(activity);
        scheduleGuides();
    }

    @Override
    public void onPageDeselected(@NonNull FragmentActivity activity) {
        screen0Selected = false;
        cancelSchedule();
        gestureGuides.release();
        Screen0DesktopMenu.dismiss();
    }

    @Override
    public void onResume(@NonNull FragmentActivity activity) {
        boundActivity = activity;
        scheduleGuides();
    }

    @Override
    public void onPause(@NonNull FragmentActivity activity) {
        cancelSchedule();
    }

    @Override
    public void onDestroy(@NonNull FragmentActivity activity) {
        cancelSchedule();
        gestureGuides.release();
        Screen0DesktopMenu.dismiss();
        workspaceView = null;
        boundActivity = null;
        screen0Selected = false;
    }

    private void scheduleGuides() {
        View root = workspaceView;
        FragmentActivity activity = boundActivity;
        if (root == null || activity == null || !screen0Selected || activity.isFinishing()) {
            return;
        }
        View decor = activity.getWindow().getDecorView();
        if (!(decor instanceof ViewGroup host)) {
            return;
        }
        cancelSchedule();
        Runnable runnable = () -> {
            scheduleGuidesRunnable = null;
            if (screen0Selected && workspaceView != null && boundActivity != null
                    && !boundActivity.isFinishing()) {
                gestureGuides.scheduleIfNeeded(workspaceView, host);
            }
        };
        scheduleGuidesRunnable = runnable;
        root.post(runnable);
    }

    private void cancelSchedule() {
        if (scheduleGuidesRunnable != null && workspaceView != null) {
            workspaceView.removeCallbacks(scheduleGuidesRunnable);
        }
        scheduleGuidesRunnable = null;
    }
}
