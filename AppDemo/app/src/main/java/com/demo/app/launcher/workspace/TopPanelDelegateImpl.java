package com.demo.app.launcher.workspace;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.demo.app.R;

import m9e778y.LauncherSDK;

/** 下拉顶部搜索面板。 */
public final class TopPanelDelegateImpl implements LauncherSDK.TopPanelDelegate {

    private static final TopPanelDelegateImpl INSTANCE = new TopPanelDelegateImpl();

    public static TopPanelDelegateImpl getInstance() {
        return INSTANCE;
    }

    @Nullable
    private TopPanelSearchController searchController;

    private TopPanelDelegateImpl() {
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View createContentView(@NonNull FragmentActivity activity) {
        return LayoutInflater.from(activity).inflate(R.layout.custom_content_top_panel, null);
    }

    @Override
    public void onViewCreated(@NonNull FragmentActivity activity, @NonNull View view) {
        searchController = new TopPanelSearchController(activity, () -> LauncherSDK.closeTopPanel(true));
        searchController.bind(view);
    }

    @Override
    public boolean handleBackPressed(@NonNull FragmentActivity activity) {
        return searchController != null && searchController.onBackPressed();
    }

    @Override
    public void onOpened(@NonNull FragmentActivity activity) {
        if (searchController != null) {
            searchController.onOpened();
        }
    }

    @Override
    public void onClosed(@NonNull FragmentActivity activity) {
        if (searchController != null) {
            searchController.onClosed();
        }
    }

    @Override
    public void onResume(@NonNull FragmentActivity activity) {
        if (searchController != null) {
            searchController.onResume();
        }
    }

    @Override
    public void onPause(@NonNull FragmentActivity activity) {
        if (searchController != null) {
            searchController.onPause();
        }
    }

    @Override
    public void onDestroy(@NonNull FragmentActivity activity) {
        if (searchController != null) {
            searchController.destroy();
            searchController = null;
        }
    }
}
