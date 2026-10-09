package com.demo.app.launcher.ads;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;

/** 对齐 RC100 {@code NativeAdView} 占位（列表/退出弹窗等）。 */
public class LauncherNativeAdPlaceholderView extends FrameLayout {

    public LauncherNativeAdPlaceholderView(@NonNull Context context) {
        this(context, null);
    }

    public LauncherNativeAdPlaceholderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LauncherNativeAdPlaceholderView(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_native_ad_placeholder, this, true);
        refreshVisibility();
    }

    public void refreshVisibility() {
        setVisibility(LauncherAds.shouldShowPlaceholder() ? View.VISIBLE : View.GONE);
    }
}
