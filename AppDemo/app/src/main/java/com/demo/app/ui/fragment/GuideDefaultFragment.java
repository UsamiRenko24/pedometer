package com.demo.app.ui.fragment;

import android.animation.Animator;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.demo.app.R;

import com.demo.app.databinding.FragmentGuideDefaultBinding;
import com.demo.app.ui.adapter.GuidePagerAdapter;
import com.demo.app.ui.guide.GuideNavigator;
import com.demo.app.ui.guide.GuidePageMotion;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.CacheUtils;
import com.demo.app.utils.DefaultLauncher;

/** 引导第 2 页。已是默认桌面才能继续，否则再次拉起系统设置。 */
public class GuideDefaultFragment extends Fragment implements GuidePageMotion {

    private static final long CYCLE_MS = 4500L;

    private FragmentGuideDefaultBinding binding;
    private GuideNavigator navigator;
    private final java.util.List<Animator> tapAnimators = new java.util.ArrayList<>();

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
        binding = FragmentGuideDefaultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnSetDefault.setOnClickListener(v -> continueIfDefault());
        bindDescription();
        resetTapState();
    }

    /** 说明里的应用名单独使用 guide_default_name 和 guide_default_name_size。 */
    private void bindDescription() {
        String name = getString(R.string.guide_app_name);
        String full = getString(R.string.guide_default_desc, name);
        SpannableString text = new SpannableString(full);
        int start = full.indexOf(name);
        if (start >= 0) {
            int end = start + name.length();
            int color = ContextCompat.getColor(requireContext(), R.color.guide_default_name);
            int sizePx = getResources().getDimensionPixelSize(R.dimen.guide_default_name_size);
            text.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new AbsoluteSizeSpan(sizePx), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.tvDesc.setText(text);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigator == null || navigator.isGuidePageIdle()) {
            startTapLoop();
        }
    }

    @Override
    public void setPageSettled(boolean settled) {
        if (settled && isResumed()) {
            startTapLoop();
        } else {
            stopTapLoop();
        }
    }

    @Override
    public void onPause() {
        stopTapLoop();
        super.onPause();
    }

    /** 已是默认桌面才进入下一页，否则重新拉起系统设置。 */
    private void continueIfDefault() {
        if (navigator == null) {
            return;
        }
        if (DefaultLauncher.isDefault(requireContext())) {
            AppLog.i("GuideDefaultFragment", "already default, go next");
            navigator.openGuidePage(GuidePagerAdapter.PAGE_NOTIFY);
            return;
        }
        AppLog.i("GuideDefaultFragment", "request default launcher");
        stopTapLoop();
        CacheUtils.setGuidePage(GuidePagerAdapter.PAGE_DEFAULT);
        CacheUtils.setGuideContinueNotify(true);
        FragmentActivity activity = requireActivity();
        DefaultLauncher.requestKeepingGuide(activity, success -> activity.runOnUiThread(() -> {
            if (!success) {
                CacheUtils.setGuideContinueNotify(false);
                return;
            }
            CacheUtils.setGuideContinueNotify(false);
            if (isAdded() && navigator != null) {
                navigator.openGuidePage(GuidePagerAdapter.PAGE_NOTIFY);
            }
        }));
    }

    private void resetTapState() {
        if (binding == null) {
            return;
        }
        binding.ivHand.setAlpha(0f);
        binding.cardBorderGlow.setAlpha(0f);
        binding.radioDot.setAlpha(0f);
        binding.radioDot.setScaleX(0.2f);
        binding.radioDot.setScaleY(0.2f);
        binding.radioRipple.setAlpha(0f);
        binding.radioRipple.setScaleX(0.5f);
        binding.radioRipple.setScaleY(0.5f);
    }

    private void startTapLoop() {
        if (binding == null) {
            return;
        }
        stopTapLoop();
        View hand = binding.ivHand;
        hand.post(() -> {
            if (binding == null || !isResumed() || (navigator != null && !navigator.isGuidePageIdle())) {
                return;
            }
            hand.setPivotX(hand.getWidth() * 0.28f);
            hand.setPivotY(hand.getHeight() * 0.18f);
            startBreath();
            playTapLoop();
        });
    }

    private void playTapLoop() {
        if (binding == null) {
            return;
        }
        View hand = binding.ivHand;
        View dot = binding.radioDot;
        View ripple = binding.radioRipple;
        float dxIn = dp(18);
        float dyIn = dp(25);
        float pressX = dp(-4);
        float pressY = dp(-7);
        float releaseX = dp(1);
        float releaseY = dp(2);
        float outX = dp(13);
        float outY = dp(16);

        ObjectAnimator handX = keyframes(hand, View.TRANSLATION_X,
                0f, dxIn, 0.14f, dxIn, 0.20f, 0f, 0.27f, 0f,
                0.36f, pressX, 0.43f, releaseX, 0.54f, releaseX, 0.65f, outX, 1f, outX);
        ObjectAnimator handY = keyframes(hand, View.TRANSLATION_Y,
                0f, dyIn, 0.14f, dyIn, 0.20f, 0f, 0.27f, 0f,
                0.36f, pressY, 0.43f, releaseY, 0.54f, releaseY, 0.65f, outY, 1f, outY);
        ObjectAnimator handScaleX = keyframes(hand, View.SCALE_X,
                0f, 0.5f, 0.14f, 0.5f, 0.20f, 1.1f, 0.27f, 1f,
                0.36f, 0.88f, 0.43f, 1f, 0.54f, 1f, 0.65f, 0.8f, 1f, 0.8f);
        ObjectAnimator handScaleY = keyframes(hand, View.SCALE_Y,
                0f, 0.5f, 0.14f, 0.5f, 0.20f, 1.1f, 0.27f, 1f,
                0.36f, 0.88f, 0.43f, 1f, 0.54f, 1f, 0.65f, 0.8f, 1f, 0.8f);
        ObjectAnimator handAlpha = keyframes(hand, View.ALPHA,
                0f, 0f, 0.14f, 0f, 0.20f, 1f, 0.54f, 1f, 0.65f, 0f, 1f, 0f);

        ObjectAnimator dotScaleX = keyframes(dot, View.SCALE_X,
                0f, 0.2f, 0.34f, 0.2f, 0.43f, 1f, 0.85f, 1f, 1f, 0.2f);
        ObjectAnimator dotScaleY = keyframes(dot, View.SCALE_Y,
                0f, 0.2f, 0.34f, 0.2f, 0.43f, 1f, 0.85f, 1f, 1f, 0.2f);
        ObjectAnimator dotAlpha = keyframes(dot, View.ALPHA,
                0f, 0f, 0.34f, 0f, 0.43f, 1f, 0.85f, 1f, 1f, 0f);

        ObjectAnimator rippleScaleX = keyframes(ripple, View.SCALE_X,
                0f, 0.5f, 0.33f, 0.5f, 0.36f, 0.7f, 0.53f, 3.2f, 1f, 3.2f);
        ObjectAnimator rippleScaleY = keyframes(ripple, View.SCALE_Y,
                0f, 0.5f, 0.33f, 0.5f, 0.36f, 0.7f, 0.53f, 3.2f, 1f, 3.2f);
        ObjectAnimator rippleAlpha = keyframes(ripple, View.ALPHA,
                0f, 0f, 0.33f, 0f, 0.36f, 1f, 0.53f, 0f, 1f, 0f);
        ObjectAnimator borderAlpha = keyframes(binding.cardBorderGlow, View.ALPHA,
                0f, 0f, 0.34f, 0f, 0.43f, 1f, 0.62f, 0f, 1f, 0f);

        Animator[] animators = {
                handX, handY, handScaleX, handScaleY, handAlpha,
                dotScaleX, dotScaleY, dotAlpha,
                rippleScaleX, rippleScaleY, rippleAlpha,
                borderAlpha
        };
        tapAnimators.clear();
        for (Animator animator : animators) {
            tapAnimators.add(animator);
            animator.start();
        }
    }

    private void startBreath() {
        if (binding == null) {
            return;
        }
        View breath = binding.cardBreath;
        breath.setAlpha(0.35f);
        ObjectAnimator animator = ObjectAnimator.ofFloat(breath, View.ALPHA, 0.35f, 1f, 0.35f);
        animator.setDuration(3600L);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        tapAnimators.add(animator);
        animator.start();
    }

    private void stopTapLoop() {
        for (Animator animator : tapAnimators) {
            animator.cancel();
        }
        tapAnimators.clear();
    }

    private ObjectAnimator keyframes(View target, android.util.Property<View, Float> property,
                                     float... fractionValue) {
        Keyframe[] frames = new Keyframe[fractionValue.length / 2];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = Keyframe.ofFloat(fractionValue[i * 2], fractionValue[i * 2 + 1]);
        }
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofKeyframe(property, frames));
        animator.setDuration(CYCLE_MS);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    public void onDestroyView() {
        stopTapLoop();
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        navigator = null;
        super.onDetach();
    }
}
