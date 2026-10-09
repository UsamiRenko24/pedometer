package com.demo.app.launcher.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;
import com.demo.app.launcher.ui.activity.VolumeHomeLauncherActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import m9e778y.LauncherSDK;

/** 固定 5 格。第一格打开音量控制，其余是预测应用，空位用浅色占位。 */
public class Screen0PredictedAppsCardView extends FrameLayout {

    public static final int PREDICTED_SLOT_COUNT = 4;
    private static final int SLOT_COUNT = PREDICTED_SLOT_COUNT + 1;

    private final Slot[] slots;
    @Nullable
    private View.OnLongClickListener volumeControlLongClick;
    @Nullable
    private PopupWindow activePopup;
    public Screen0PredictedAppsCardView(@NonNull Context context) {
        this(context, null);
    }

    public Screen0PredictedAppsCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Screen0PredictedAppsCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClipChildren(false);
        setClipToPadding(false);
        LayoutInflater.from(context).inflate(R.layout.view_screen0_predicted_apps, this, true);
        slots = new Slot[]{
                bindSlot(R.id.screen0_predicted_slot_0),
                bindSlot(R.id.screen0_predicted_slot_1),
                bindSlot(R.id.screen0_predicted_slot_2),
                bindSlot(R.id.screen0_predicted_slot_3),
                bindSlot(R.id.screen0_predicted_slot_4),
        };
        bindSelf(slots[0]);
        for (int i = 1; i < SLOT_COUNT; i++) {
            bindEmpty(slots[i]);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        dismissPopup();
        super.onDetachedFromWindow();
    }

    /** 仅第一格（音量控制）：Home Settings / Widgets。 */
    public void setVolumeControlLongClickListener(@Nullable View.OnLongClickListener listener) {
        volumeControlLongClick = listener;
        applyLongClick(slots[0].root, listener);
    }

    public void submitPredicted(@NonNull List<LauncherSDK.PredictedAppInfo> predicted) {
        dismissPopup();
        String selfPackage = getContext().getPackageName();
        Map<String, LauncherSDK.PredictedAppInfo> unique = new LinkedHashMap<>();
        for (LauncherSDK.PredictedAppInfo info : predicted) {
            if (info.packageName == null || info.packageName.isEmpty()
                    || selfPackage.equals(info.packageName) || unique.containsKey(info.packageName)) {
                continue;
            }
            unique.put(info.packageName, info);
            if (unique.size() >= PREDICTED_SLOT_COUNT) {
                break;
            }
        }
        List<LauncherSDK.PredictedAppInfo> filtered = new ArrayList<>(unique.values());
        bindSelf(slots[0]);
        for (int i = 0; i < PREDICTED_SLOT_COUNT; i++) {
            if (i < filtered.size()) {
                bindPredicted(slots[i + 1], filtered.get(i));
            } else {
                bindEmpty(slots[i + 1]);
            }
        }
        setVolumeControlLongClickListener(volumeControlLongClick);
    }

    private Slot bindSlot(int rootId) {
        View root = findViewById(rootId);
        return new Slot(
                root,
                root.findViewById(R.id.screen0_predicted_icon),
                root.findViewById(R.id.screen0_predicted_title));
    }

    private void bindSelf(@NonNull Slot slot) {
        slot.root.setAlpha(1f);
        slot.icon.setBackgroundResource(0);
        slot.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        slot.icon.setImageResource(R.drawable.spalsh_round);
        slot.title.setText(R.string.guide_app_name);
        slot.root.setOnClickListener(v -> {
            Context context = v.getContext();
            VolumeHomeLauncherActivity.start(context);
        });
        setVolumeControlLongClickListener(volumeControlLongClick);
        slot.root.setClickable(true);
        slot.root.setFocusable(true);
    }

    private void bindPredicted(@NonNull Slot slot, @NonNull LauncherSDK.PredictedAppInfo info) {
        slot.root.setAlpha(info.disabled ? 0.4f : 1f);
        slot.icon.setBackgroundResource(0);
        slot.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (info.icon != null) {
            slot.icon.setImageBitmap(info.icon);
        } else {
            slot.icon.setImageResource(R.drawable.icon_home_app_folder);
        }
        slot.title.setText(info.appName == null ? "" : info.appName);
        if (info.disabled || info.intent == null) {
            slot.root.setOnClickListener(null);
            clearAppInfoLongClick(slot);
            return;
        }
        slot.root.setOnClickListener(v -> {
            Intent intent = new Intent(info.intent);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(intent);
        });
        bindAppInfoLongClick(slot, info.packageName);
        slot.root.setClickable(true);
        slot.root.setFocusable(true);
    }

    private void bindEmpty(@NonNull Slot slot) {
        slot.root.setAlpha(1f);
        slot.icon.setImageDrawable(null);
        slot.icon.setBackgroundResource(R.drawable.bg_screen0_predicted_placeholder);
        slot.title.setText("");
        slot.root.setOnClickListener(null);
        clearAppInfoLongClick(slot);
        slot.root.setClickable(false);
        slot.root.setFocusable(false);
    }

