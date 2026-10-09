package com.demo.app.launcher.ui.view.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.launcher.gamecenter.model.GameItemUi;
import com.demo.app.launcher.news.NewsArticle;
import com.demo.app.launcher.wallpaper.model.WallpaperItemUi;

import java.util.List;

/** 搜索首页扁平列表条目（对齐 RemoteControl100 穿插规则）。 */
public abstract class LauncherSearchHomeItem {

    public static final int NEWS_STYLE_HERO = 0;
    public static final int NEWS_STYLE_COMPACT = 1;

    private LauncherSearchHomeItem() {
    }

    public static final class CalendarHeader extends LauncherSearchHomeItem {
    }

    public static final class Ad extends LauncherSearchHomeItem {
    }

    public static final class NewsLoading extends LauncherSearchHomeItem {
    }

    public static final class NewsError extends LauncherSearchHomeItem {
    }

    public static final class GroupTitle extends LauncherSearchHomeItem {
        public final String title;
        @Nullable
        public final String subtitle;

        public GroupTitle(@NonNull String title, @Nullable String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    public static final class News extends LauncherSearchHomeItem {
        @NonNull
        public final NewsArticle article;
        public final int style;

        public News(@NonNull NewsArticle article, int style) {
            this.article = article;
            this.style = style;
        }
    }

    public static final class NewsCompactGroup extends LauncherSearchHomeItem {
        @NonNull
        public final List<NewsArticle> articles;

        public NewsCompactGroup(@NonNull List<NewsArticle> articles) {
            this.articles = articles;
        }
    }

    public static final class GameFeature extends LauncherSearchHomeItem {
        @NonNull
        public final GameItemUi game;

        public GameFeature(@NonNull GameItemUi game) {
            this.game = game;
        }
    }

    public static final class GamePicks extends LauncherSearchHomeItem {
        @NonNull
        public final List<GameItemUi> games;

        public GamePicks(@NonNull List<GameItemUi> games) {
            this.games = games;
        }
    }

    public static final class WallpaperRow extends LauncherSearchHomeItem {
        @NonNull
        public final List<WallpaperItemUi> wallpapers;

        public WallpaperRow(@NonNull List<WallpaperItemUi> wallpapers) {
            this.wallpapers = wallpapers;
        }
    }
}
