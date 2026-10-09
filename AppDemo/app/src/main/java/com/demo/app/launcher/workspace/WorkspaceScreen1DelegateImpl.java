package com.demo.app.launcher.workspace;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import m9e778y.LauncherSDK;

/** 1 屏：首次布局由 {@link Screen1Installer} 落位。 */
public final class WorkspaceScreen1DelegateImpl implements LauncherSDK.WorkspaceScreenDelegate {

    private static final WorkspaceScreen1DelegateImpl INSTANCE = new WorkspaceScreen1DelegateImpl();

    public static WorkspaceScreen1DelegateImpl getInstance() {
        return INSTANCE;
    }

    private WorkspaceScreen1DelegateImpl() {
    }

    @Override
    public long getScreenId() {
        return 1;
    }

    @Override
    public void ensurePlaced(@NonNull Context context) {
        Screen1Installer.run(context);
    }

    @Override
    public void onViewCreated(@NonNull FragmentActivity activity, @NonNull View view) {
    }

    @Override
    public void onPageSelected(@NonNull FragmentActivity activity) {
    }

    @Override
    public void onPageDeselected(@NonNull FragmentActivity activity) {
    }

    @Override
    public void onResume(@NonNull FragmentActivity activity) {
    }

    @Override
    public void onPause(@NonNull FragmentActivity activity) {
    }

    @Override
    public void onDestroy(@NonNull FragmentActivity activity) {
    }
}
