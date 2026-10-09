package com.demo.app.launcher.wallpaper;

import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.demo.app.integration.http.RxHttp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;
import okhttp3.Response;

/** 把壁纸设到桌面和锁屏，或保存到相册。 */
public final class WallpaperActions {

    public interface Done {
        void onDone(boolean success);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private WallpaperActions() {
    }

    public static void apply(@NonNull Context context, @NonNull String url, int flags, @NonNull Done done) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            boolean success = false;
            try {
                Bitmap bitmap = Glide.with(app).asBitmap().load(url).submit().get();
                WallpaperManager.getInstance(app).setBitmap(bitmap, null, true, flags);
                success = true;
            } catch (Exception ignored) {
            }
            boolean result = success;
            MAIN.post(() -> done.onDone(result));
        });
    }

    public static void download(@NonNull Context context, @NonNull String url,
                                @NonNull String fileName, @NonNull Done done) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            boolean success = false;
            try {
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = RxHttp.getOrCreateClient().newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        byte[] bytes = response.body().bytes();
                        success = save(app, bytes, fileName);
                    }
                }
            } catch (Exception ignored) {
            }
            boolean result = success;
            MAIN.post(() -> done.onDone(result));
        });
    }

    private static boolean save(@NonNull Context context, @NonNull byte[] bytes, @NonNull String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Wallpaper");
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        android.net.Uri uri = context.getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            return false;
        }
        try (java.io.OutputStream output = context.getContentResolver().openOutputStream(uri)) {
            if (output == null) {
                return false;
            }
            output.write(bytes);
        } catch (Exception exception) {
            return false;
        }
        values.clear();
        values.put(MediaStore.MediaColumns.IS_PENDING, 0);
        context.getContentResolver().update(uri, values, null, null);
        return true;
    }
}
