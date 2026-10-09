package com.demo.app.launcher.news;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.http.DataRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** 新闻分类和列表。请求走 DataRepository，缓存时间和 RemoteControl100 一致。 */
public final class NewsRepository {

    public interface TagsCallback {
        void onResult(@Nullable List<NewsTag> tags);
    }

    public interface ArticlesCallback {
        void onResult(@Nullable List<NewsArticle> articles, int total);
    }

    private static final int PAGE_SIZE = 10;

    private NewsRepository() {
    }

    public static void tags(@NonNull TagsCallback callback) {
        DataRepository.getNewsTag(NewsStore.area().country, (result, error) ->
                callback.onResult(parseTags(result)));
    }

    public static void articles(@NonNull String tag, int page, @NonNull ArticlesCallback callback) {
        DataRepository.getNews(page, PAGE_SIZE, tag, NewsStore.area().country, (result, error) -> {
            Page parsed = parseArticles(result);
            if (parsed == null) {
                callback.onResult(null, 0);
            } else {
                callback.onResult(parsed.articles, parsed.count);
            }
        });
    }

    @Nullable
    private static List<NewsTag> parseTags(@Nullable String raw) {
        JSONArray data = array(raw);
        if (data == null) {
            return null;
        }
        List<NewsTag> tags = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.optJSONObject(i);
            if (item == null) {
                continue;
            }
            NewsTag tag = new NewsTag();
            tag.country = item.optString("country", "");
            tag.name = item.optString("name", "");
            tag.tag = item.optString("tag", "");
            if (!tag.tag.isEmpty()) {
                tags.add(tag);
            }
        }
        tags.sort((left, right) -> {
            boolean leftTop = isTop(left);
            boolean rightTop = isTop(right);
            if (leftTop == rightTop) {
                return 0;
            }
            return leftTop ? -1 : 1;
        });
        if (tags.isEmpty() || !isTop(tags.get(0))) {
            NewsTag top = new NewsTag();
            top.tag = "top";
            top.name = "Top";
            tags.add(0, top);
        }
        return tags;
    }

    @Nullable
    private static Page parseArticles(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        try {
            JSONObject object = new JSONObject(raw);
            if (object.optInt("code", -1) != 0) {
                return null;
            }
            JSONArray data = object.optJSONArray("data");
            if (data == null) {
                return null;
            }
            List<NewsArticle> articles = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                NewsArticle article = new NewsArticle();
                article.title = item.optString("title", "");
                article.link = item.optString("link", "");
                article.pub_date = item.optString("pub_date", "");
                article.description = item.optString("description", "");
                article.source_name = item.optString("source_name", "");
                article.source_url = item.optString("source_url", "");
                article.img_url = item.optString("img_url", "");
                article.tag = item.optString("tag", "");
                if (article.imageUrl() != null) {
                    articles.add(article);
                }
            }
            if (articles.isEmpty()) {
                return null;
            }
            return new Page(articles, object.optInt("count", articles.size()));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static JSONArray array(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        try {
            JSONObject object = new JSONObject(raw);
            if (object.optInt("code", -1) != 0) {
                return null;
            }
            return object.optJSONArray("data");
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isTop(@NonNull NewsTag tag) {
        return "top".equalsIgnoreCase(tag.tag) || "tops".equalsIgnoreCase(tag.tag);
    }

    private static final class Page {
        final List<NewsArticle> articles;
        final int count;

        Page(List<NewsArticle> articles, int count) {
            this.articles = articles;
            this.count = count;
        }
    }
}
