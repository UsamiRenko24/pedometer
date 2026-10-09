package com.demo.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.demo.app.databinding.ActivityMainBinding;
import com.demo.app.launcher.ads.SplashFeedPlaceholder;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.CacheUtils;
import com.demo.app.utils.SystemUiHelper;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.ui.activity.GuideActivity;
import com.demo.app.ui.activity.LanguageActivity;
import com.demo.app.ui.activity.NotificationActivity;
import com.demo.app.ui.activity.SplashActivity;
import com.demo.app.ui.activity.SupportActivity;
import com.demo.app.ui.adapter.VolumePagerAdapter;
import com.demo.app.ui.fragment.VolumeControlFragment;
import com.demo.app.ui.fragment.VolumeSettingsFragment;

/** 主页：默认打开声音设置，可切到声音控制。左上角菜单滑出设置侧边栏。 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {

    private static final String STATE_PAGE = "main_page";

    public static final String EXTRA_REFRESH_LANGUAGE = "extra_refresh_language";
    public static final String EXTRA_OPEN_SETTINGS = "extra_open_settings";

    /** 首次进入用音量设置；语言切换重建时沿用原来的那一页。 */
    private int initialPage = VolumePagerAdapter.PAGE_SETTINGS;

    /** 语言确认后重建主页，让新语言作用到已经打开的界面。 */
    private final ActivityResultLauncher<Intent> languageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    AppLog.i("MainActivity", "language changed, recreate");
                    recreate();
                }
            });

    @Nullable
    @Override
    protected ActivityMainBinding getViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        super.initWindow(rootView);
        binding.drawerLayout.setStatusBarBackground(null);
        binding.drawerLayout.setFitsSystemWindows(false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout, (v, insets) -> {
            Insets bars = insets.getInsets(SystemUiHelper.contentInsetTypes());
            View content = binding.drawerLayout.getChildAt(0);
            content.setPadding(bars.left, bars.top, bars.right, content.getPaddingBottom());
            binding.settingsDrawer.getRoot().setPadding(bars.left, bars.top, bars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(binding.drawerLayout);
    }

    @Override
    protected void initViews() {
        VolumePagerAdapter adapter = new VolumePagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setUserInputEnabled(false);
        binding.viewPager.setOffscreenPageLimit(1);
        binding.viewPager.setCurrentItem(initialPage, false);
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateTabs(position);
            }
        });
        updateTabs(initialPage);
        binding.drawerLayout.setScrimColor(Color.TRANSPARENT);
        //禁止侧边栏从屏幕边缘滑动
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        binding.settingsDrawer.tvVersion.setText(appVersionName());
        closeSplashWhenDrawn();
        // 进主界面先弹一屏开屏广告占位，短暂展示后自动消失
        SplashFeedPlaceholder.run(this, () -> { });
    }

    private void closeSplashWhenDrawn() {
        View root = binding.getRoot();
        root.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                root.getViewTreeObserver().removeOnPreDrawListener(this);
                root.postDelayed(SplashActivity::finishIfRunning, 420);
                return true;
            }
        });
    }

    @Override
    protected void initEvents() {
        binding.btnMenu.setOnClickListener(v -> {
            AppLog.i("MainActivity", "open settings");
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });
        binding.settingsDrawer.btnSettingsBack.setOnClickListener(v ->
                binding.drawerLayout.closeDrawer(GravityCompat.START));
        binding.settingsDrawer.rowLanguage.setOnClickListener(v -> {
            AppLog.i("MainActivity", "open language");
            languageLauncher.launch(new Intent(this, LanguageActivity.class));
        });
        binding.settingsDrawer.rowNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class)));
        binding.settingsDrawer.rowPrivacy.setOnClickListener(v -> openLink(R.string.url_privacy));
        binding.settingsDrawer.rowTerms.setOnClickListener(v -> openLink(R.string.url_terms));
        if (getIntent().getBooleanExtra(EXTRA_OPEN_SETTINGS, false)) {
            getIntent().removeExtra(EXTRA_OPEN_SETTINGS);
            binding.drawerLayout.post(() -> binding.drawerLayout.openDrawer(GravityCompat.START));
        }
        binding.settingsDrawer.rowSupport.setOnClickListener(v ->
                startActivity(new Intent(this, SupportActivity.class)));
        binding.btnVolumeSettings.setOnClickListener(v -> selectPage(VolumePagerAdapter.PAGE_SETTINGS));
        binding.btnVolumeControl.setOnClickListener(v -> selectPage(VolumePagerAdapter.PAGE_CONTROL));
        binding.btnBoost.setOnClickListener(v -> {
            boolean muted = !v.isSelected();
            v.setSelected(muted);
            binding.btnBoost.setImageResource(
                    muted ? R.drawable.volume_notice_silent : R.drawable.volume_notice);
            VolumeSettingsFragment settings = findSettingsFragment();
            if (settings != null) {
                settings.setHeaderMuted(muted);
            }
        });
    }

    @Nullable
    private VolumeSettingsFragment findSettingsFragment() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof VolumeSettingsFragment) {
                return (VolumeSettingsFragment) fragment;
            }
            for (Fragment child : fragment.getChildFragmentManager().getFragments()) {
                if (child instanceof VolumeSettingsFragment) {
                    return (VolumeSettingsFragment) child;
                }
            }
        }
        return null;
    }

    private void openLink(int urlRes) {
        String url = getString(urlRes);
        if (url.isEmpty()) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException ignored) {
            AppLog.i("MainActivity", "cannot open " + url);
        }
    }

    private void selectPage(int page) {
        AppLog.i("MainActivity", "select page " + page);
        if (page == VolumePagerAdapter.PAGE_SETTINGS) {
            VolumeControlFragment control = findControlFragment();
            if (control != null) {
                control.stopPlayback();
            }
        }
        if (binding.viewPager.getCurrentItem() != page) {
            binding.viewPager.setCurrentItem(page, false);
        }
        updateTabs(page);
    }

    @Nullable
    private VolumeControlFragment findControlFragment() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof VolumeControlFragment) {
                return (VolumeControlFragment) fragment;
            }
            for (Fragment child : fragment.getChildFragmentManager().getFragments()) {
                if (child instanceof VolumeControlFragment) {
                    return (VolumeControlFragment) child;
                }
            }
        }
        return null;
    }

    private void updateTabs(int page) {
        boolean settingsSelected = page == VolumePagerAdapter.PAGE_SETTINGS;
        binding.btnVolumeSettings.setBackgroundResource(
                settingsSelected ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        binding.btnVolumeControl.setBackgroundResource(
                settingsSelected ? R.drawable.bg_tab_unselected : R.drawable.bg_tab_selected);
        binding.btnVolumeSettings.setTextColor(
                settingsSelected ? Color.WHITE : Color.parseColor("#1B2326"));
        binding.btnVolumeControl.setTextColor(
                settingsSelected ? Color.parseColor("#1B2326") : Color.WHITE);
        binding.btnBoost.setVisibility(settingsSelected ? View.VISIBLE : View.GONE);
    }

    private String appVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            return "";
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            initialPage = savedInstanceState.getInt(STATE_PAGE, VolumePagerAdapter.PAGE_SETTINGS);
        }
        super.onCreate(savedInstanceState);
        if (CacheUtils.isShowGuidePage()) {
            bringGuideToFront();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (CacheUtils.isShowGuidePage()) {
            bringGuideToFront();
            return;
        }
        if (intent.getBooleanExtra(EXTRA_REFRESH_LANGUAGE, false)) {
            intent.removeExtra(EXTRA_REFRESH_LANGUAGE);
            setIntent(intent);
            AppLog.i("MainActivity", "language changed, recreate");
            recreate();
            return;
        }
        if (intent.getBooleanExtra(EXTRA_OPEN_SETTINGS, false)) {
            intent.removeExtra(EXTRA_OPEN_SETTINGS);
            setIntent(intent);
            binding.drawerLayout.post(() -> binding.drawerLayout.openDrawer(GravityCompat.START));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CacheUtils.isShowGuidePage()) {
            bringGuideToFront();
        }
    }

    /**设为默认桌面后系统会打开主页。引导还没结束时，把原来引导页带回前台。 */
    private void bringGuideToFront() {
        AppLog.i("MainActivity", "guide still running, return to guide");
        Intent guide = new Intent(this, GuideActivity.class);
        guide.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(guide);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PAGE, binding.viewPager.getCurrentItem());
    }

    /** 系统恢复页面后，再把底部选中态对齐到实际内容。ViewPager 的页码要等布局后才落定。 */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        binding.viewPager.post(() -> updateTabs(binding.viewPager.getCurrentItem()));
    }

    @Override
    protected void initData(@Nullable Bundle bundle) {
    }
}
