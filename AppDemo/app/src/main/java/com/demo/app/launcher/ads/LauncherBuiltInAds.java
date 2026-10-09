package com.demo.app.launcher.ads;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** 内置页固定广告位显隐（占位模式下一律展示）。 */
public final class LauncherBuiltInAds {

    private LauncherBuiltInAds() {
    }

    public static void show(@Nullable View slot) {
        if (slot == null) {
            return;
        }
        slot.setVisibility(LauncherAds.shouldShowPlaceholder() ? View.VISIBLE : View.GONE);
    }

    public static void bindBottomBanner(@Nullable LauncherBottomBannerPlaceholderView banner) {
        if (banner != null) {
            banner.refreshVisibility();
        }
    }

    public static void bindNativeSlot(@Nullable LauncherNativeAdPlaceholderView nativeSlot) {
        if (nativeSlot != null) {
            nativeSlot.refreshVisibility();
        }
    }

    public static void bindAnyBanner(@NonNull View bannerRoot) {
        bannerRoot.setVisibility(LauncherAds.shouldShowPlaceholder() ? View.VISIBLE : View.GONE);
    }
}
