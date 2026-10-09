package com.demo.app.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demo.app.databinding.FragmentGuideCompleteBinding;
import com.demo.app.ui.guide.GuideNavigator;
import com.demo.app.utils.AppLog;

import java.util.ArrayList;
import java.util.List;

/** 引导第 4 页。展示完成状态，彩带落下，点击后进入语言页。 */
public class GuideCompleteFragment extends Fragment {

    private static final int[] RIBBON_COLORS = {
            0xFF1DA9FF, 0xFFFF4AD0, 0xFFF4FF28, 0xFFFF4AD0, 0xFF1DA9FF
    };
    private static final float[] RIBBON_X = {0.15f, 0.30f, 0.50f, 0.68f, 0.85f};
    private static final long[] RIBBON_DELAY = {0L, 800L, 1700L, 300L, 1200L};
    private static final long FALL_MS = 5000L;

    private FragmentGuideCompleteBinding binding;
    private GuideNavigator navigator;
    private final List<Animator> fallAnimators = new ArrayList<>();

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
        binding = FragmentGuideCompleteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnStart.setOnClickListener(v -> {
            AppLog.i("GuideCompleteFragment", "start");
            if (navigator != null) {
                navigator.goNextGuidePage();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.confettiFall.post(this::playConfettiFall);
    }

    @Override
    public void onPause() {
        stopConfettiFall();
        super.onPause();
    }

    private void playConfettiFall() {
        if (binding == null) {
            return;
        }
        stopConfettiFall();
        FrameLayout layer = binding.confettiFall;
        layer.removeAllViews();
        int width = layer.getWidth();
        if (width <= 0) {
            return;
        }
        float startY = dp(-24);
        float endY = dp(460);
        int ribbonW = Math.round(dp(8));
        int ribbonH = Math.round(dp(18));
        for (int i = 0; i < RIBBON_COLORS.length; i++) {
            View ribbon = new View(requireContext());
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(RIBBON_COLORS[i]);
            shape.setCornerRadius(dp(2));
            ribbon.setBackground(shape);
            ribbon.setAlpha(0f);
            ribbon.setRotation(i * 28f);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ribbonW, ribbonH);
            lp.gravity = Gravity.START | Gravity.TOP;
            lp.leftMargin = Math.round(width * RIBBON_X[i] - ribbonW / 2f);
            ribbon.setLayoutParams(lp);
            layer.addView(ribbon);

            ObjectAnimator fall = ObjectAnimator.ofFloat(ribbon, View.TRANSLATION_Y, startY, endY);
            ObjectAnimator spin = ObjectAnimator.ofFloat(ribbon, View.ROTATION, ribbon.getRotation(), ribbon.getRotation() + 420f);
            ObjectAnimator fade = ObjectAnimator.ofFloat(ribbon, View.ALPHA, 1f, 0f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(fall, spin, fade);
            set.setDuration(FALL_MS);
            set.setStartDelay(RIBBON_DELAY[i]);
            set.setInterpolator(new LinearInterpolator());
            fallAnimators.add(set);
            set.start();
        }
    }

    private void stopConfettiFall() {
        for (Animator animator : fallAnimators) {
            animator.cancel();
        }
        fallAnimators.clear();
        if (binding != null) {
            binding.confettiFall.removeAllViews();
        }
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    public void onDestroyView() {
        stopConfettiFall();
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        navigator = null;
        super.onDetach();
    }
}
