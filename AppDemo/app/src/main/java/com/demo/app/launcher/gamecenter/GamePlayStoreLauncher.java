package com.demo.app.launcher.gamecenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** 通过 link 或 bid 打开 Google Play 或 H5 试玩链接。 */
public final class GamePlayStoreLauncher {

    private static final String PLAY_STORE_DETAILS = "https://play.google.com/store/apps/details?id=";
    private static final String PLAY_STORE_PACKAGE = "com.android.vending";

    private GamePlayStoreLauncher() {
    }

    public static boolean open(@NonNull Context context, @Nullable String link, @Nullable String bid) {
        String url = link != null ? link.trim() : "";
        if (url.isEmpty() && bid != null && !bid.trim().isEmpty()) {
            url = PLAY_STORE_DETAILS + bid.trim();
        }
        if (url.isEmpty()) {
            return false;
        }
        return openUrl(context, url);
    }

    private static boolean openUrl(@NonNull Context context, @NonNull String url) {
        Uri uri = Uri.parse(url);
        if (url.contains("play.google.com")) {
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, uri);
            playStoreIntent.setPackage(PLAY_STORE_PACKAGE);
            try {
                context.startActivity(playStoreIntent);
                return true;
            } catch (Exception ignored) {
            }
        }
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