    private static void applyLongClick(@NonNull View view, @Nullable View.OnLongClickListener listener) {
        view.setOnLongClickListener(listener);
        view.setLongClickable(listener != null);
        view.setHapticFeedbackEnabled(listener != null);
    }

    private void clearAppInfoLongClick(@NonNull Slot slot) {
        slot.root.setOnLongClickListener(null);
        slot.root.setOnTouchListener(null);
        slot.root.setLongClickable(false);
    }

    private void bindAppInfoLongClick(@NonNull Slot slot, @Nullable String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            clearAppInfoLongClick(slot);
            return;
        }
        final float touchSlop = ViewConfiguration.get(slot.root.getContext()).getScaledTouchSlop();
        final float[] down = new float[2];
        slot.root.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    down[0] = event.getX();
                    down[1] = event.getY();
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = Math.abs(event.getX() - down[0]);
                    float dy = Math.abs(event.getY() - down[1]);
                    if ((dx > touchSlop && dx >= dy) || (dy > touchSlop && dy > dx)) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                default:
                    break;
            }
            return false;
        });
        slot.root.setOnLongClickListener(v -> {
            showAppInfoPopup(slot.icon, packageName);
            return true;
        });
        slot.root.setLongClickable(true);
    }

    private void showAppInfoPopup(@NonNull View anchor, @NonNull String packageName) {
        dismissPopup();
        View content = LayoutInflater.from(anchor.getContext())
                .inflate(R.layout.popup_screen0_predicted_app_action, null, false);
        View caret = content.findViewById(R.id.predicted_popup_caret);
        View card = content.findViewById(R.id.predicted_popup_card);
        content.findViewById(R.id.predicted_popup_app_info).setOnClickListener(v -> {
            dismissPopup();
            openAppDetails(anchor.getContext(), packageName);
        });

        content.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int popupWidth = content.getMeasuredWidth();
        int popupHeight = content.getMeasuredHeight();
        content.layout(0, 0, popupWidth, popupHeight);

        int gap = dp(2);
        int edgeDim = getResources().getDimensionPixelSize(R.dimen.sw_dp_8);
        int edge = edgeDim > 0 ? edgeDim : dp(8);

        int[] anchorLoc = new int[2];
        int[] cardFrameLoc = new int[2];
        anchor.getLocationInWindow(anchorLoc);
        View predictedFrame = findViewById(R.id.screen0_predicted_apps_card);
        if (predictedFrame == null) {
            predictedFrame = this;
        }
        predictedFrame.getLocationInWindow(cardFrameLoc);
        int windowW = anchor.getRootView().getWidth();
        if (windowW <= 0) {
            windowW = getResources().getDisplayMetrics().widthPixels;
        }

        int idealX = anchorLoc[0] + (anchor.getWidth() - popupWidth) / 2;
        int idealY = cardFrameLoc[1] - popupHeight - gap;
        int maxX = Math.max(edge, windowW - popupWidth - edge);
        int x = Math.max(edge, Math.min(idealX, maxX));
        int y = Math.max(edge, idealY);
        float caretShift = idealX - x;

        PopupWindow popup = new PopupWindow(
                content,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setElevation(dp(9));
        popup.setOnDismissListener(() -> activePopup = null);

        applyCaretShift(card, caret, caretShift);
        activePopup = popup;
        popup.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
        content.post(() -> applyCaretShift(card, caret, caretShift));

        content.setPivotX(popupWidth / 2f);
        content.setPivotY(popupHeight);
        content.setAlpha(0f);
        content.setScaleX(0.94f);
        content.setScaleY(0.94f);
        content.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(140L)
                .withEndAction(() -> applyCaretShift(card, caret, caretShift))
                .start();
    }

    private void applyCaretShift(@NonNull View card, @NonNull View caret, float shift) {
        int pad = getResources().getDimensionPixelSize(R.dimen.sw_dp_6);
        if (pad == 0) {
            pad = dp(6);
        }
        float minTx = (card.getLeft() + pad) - caret.getLeft();
        float maxTx = (card.getLeft() + card.getWidth() - pad - caret.getWidth()) - caret.getLeft();
        caret.setTranslationX(maxTx >= minTx ? Math.max(minTx, Math.min(shift, maxTx)) : shift);
    }

    private void dismissPopup() {
        if (activePopup != null) {
            activePopup.dismiss();
            activePopup = null;
        }
    }

    private static void openAppDetails(@NonNull Context context, @NonNull String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", packageName, null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Throwable ignored) {
            Toast.makeText(context, packageName, Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class Slot {
        final View root;
        final ImageView icon;
        final TextView title;

        Slot(View root, ImageView icon, TextView title) {
            this.root = root;
            this.icon = icon;
            this.title = title;
        }
    }
}
