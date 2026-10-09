package com.demo.app.launcher.ui.adapter.discover;

import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.demo.app.launcher.ads.SplashFeedPlaceholder;
import com.demo.app.launcher.gamecenter.model.GameItemUi;
import com.demo.app.launcher.ui.activity.discover.DiscoverDetailActivity;

import java.util.Locale;

/** Discover 列表里共用的图标加载和点击。 */
final class DiscoverItemBinder {

    private DiscoverItemBinder() {
    }

    static void openLink(@NonNull android.view.View view, @NonNull GameItemUi item) {
        if (!item.getBid().isEmpty()) {
            android.content.Context ctx = view.getContext();
            SplashFeedPlaceholder.runFromContext(
                    ctx,
                    titleOf(item),
                    () -> DiscoverDetailActivity.start(ctx, item.getBid()));
            return;
        }
        String link = item.getLink();
        if (link.isEmpty()) {
            return;
        }
        try {
            view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (Exception ignored) {
        }
    }

    static void loadIcon(@NonNull ImageView image, @NonNull GameItemUi item) {
        String url = item.getIconUrl().isEmpty() ? item.getImageUrl() : item.getIconUrl();
        Glide.with(image).load(url).centerCrop().into(image);
        image.setClipToOutline(true);
    }

    static void loadBanner(@NonNull ImageView image, @NonNull GameItemUi item) {
        String url = item.getBannerUrl().isEmpty() ? item.getImageUrl() : item.getBannerUrl();
        if (url.isEmpty()) {
            url = item.getIconUrl();
        }
        Glide.with(image).load(url).centerCrop().into(image);
        image.setClipToOutline(true);
    }

    @NonNull
    static String titleOf(@NonNull GameItemUi item) {
        return item.getDisplayTitle().isEmpty() ? item.getName() : item.getDisplayTitle();
    }

    @NonNull
    static String ratingOf(@NonNull GameItemUi item) {
        if (item.getRating() <= 0) {
            return "";
        }
        return String.format(Locale.US, "%.1f", item.getRating());
    }
}
