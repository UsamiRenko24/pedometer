package com.demo.app.ui.fragment;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demo.app.databinding.FragmentGuideMomentsBinding;
import com.demo.app.ui.guide.GuideNavigator;
import com.demo.app.ui.guide.GuidePageMotion;
import com.demo.app.utils.AppLog;

/** 引导第 8 页。三个图标绕耳机旋转，点击后结束引导。 */
public class GuideMomentsFragment extends Fragment implements GuidePageMotion {

    private static final long ORBIT_MS = 10470L;
    private static final float MEDIA_ANGLE = 180f;
    private static final float NOTICE_ANGLE = 300f;
    private static final float PLAY_ANGLE = 60f;

    private FragmentGuideMomentsBinding binding;
    private GuideNavigator navigator;
    private ValueAnimator orbitAnimator;

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
        binding = FragmentGuideMomentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnNext.setOnClickListener(v -> {
            AppLog.i("GuideMomentsFragment", "finish guide");
            if (navigator != null) {
                navigator.goNextGuidePage();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigator == null || navigator.isGuidePageIdle()) {
            startOrbit();
        }
    }

    @Override
    public void onPause() {
        stopOrbit();
        super.onPause();
    }

    @Override
    public void setPageSettled(boolean settled) {
        if (settled && isResumed()) {
            startOrbit();
        } else {
            stopOrbit();
        }
    }

    private void startOrbit() {
        if (binding == null || orbitAnimator != null) {
            return;
        }
        binding.orbit.post(() -> {
            if (binding == null || !isResumed() || orbitAnimator != null) {
                return;
            }
            if (navigator != null && !navigator.isGuidePageIdle()) {
                return;
            }
            float radiusX = binding.orbit.getWidth() * (133f / 162f);
            float radiusY = binding.orbit.getHeight() * (68f / 174f);
            orbitAnimator = ValueAnimator.ofFloat(0f, 360f);
            orbitAnimator.setDuration(ORBIT_MS);
            orbitAnimator.setRepeatCount(ValueAnimator.INFINITE);
            orbitAnimator.setInterpolator(new LinearInterpolator());
            orbitAnimator.addUpdateListener(animation -> {
                if (binding == null) {
                    return;
                }
                float spin = (float) animation.getAnimatedValue();
                place(binding.ivMedia, radiusX, radiusY, MEDIA_ANGLE + spin);
                place(binding.ivNotice, radiusX, radiusY, NOTICE_ANGLE + spin);
                place(binding.ivPlay, radiusX, radiusY, PLAY_ANGLE + spin);
            });
            orbitAnimator.start();
        });
    }

    private void place(View icon, float radiusX, float radiusY, float degrees) {
        double rad = Math.toRadians(degrees);
        float depth = (float) Math.sin(rad);
        icon.setTranslationX((float) (Math.cos(rad) * radiusX));
        icon.setTranslationY(depth * radiusY);
        float scale = 0.95f + 0.16f * depth;
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        icon.setRotation((float) (Math.cos(rad) * 13f));
        icon.setAlpha(0.9f + 0.1f * depth);
        icon.setTranslationZ(depth > 0f ? dp(2) : 0f);
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private void stopOrbit() {
        if (orbitAnimator != null) {
            orbitAnimator.cancel();
            orbitAnimator = null;
        }
    }

    @Override
    public void onDestroyView() {
        stopOrbit();
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        navigator = null;
        super.onDetach();
    }
}
