package com.demo.app.launcher.ui.activity.folder;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.demo.app.R;

/** 文件分类。压缩包只出现在搜索筛选里。 */
public enum FolderKind {
    IMAGE(R.string.folder_images, R.string.folder_images, R.string.folder_empty_images,
            R.drawable.icon_home_app_folder_images, R.drawable.icon_home_app_folder_image_no_found),
    VIDEO(R.string.folder_video, R.string.folder_video, R.string.folder_empty_video,
            R.drawable.icon_home_app_folder_video, R.drawable.icon_home_app_folder_video_no_found),
    AUDIO(R.string.folder_music, R.string.folder_audio, R.string.folder_empty_audio,
            R.drawable.icon_home_app_folder_music, R.drawable.icon_home_app_folder_audio_no_found),
    DOCUMENT(R.string.folder_files, R.string.folder_document, R.string.folder_empty_document,
            R.drawable.icon_home_app_folder_files, R.drawable.icon_home_app_folder_document_no_found),
    DOWNLOAD(R.string.folder_download, R.string.folder_download, R.string.folder_empty_download,
            R.drawable.icon_home_app_folder_download, R.drawable.icon_home_app_folder_download_no_found),
    APK(R.string.folder_apk, R.string.folder_apk, R.string.folder_empty_apk,
            R.drawable.icon_home_app_folder_apk, R.drawable.icon_home_app_folder_apk_no_found),
    COMPRESSED(R.string.folder_filter_compressed, R.string.folder_filter_compressed, R.string.folder_search_empty,
            R.drawable.icon_home_app_folder_files, R.drawable.icon_home_app_folder_files);

    @StringRes
    public final int homeTitle;
    @StringRes
    public final int pageTitle;
    @StringRes
    public final int emptyText;
    @DrawableRes
    public final int icon;
    @DrawableRes
    public final int emptyIcon;

    FolderKind(@StringRes int homeTitle, @StringRes int pageTitle, @StringRes int emptyText,
               @DrawableRes int icon, @DrawableRes int emptyIcon) {
        this.homeTitle = homeTitle;
        this.pageTitle = pageTitle;
        this.emptyText = emptyText;
        this.icon = icon;
        this.emptyIcon = emptyIcon;
    }

    @NonNull
    public static FolderKind from(@NonNull String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return IMAGE;
        }
    }
}
