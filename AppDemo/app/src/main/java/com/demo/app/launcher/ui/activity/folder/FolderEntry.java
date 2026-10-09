package com.demo.app.launcher.ui.activity.folder;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.demo.app.R;

import java.util.Locale;

/** 一条本地文件。 */
public final class FolderEntry {

    public final Uri uri;
    public final String name;
    public final long size;
    public final long modifiedMillis;
    public final String mime;
    public final FolderKind kind;

    public FolderEntry(@NonNull Uri uri, @NonNull String name, long size, long modifiedMillis,
                       @NonNull String mime, @NonNull FolderKind kind) {
        this.uri = uri;
        this.name = name;
        this.size = size;
        this.modifiedMillis = modifiedMillis;
        this.mime = mime;
        this.kind = kind;
    }

    @NonNull
    public String meta(@NonNull Context context) {
        return extension() + " · " + sizeText(size) + " · " + ago(context, modifiedMillis);
    }

    @NonNull
    private String extension() {
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot < name.length() - 1) {
            return name.substring(dot + 1).toUpperCase(Locale.US);
        }
        if (mime.startsWith("image/")) {
            return "JPG";
        }
        if (mime.startsWith("video/")) {
            return "MP4";
        }
        if (mime.startsWith("audio/")) {
            return "MP3";
        }
        return "FILE";
    }

    @NonNull
    public static String sizeText(long bytes) {
        if (bytes >= 1_000_000_000L) {
            return String.format(Locale.US, "%.1fGB", bytes / 1_000_000_000d);
        }
        if (bytes >= 1_000_000L) {
            return String.format(Locale.US, "%.1fMB", bytes / 1_000_000d);
        }
        if (bytes >= 1_000L) {
            return String.format(Locale.US, "%.1fKB", bytes / 1_000d);
        }
        return bytes + "B";
    }

    @NonNull
    public static String ago(@NonNull Context context, long modifiedMillis) {
        long delta = Math.max(0L, System.currentTimeMillis() - modifiedMillis);
        long minutes = delta / 60_000L;
        if (minutes < 1) {
            return context.getString(R.string.folder_just_now);
        }
        if (minutes < 60) {
            return context.getString(R.string.folder_minutes_ago, minutes);
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours == 1
                    ? context.getString(R.string.folder_hour_ago)
                    : context.getString(R.string.folder_hours_ago, hours);
        }
        long days = hours / 24;
        return days == 1
                ? context.getString(R.string.folder_day_ago)
                : context.getString(R.string.folder_days_ago, days);
    }

    public static boolean isCompressed(@NonNull String name) {
        String lower = name.toLowerCase(Locale.US);
        return lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")
                || lower.endsWith(".tar") || lower.endsWith(".gz");
    }

    public static boolean isApk(@NonNull String name, @NonNull String mime) {
        return mime.equals("application/vnd.android.package-archive")
                || name.toLowerCase(Locale.US).endsWith(".apk");
    }

    @NonNull
    public static String mimeOrEmpty(String mime) {
        return TextUtils.isEmpty(mime) ? "" : mime;
    }
}
