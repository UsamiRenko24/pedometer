package com.demo.app.launcher.ui.activity.discover;



import android.content.Context;

import android.content.Intent;

import android.graphics.Color;

import android.view.View;

import android.widget.ImageView;

import android.widget.TextView;



import androidx.activity.EdgeToEdge;

import androidx.activity.SystemBarStyle;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;

import androidx.core.graphics.Insets;

import androidx.core.view.ViewCompat;

import androidx.core.view.WindowCompat;

import androidx.core.view.WindowInsetsCompat;

import androidx.core.view.WindowInsetsControllerCompat;

import androidx.viewpager2.widget.ViewPager2;



import com.demo.app.R;

import com.demo.app.databinding.ActivityDiscoverBinding;
import com.demo.app.launcher.ads.LauncherBackInsertPlaceholder;
import com.demo.app.launcher.ads.LauncherBackPressAds;

import com.demo.app.launcher.ui.adapter.discover.DiscoverPagerAdapter;

import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;



/** Discover 首页。底部 Explore、Apps、Games，默认 Explore。 */

public class DiscoverActivity extends BaseActivity<ActivityDiscoverBinding> {



    private static final int TAB_EXPLORE = 0;

    private static final int TAB_APPS = 1;

    private static final int TAB_GAMES = 2;



    public static void start(@NonNull Context context) {

        Intent intent = new Intent(context, DiscoverActivity.class);

        if (!(context instanceof android.app.Activity)) {

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        }

        context.startActivity(intent);

    }



    @Nullable

    @Override

    protected ActivityDiscoverBinding getViewBinding() {

        return ActivityDiscoverBinding.inflate(getLayoutInflater());

    }



    @Override

    protected void initWindow(View rootView) {

        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));

        applyImmersive();

        ViewCompat.setOnApplyWindowInsetsListener(binding.discoverRoot, (v, insets) -> {

            Insets bars = insets.getInsets(

                    SystemUiHelper.contentInsetTypes());

            v.setPadding(bars.left, bars.top, bars.right, 0);

            int tabPad = binding.discoverTabBar.getPaddingTop();

            binding.discoverTabBar.setPadding(0, tabPad, 0, tabPad + bars.bottom);

            return WindowInsetsCompat.CONSUMED;

        });

    }



    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), true, true);
    }



    @Override

    protected void initViews() {

        binding.discoverPager.setAdapter(new DiscoverPagerAdapter(this));

        binding.discoverPager.setUserInputEnabled(false);

        binding.discoverPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);

        showTab(TAB_EXPLORE);
        LauncherBackInsertPlaceholder.preload(this);
        LauncherBackPressAds.install(this, this::finish);

    }



    @Override

    protected void initEvents() {

        binding.discoverTabExplore.setOnClickListener(v -> showTab(TAB_EXPLORE));

        binding.discoverTabApps.setOnClickListener(v -> showTab(TAB_APPS));

        binding.discoverTabGames.setOnClickListener(v -> showTab(TAB_GAMES));

    }



    private void showTab(int index) {

        if (binding.discoverPager.getCurrentItem() != index) {

            binding.discoverPager.setCurrentItem(index, false);

        }

        tint(binding.discoverTabExploreIcon, binding.discoverTabExploreText, index == TAB_EXPLORE,

                R.drawable.icon_home_app_discover_navigation_explore_selected,

                R.drawable.icon_home_app_discover_navigation_explore_unselected);

        tint(binding.discoverTabAppsIcon, binding.discoverTabAppsText, index == TAB_APPS,

                R.drawable.icon_home_app_discover_navigation_apps_selected,

                R.drawable.icon_home_app_discover_navigation_apps_unselected);

        tint(binding.discoverTabGamesIcon, binding.discoverTabGamesText, index == TAB_GAMES,

                R.drawable.icon_home_app_discover_navigation_games_selected,

                R.drawable.icon_home_app_discover_navigation_games_unselected);

    }



    private void tint(@NonNull ImageView icon, @NonNull TextView label, boolean selected,

                      int selectedIcon, int unselectedIcon) {

        icon.setImageResource(selected ? selectedIcon : unselectedIcon);

        label.setTextColor(selected ? getColor(R.color.color_4F7CFB) : getColor(R.color.color_A1ADC8));

    }

}

