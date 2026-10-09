package com.demo.app.launcher.news;

import androidx.annotation.Nullable;

public final class NewsArticle {
    public String title = "";
    public String link = "";
    public String pub_date = "";
    public String description = "";
    public String source_name = "";
    public String source_url = "";
    public String img_url = "";
    public String tag = "";

    public boolean isVideo() {
        String value = ((link == null ? "" : link) + " " + (source_name == null ? "" : source_name))
                .toLowerCase();
        return value.contains("video")
                || value.contains("youtu")
                || value.contains("tiktok")
                || value.contains(".mp4")
                || value.contains("buzz");
    }

    @Nullable
    public String imageUrl() {
        if (img_url == null) {
            return null;
        }
        String url = img_url.trim();
        int split = url.indexOf("||");
        if (split >= 0) {
            url = url.substring(0, split).trim();
        }
        return url.isEmpty() ? null : url;
    }
}
