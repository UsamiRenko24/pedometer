package com.demo.app.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.demo.app.ui.fragment.VolumeControlFragment;
import com.demo.app.ui.fragment.VolumeSettingsFragment;

public class VolumePagerAdapter extends FragmentStateAdapter {

    public static final int PAGE_SETTINGS = 0;
    public static final int PAGE_CONTROL = 1;
    private static final int PAGE_COUNT = 2;

    public VolumePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == PAGE_SETTINGS) {
            return new VolumeSettingsFragment();
        }
        return new VolumeControlFragment();
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}
