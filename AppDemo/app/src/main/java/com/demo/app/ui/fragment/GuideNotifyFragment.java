package com.demo.app.ui.fragment;

import android.Manifest;
import android.animation.Animator;
import android.content.pm.PackageManager;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.demo.app.R;
import com.demo.app.databinding.FragmentGuideNotificationBinding;
import com.demo.app.ui.guide.GuideNavigator;
import com.demo.app.ui.guide.GuidePageMotion;
import com.demo.app.utils.AppLog;

import java.util.ArrayList;
import java.util.List;

/** 引导第 3 页。点按钮后弹出通知说明，Proceed 再申请系统通知权限。 */
public class GuideNotifyFragment extends Fragment implements GuidePageMotion {

    private static final long BELL_CYCLE_MS = 4000L;
    /** 铃铛开始摆动之后，音波才从这里出现，并在这一轮里传完。 */
    private static final float WAVE_START = 0.26f;
    private static final float WAVE_SPAN = 0.42f;
    private static final float WAVE_STAGGER = 0.08f;

    private FragmentGuideNotificationBinding binding;
    private GuideNavigator navigator;
    private final List<Animator> motionAnimators = new ArrayList<>();
    private Dialog notifySheet;
    private boolean awaitingSettings;
    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> goNext());

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
        binding = FragmentGuideNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnEnable.setOnClickListener(v -> {
            boolean enabled = notificationsEnabled();
            AppLog.i("GuideNotifyFragment", enabled ? "notifications already on" : "show notify sheet");
            if (enabled) {
                goNext();
            } else {
                showNotifySheet();
            }
        });
        for (View arc : arcs()) {
            arc.setAlpha(0f);
            arc.setPivotX(0f);
            arc.setPivotY(arc.getLayoutParams().height / 2f);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (awaitingSettings) {
            awaitingSettings = false;
            goNext();
            return;
        }
        if (navigator == null || navigator.isGuidePageIdle()) {
            startMotion();
        }
    }

    @Override
    public void setPageSettled(boolean settled) {
        if (settled && isResumed()) {
            startMotion();
        } else {
            stopMotion();
            dismissNotifySheet();
        }
    }

    @Override
    public void onPause() {
        stopMotion();
        super.onPause();
    }

    private void showNotifySheet() {
        if (!isAdded() || (notifySheet != null && notifySheet.isShowing())) {
            return;
        }
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_guide_notify_sheet);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.55f);
        }
        dialog.findViewById(R.id.btn_proceed).setOnClickListener(v -> requestNotifications());
        dialog.setOnDismissListener(d -> {
            if (notifySheet == dialog) {
                notifySheet = null;
            }
        });
        notifySheet = dialog;
        dialog.show();
    }

    private void requestNotifications() {
        dismissNotifySheet();
        if (needsNotificationPermission()) {
            View root = binding == null ? null : binding.getRoot();
            if (root != null) {
                root.post(() -> {
                    if (isAdded()) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                });
            }
            return;
        }
        if (!notificationsEnabled()) {
            awaitingSettings = true;
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
            startActivity(intent);
            return;
        }
        goNext();
    }

    private boolean needsNotificationPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED;
    }

    private boolean notificationsEnabled() {
        if (needsNotificationPermission()) {
            return false;
        }
        return NotificationManagerCompat.from(requireContext()).areNotificationsEnabled();
    }

    private void goNext() {
        if (navigator != null) {
            navigator.goNextGuidePage();
        }
    }

    private void dismissNotifySheet() {
        if (notifySheet != null) {
            notifySheet.dismiss();
            notifySheet = null;
        }
    }

    private void startMotion() {
        if (binding == null) {
            return;
        }
        stopMotion();
        View bell = binding.bellGroup;
        bell.post(() -> {
            if (binding == null || !isResumed() || (navigator != null && !navigator.isGuidePageIdle())) {
                return;
            }
            bell.setPivotX(bell.getWidth() * 0.5f);
            bell.setPivotY(bell.getHeight() * 0.2f);
            playMotion();
        });
    }

    private void playMotion() {
        if (binding == null) {
            return;
        }
        ObjectAnimator bell = keyframes(binding.bellGroup, View.ROTATION, BELL_CYCLE_MS, 0,
                0f, 0f, 0.15f, 0f, 0.24f, 2f, 0.33f, -2f, 0.42f, 1f, 0.51f, -1f, 0.65f, 0f, 1f, 0f);
        bell.setInterpolator(new AccelerateDecelerateInterpolator());

        View[] arcs = arcs();
        motionAnimators.clear();
        motionAnimators.add(bell);
        bell.start();
        for (int i = 0; i < arcs.length; i++) {
            View arc = arcs[i];
            arc.setPivotX(0f);
            arc.setPivotY(arc.getHeight() / 2f);
            float start = WAVE_START + i * WAVE_STAGGER;
            float end = start + WAVE_SPAN;
            float travel = dp(40);
            ObjectAnimator move = keyframes(arc, View.TRANSLATION_X, BELL_CYCLE_MS, 0L,
                    0f, 0f, start, 0f, end, travel, 1f, 0f);
            ObjectAnimator scaleX = keyframes(arc, View.SCALE_X, BELL_CYCLE_MS, 0L,
                    0f, 0.72f, start, 0.72f, end, 1.45f, 1f, 0.72f);
            ObjectAnimator scaleY = keyframes(arc, View.SCALE_Y, BELL_CYCLE_MS, 0L,
                    0f, 0.72f, start, 0.72f, end, 1.55f, 1f, 0.72f);
            ObjectAnimator alpha = keyframes(arc, View.ALPHA, BELL_CYCLE_MS, 0L,
                    0f, 0f,
                    start, 0f,
                    start + 0.18f * WAVE_SPAN, 0.85f,
                    start + 0.72f * WAVE_SPAN, 0.35f,
                    end, 0f,
                    1f, 0f);
            move.setInterpolator(new LinearInterpolator());
            scaleX.setInterpolator(new LinearInterpolator());
            scaleY.setInterpolator(new LinearInterpolator());
            alpha.setInterpolator(new LinearInterpolator());
            motionAnimators.add(move);
            motionAnimators.add(scaleX);
            motionAnimators.add(scaleY);
            motionAnimators.add(alpha);
            move.start();
            scaleX.start();
            scaleY.start();
            alpha.start();
        }
    }

    private ObjectAnimator keyframes(View target, android.util.Property<View, Float> property,
                                     long duration, long startDelay, float... fractionValue) {
        Keyframe[] frames = new Keyframe[fractionValue.length / 2];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = Keyframe.ofFloat(fractionValue[i * 2], fractionValue[i * 2 + 1]);
        }
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofKeyframe(property, frames));
        animator.setDuration(duration);
        animator.setStartDelay(startDelay);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        return animator;
    }

    private View[] arcs() {
        return new View[]{binding.waveArc1, binding.waveArc2, binding.waveArc3};
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private void stopMotion() {
        for (Animator animator : motionAnimators) {
            animator.cancel();
        }
        motionAnimators.clear();
    }

    @Override
    public void onDestroyView() {
        dismissNotifySheet();
        stopMotion();
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        navigator = null;
        super.onDetach();
    }
}
