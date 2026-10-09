package com.demo.app.launcher.workspace;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.demo.app.R;
import com.demo.app.launcher.ads.LauncherAds;
import com.demo.app.ui.adapter.VolumePagerAdapter;
import com.demo.app.ui.fragment.VolumeControlFragment;
import com.demo.app.ui.fragment.VolumeSettingsFragment;

/** 负一屏放音量设置和音量控制，用底部标签切换。不带主界面的菜单和设置抽屉。 */
public final class LeftScreenHomeController {

    private final FragmentActivity activity;
    @Nullable
    private View root;
    @Nullable
    private ViewPager2 pager;
    @Nullable
    private ImageView boost;
    @Nullable
    private TextView settingsTab;
    @Nullable
    private TextView controlTab;

    public LeftScreenHomeController(@NonNull FragmentActivity activity) {
        this.activity = activity;
    }

    public void bind(@NonNull View view) {
        root = view;
        pager = view.findViewById(R.id.left_screen_pager);
        boost = view.findViewById(R.id.left_screen_boost);
        settingsTab = view.findViewById(R.id.left_screen_settings);
        controlTab = view.findViewById(R.id.left_screen_control);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            applySystemBars(v, insets);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
        view.post(() -> {
            if (root == null) {
                return;
            }
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(root);
            applySystemBars(root, insets);
        });
        if (pager.getAdapter() == null) {
            VolumePagerAdapter adapter = new VolumePagerAdapter(activity);
            pager.setAdapter(adapter);
            pager.setUserInputEnabled(false);
            pager.setOffscreenPageLimit(1);
            pager.setCurrentItem(VolumePagerAdapter.PAGE_SETTINGS, false);
            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updateTabs(position);
                }
            });
        }
        updateTabs(pager.getCurrentItem());
        settingsTab.setOnClickListener(v -> selectPage(VolumePagerAdapter.PAGE_SETTINGS));
        controlTab.setOnClickListener(v -> selectPage(VolumePagerAdapter.PAGE_CONTROL));
        boost.setOnClickListener(v -> {
            boolean muted = !v.isSelected();
            v.setSelected(muted);
            boost.setImageResource(muted ? R.drawable.volume_notice_silent : R.drawable.volume_notice);
            VolumeSettingsFragment settings = findSettings();
            if (settings != null) {
                settings.setHeaderMuted(muted);
            }
        });
    }

    public void onResume() {
        if (root != null) {
            applySystemBars(root, ViewCompat.getRootWindowInsets(root));
            LauncherAds.setVisible(root.findViewById(R.id.left_screen_banner_ad), true);
        }
    }

    private void applySystemBars(@NonNull View view, @Nullable WindowInsetsCompat insets) {
        int top = statusBarHeight(view);
        int left = 0;
        int right = 0;
        if (insets != null) {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout());
            top = Math.max(top, bars.top);
            left = bars.left;
            right = bars.right;
        }
        view.setPadding(left, top, right, 0);
    }

    private static int statusBarHeight(@NonNull View view) {
        return systemDimen(view, "status_bar_height");
    }

    private static int systemDimen(@NonNull View view, @NonNull String name) {
        int id = view.getResources().getIdentifier(name, "dimen", "android");
        if (id <= 0) {
            return 0;
        }
        return view.getResources().getDimensionPixelSize(id);
    }

    public void onPause() {
        VolumeControlFragment control = findControl();
        if (control != null) {
            control.stopPlayback();
        }
    }

    public boolean handleBackPressed() {
        return false;
    }

    public void destroy() {
        onPause();
        if (pager != null) {
            pager.setAdapter(null);
            pager = null;
        }
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, null);
            root = null;
        }
        boost = null;
        settingsTab = null;
        controlTab = null;
    }

    private void selectPage(int page) {
        if (pager == null) {
            return;
        }
        if (page == VolumePagerAdapter.PAGE_SETTINGS) {
            onPause();
        }
        if (pager.getCurrentItem() != page) {
            pager.setCurrentItem(page, false);
        }
        updateTabs(page);
    }

    private void updateTabs(int page) {
        if (settingsTab == null || controlTab == null || boost == null) {
            return;
        }
        boolean settingsSelected = page == VolumePagerAdapter.PAGE_SETTINGS;
        settingsTab.setBackgroundResource(
                settingsSelected ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        controlTab.setBackgroundResource(
                settingsSelected ? R.drawable.bg_tab_unselected : R.drawable.bg_tab_selected);
        settingsTab.setTextColor(settingsSelected ? Color.WHITE : Color.parseColor("#1B2326"));
        controlTab.setTextColor(settingsSelected ? Color.parseColor("#1B2326") : Color.WHITE);
        boost.setVisibility(settingsSelected ? View.VISIBLE : View.GONE);
    }

    @Nullable
    private VolumeSettingsFragment findSettings() {
        for (Fragment fragment : activity.getSupportFragmentManager().getFragments()) {
            if (fragment instanceof VolumeSettingsFragment settings) {
                return settings;
            }
        }
        return null;
    }

    @Nullable
    private VolumeControlFragment findControl() {
        for (Fragment fragment : activity.getSupportFragmentManager().getFragments()) {
            if (fragment instanceof VolumeControlFragment control) {
                return control;
            }
        }
        return null;
    }
}
