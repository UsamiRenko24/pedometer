package com.demo.app.launcher.ads;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;

/**
 * RC100 TopOn 广告位的占位开关；接入真实 SDK 后在此统一切换。
 */
public final class LauncherAds {

    /** true：各广告容器展示灰色占位；false：隐藏占位（待 SDK 填充）。 */
    public static final boolean PLACEHOLDER_MODE = true;

    private LauncherAds() {
    }

    public static boolean shouldShowPlaceholder() {
        return PLACEHOLDER_MODE;
    }

    /** 在容器内展示标准灰色 AD 条（下拉搜索 / Discover 等同款）。 */
    public static void bindBlockPlaceholder(@NonNull ViewGroup container) {
        container.removeAllViews();
        if (!shouldShowPlaceholder()) {
            container.setVisibility(View.GONE);
            return;
        }
        LayoutInflater.from(container.getContext())
                .inflate(R.layout.include_launcher_ad_placeholder, container, true);
        container.setVisibility(View.VISIBLE);
    }

    /** 0 屏选中：对齐 RC100 预加载活跃开屏 / 抽屉 Native（占位阶段无 SDK 调用）。 */
    public static void onScreen0Selected(@NonNull Context context) {
        if (!shouldShowPlaceholder()) {
            return;
        }
        // Reserved for TopOn preloadLauncherActiveSplashAdvert / preloadAppDrawerNativeAdvert.
    }

    public static void setVisible(@Nullable View adSlot, boolean visible) {
        if (adSlot == null) {
            return;
        }
        if (!shouldShowPlaceholder()) {
            adSlot.setVisibility(View.GONE);
            return;
        }
        adSlot.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
