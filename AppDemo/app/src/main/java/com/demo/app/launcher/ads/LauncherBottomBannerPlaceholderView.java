package com.demo.app.launcher.ads;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;

/** 对齐 RC100 {@code BottomBannerView} 底栏 Banner 占位。 */
public class LauncherBottomBannerPlaceholderView extends FrameLayout {

    public LauncherBottomBannerPlaceholderView(@NonNull Context context) {
        this(context, null);
    }

    public LauncherBottomBannerPlaceholderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LauncherBottomBannerPlaceholderView(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_bottom_banner_placeholder, this, true);
        refreshVisibility();
    }

    public void refreshVisibility() {
        setVisibility(LauncherAds.shouldShowPlaceholder() ? View.VISIBLE : View.GONE);
    }
}
