package com.demo.app.launcher.ui.adapter.discover;



import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentActivity;

import androidx.viewpager2.adapter.FragmentStateAdapter;



import com.demo.app.launcher.ui.fragment.discover.DiscoverFeedFragment;



/** Discover 底部分页：0=Explore，1=Apps，2=Games（与 activity_discover 底部栏从左到右一致）。 */

public final class DiscoverPagerAdapter extends FragmentStateAdapter {

    private static final int PAGE_EXPLORE = 0;
    private static final int PAGE_APPS = 1;
    private static final int PAGE_GAMES = 2;
    /** 与 GameCenterConfig category_id 一致：1=应用，2=游戏。 */
    private static final int CATEGORY_APPS = 1;
    private static final int CATEGORY_GAMES = 2;



    public DiscoverPagerAdapter(@NonNull FragmentActivity activity) {

        super(activity);

    }



    @Override

    public int getItemCount() {

        return 3;

    }



    @NonNull

    @Override

    public Fragment createFragment(int position) {

        final int categoryId;
        if (position == PAGE_APPS) {
            categoryId = CATEGORY_APPS;
        } else if (position == PAGE_GAMES) {
            categoryId = CATEGORY_GAMES;
        } else {
            categoryId = DiscoverFeedAdapter.CATEGORY_EXPLORE;
        }

        return DiscoverFeedFragment.newInstance(categoryId);

    }

}

