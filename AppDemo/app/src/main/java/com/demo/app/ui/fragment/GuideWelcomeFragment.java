package com.demo.app.ui.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.demo.app.databinding.FragmentGuideWelcomeBinding;
import com.demo.app.ui.adapter.GuidePagerAdapter;
import com.demo.app.ui.guide.GuideNavigator;
import com.demo.app.ui.guide.GuidePageMotion;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.CacheUtils;
import com.demo.app.utils.DefaultLauncher;

/** 引导第 1 页。Get Started 拉起默认桌面设置，无论是否设置都会进入下一页。 */
public class GuideWelcomeFragment extends Fragment implements GuidePageMotion {

    private static final long SPLASH_ENTER_MS = 1000L;
    private static final long ITEM_DURATION_MS = 720L;
    private static final long ITEM_GAP_MS = 380L;

    private FragmentGuideWelcomeBinding binding;
    private GuideNavigator navigator;
    private AnimatorSet featureAnimator;
    private boolean entrancePlayed;
    private final Runnable playRunnable = this::playFeatureEnter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof GuideNavigator) {
            navigator = (GuideNavigator) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGuideWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnGetStarted.setOnClickListener(v -> requestDefaultThenContinue());
        prepareFeatureItems();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        binding.soundWaveFx.setRunning(navigator == null || navigator.isGuidePageIdle());
        binding.getRoot().removeCallbacks(playRunnable);
        long delay = entrancePlayed ? 120L : SPLASH_ENTER_MS;
        binding.getRoot().postDelayed(playRunnable, delay);
    }

    @Override
    public void onPause() {
        if (binding != null) {
            binding.soundWaveFx.setRunning(false);
            binding.getRoot().removeCallbacks(playRunnable);
        }
        if (featureAnimator != null) {
            featureAnimator.cancel();
        }
        super.onPause();
    }

    @Override
    public void setPageSettled(boolean settled) {
        if (binding == null) {
            return;
        }
        binding.soundWaveFx.setRunning(settled && isResumed());
    }

    /** 先弹出系统默认桌面设置。无论用户是否设置，都进入下一页。 */
    private void requestDefaultThenContinue() {
        if (navigator == null) {
            return;
        }
        if (DefaultLauncher.isDefault(requireContext())) {
            AppLog.i("GuideWelcomeFragment", "already default");
            navigator.goNextGuidePage();
            return;
        }
        AppLog.i("GuideWelcomeFragment", "request default launcher");
        if (binding != null) {
            binding.soundWaveFx.setRunning(false);
            binding.getRoot().removeCallbacks(playRunnable);
        }
        if (featureAnimator != null) {
            featureAnimator.cancel();
        }
        CacheUtils.setGuideContinueNotify(true);
        FragmentActivity activity = requireActivity();
        DefaultLauncher.requestKeepingGuide(activity, success -> activity.runOnUiThread(() -> {
            CacheUtils.setGuideContinueNotify(false);
            if (!isAdded() || navigator == null) {
                return;
            }
            if (success) {
                navigator.openGuidePage(GuidePagerAdapter.PAGE_NOTIFY);
            } else {
                navigator.goNextGuidePage();
            }
        }));
    }

    /** 三条功能先藏住，进入页面后再依次上移淡入 */
    private void prepareFeatureItems() {
        if (binding == null) {
            return;
        }
        float offset = dp(36);
        for (View item : featureItems()) {
            item.setAlpha(0f);
            item.setTranslationY(offset);
        }
    }

    private void playFeatureEnter() {
        if (binding == null) {
            return;
        }
        if (featureAnimator != null) {
            featureAnimator.cancel();
        }
        entrancePlayed = true;
        float offset = dp(36);
        View[] items = featureItems();
        AnimatorSet[] sets = new AnimatorSet[items.length];
        for (int i = 0; i < items.length; i++) {
            View item = items[i];
            item.setAlpha(0f);
            item.setTranslationY(offset);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(item, View.ALPHA, 0f, 1f);
            ObjectAnimator move = ObjectAnimator.ofFloat(item, View.TRANSLATION_Y, offset, 0f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(alpha, move);
            set.setDuration(ITEM_DURATION_MS);
            set.setStartDelay(i * ITEM_GAP_MS);
            set.setInterpolator(new DecelerateInterpolator());
            sets[i] = set;
        }
        featureAnimator = new AnimatorSet();
        featureAnimator.playTogether(sets);
        featureAnimator.start();
    }

    private View[] featureItems() {
        return new View[]{binding.featureItem1, binding.featureItem2, binding.featureItem3};
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    public void onDestroyView() {
        if (binding != null) {
            binding.getRoot().removeCallbacks(playRunnable);
        }
        if (featureAnimator != null) {
            featureAnimator.cancel();
            featureAnimator = null;
        }
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        navigator = null;
        super.onDetach();
    }
}
