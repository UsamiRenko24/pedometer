package com.demo.app.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.demo.app.ui.fragment.GuideBoostFragment;
import com.demo.app.ui.fragment.GuideCompleteFragment;
import com.demo.app.ui.fragment.GuideDefaultFragment;
import com.demo.app.ui.fragment.GuideLanguageFragment;
import com.demo.app.ui.fragment.GuideMomentsFragment;
import com.demo.app.ui.fragment.GuideNotifyFragment;
import com.demo.app.ui.fragment.GuideSoundFragment;
import com.demo.app.ui.fragment.GuideWelcomeFragment;

/**
 * 共 8 页引导。
 */
public class GuidePagerAdapter extends FragmentStateAdapter {

    public static final int PAGE_WELCOME = 0;
    public static final int PAGE_DEFAULT = 1;
    public static final int PAGE_NOTIFY = 2;
    public static final int PAGE_COMPLETE = 3;
    public static final int PAGE_LANGUAGE = 4;
    public static final int PAGE_SOUND = 5;
    public static final int PAGE_BOOST = 6;
    public static final int PAGE_MOMENTS = 7;
    public static final int PAGE_COUNT = 8;

    public GuidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case PAGE_DEFAULT -> new GuideDefaultFragment();
            case PAGE_NOTIFY -> new GuideNotifyFragment();
            case PAGE_COMPLETE -> new GuideCompleteFragment();
            case PAGE_LANGUAGE -> new GuideLanguageFragment();
            case PAGE_SOUND -> new GuideSoundFragment();
            case PAGE_BOOST -> new GuideBoostFragment();
            case PAGE_MOMENTS -> new GuideMomentsFragment();
            default -> new GuideWelcomeFragment();
        };
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}
