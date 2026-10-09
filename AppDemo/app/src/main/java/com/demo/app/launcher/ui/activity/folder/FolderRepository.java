package com.demo.app.launcher.ui.activity.folder;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 读取媒体库和存储容量。没有权限时对应分类返回空列表。 */
public final class FolderRepository {

    public static final class Volume {
        public final long total;
        public final long used;

        Volume(long total, long used) {
            this.total = total;
            this.used = used;
        }
    }

    public static final class Snapshot {
        @NonNull
        public final List<FolderEntry> files;
        @NonNull
        public final Volume phone;
        @NonNull
        public final Volume sd;

        Snapshot(@NonNull List<FolderEntry> files, @NonNull Volume phone, @NonNull Volume sd) {
            this.files = files;
            this.phone = phone;
            this.sd = sd;
        }
    }

    public interface Callback {
        void onLoaded(@NonNull Snapshot snapshot);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final String[] PROJECTION = {
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.MIME_TYPE
    };
    private static final String ORDER = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";

    private FolderRepository() {
    }

    public static void load(@NonNull Context context, @NonNull Callback callback) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            Snapshot snapshot = read(app);
            MAIN.post(() -> callback.onLoaded(snapshot));
        });
    }

    @NonNull
    private static Snapshot read(@NonNull Context context) {
        LinkedHashMap<String, FolderEntry> unique = new LinkedHashMap<>();
        query(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, FolderKind.IMAGE, unique);
        query(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, FolderKind.VIDEO, unique);
        query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, FolderKind.AUDIO, unique);
        query(context, MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, null, FolderKind.DOWNLOAD, unique);
        query(context, MediaStore.Files.getContentUri("external"),
                MediaStore.MediaColumns.MIME_TYPE + " LIKE ? OR "
                        + MediaStore.MediaColumns.MIME_TYPE + " LIKE ? OR "
                        + MediaStore.MediaColumns.MIME_TYPE + " LIKE ? OR "
                        + MediaStore.MediaColumns.MIME_TYPE + " LIKE ? OR "
                        + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ?",
                new String[]{"application/pdf", "text/%", "application/msword%",
                        "application/vnd.openxmlformats%", "%.txt"},
                FolderKind.DOCUMENT, unique);
        query(context, MediaStore.Files.getContentUri("external"),
                MediaStore.MediaColumns.MIME_TYPE + "=? OR "
                        + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ?",
                new String[]{"application/vnd.android.package-archive", "%.apk"},
                FolderKind.APK, unique);
        query(context, MediaStore.Files.getContentUri("external"),
                MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ? OR "
                        + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ? OR "
                        + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ? OR "
                        + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ? OR "
                        + MediaStore.MediaColumns.DISPLAY_NAME + " LIKE ?",
                new String[]{"%.zip", "%.rar", "%.7z", "%.tar", "%.gz"},
                FolderKind.COMPRESSED, unique);
        List<FolderEntry> files = new ArrayList<>(unique.values());
        files.sort(Comparator.comparingLong((FolderEntry entry) -> entry.modifiedMillis).reversed());
        return new Snapshot(files, volume(context, true), volume(context, false));
    }

    private static void query(@NonNull Context context, @NonNull Uri collection,
                              @Nullable String selection, @Nullable String[] args,
                              @NonNull FolderKind kind, @NonNull LinkedHashMap<String, FolderEntry> out) {
        try (Cursor cursor = context.getContentResolver().query(
                collection, PROJECTION, selection, args, ORDER)) {
            if (cursor == null) {
                return;
            }
            int idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
            int nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
            int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
            int timeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED);
            int mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);
            int count = 0;
            while (cursor.moveToNext() && count < 200) {
                long id = cursor.getLong(idCol);
                String name = cursor.getString(nameCol);
                if (name == null || name.isEmpty()) {
                    continue;
                }
                String mime = FolderEntry.mimeOrEmpty(cursor.getString(mimeCol));
                FolderKind resolved = kind;
                if (kind == FolderKind.DOWNLOAD && FolderEntry.isApk(name, mime)) {
                    resolved = FolderKind.APK;
                } else if (kind == FolderKind.DOWNLOAD && FolderEntry.isCompressed(name)) {
                    resolved = FolderKind.COMPRESSED;
                }
                Uri uri = ContentUris.withAppendedId(collection, id);
                out.putIfAbsent(uri.toString(), new FolderEntry(
                        uri,
                        name,
                        cursor.isNull(sizeCol) ? 0L : cursor.getLong(sizeCol),
                        cursor.getLong(timeCol) * 1000L,
                        mime,
                        resolved));
                count++;
            }
        } catch (RuntimeException ignored) {
            // 没授权或这台机器没有对应集合时跳过这一类。
        }
    }

    @NonNull
    private static Volume volume(@NonNull Context context, boolean primary) {
        StorageManager manager = context.getSystemService(StorageManager.class);
        if (manager != null) {
            for (StorageVolume storage : manager.getStorageVolumes()) {
                boolean match = primary ? storage.isPrimary() : storage.isRemovable();
                if (!match) {
                    continue;
                }
                File dir = storage.getDirectory();
                if (dir != null) {
                    return stat(dir);
                }
            }
        }
        if (primary) {
            return stat(Environment.getExternalStorageDirectory());
        }
        return new Volume(0L, 0L);
    }

    @NonNull
    private static Volume stat(@NonNull File dir) {
        try {
            android.os.StatFs fs = new android.os.StatFs(dir.getAbsolutePath());
            long total = fs.getTotalBytes();
            long used = Math.max(0L, total - fs.getAvailableBytes());
            return new Volume(total, used);
        } catch (RuntimeException ignored) {
            return new Volume(0L, 0L);
        }
    }
}
