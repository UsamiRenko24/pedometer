package com.demo.app.launcher.ui.adapter.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.app.R;
import com.demo.app.databinding.ItemNewsArticleBinding;
import com.demo.app.launcher.gamecenter.model.GameItemUi;
import com.demo.app.launcher.news.NewsArticle;
import com.demo.app.launcher.ui.view.search.LauncherSearchHomeItem;
import com.demo.app.launcher.wallpaper.model.WallpaperItemUi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 搜索首页：新闻 / 游戏 / 壁纸 / 广告占位穿插列表。 */
public final class LauncherSearchHomeFlatAdapter
        extends RecyclerView.Adapter<LauncherSearchHomeFlatAdapter.Holder> {

    public interface Listener {
        void onNews(@NonNull NewsArticle article);

        void onNewsRefresh();

        void onGame(@NonNull GameItemUi game);

        void onWallpaper(@NonNull WallpaperItemUi wallpaper);
    }

    public interface CalendarBinder {
        void bind(@NonNull View calendarWeatherRoot);
    }

    private static final int TYPE_CALENDAR = 1;
    private static final int TYPE_AD = 2;
    private static final int TYPE_GROUP = 3;
    private static final int TYPE_NEWS_HERO = 4;
    private static final int TYPE_NEWS_COMPACT = 5;
    private static final int TYPE_NEWS_COMPACT_GROUP = 6;
    private static final int TYPE_GAME_FEATURE = 7;
    private static final int TYPE_GAME_PICKS = 8;
    private static final int TYPE_WALLPAPER = 9;
    private static final int TYPE_NEWS_LOADING = 10;
    private static final int TYPE_NEWS_ERROR = 11;

    private final List<LauncherSearchHomeItem> items = new ArrayList<>();
    @Nullable
    private Listener listener;
    @Nullable
    private CalendarBinder calendarBinder;

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    public void setCalendarBinder(@Nullable CalendarBinder calendarBinder) {
        this.calendarBinder = calendarBinder;
    }

    public void submit(@NonNull List<LauncherSearchHomeItem> next) {
        items.clear();
        items.addAll(next);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        LauncherSearchHomeItem item = items.get(position);
        if (item instanceof LauncherSearchHomeItem.CalendarHeader) {
            return TYPE_CALENDAR;
        }
        if (item instanceof LauncherSearchHomeItem.Ad) {
            return TYPE_AD;
        }
        if (item instanceof LauncherSearchHomeItem.GroupTitle) {
            return TYPE_GROUP;
        }
        if (item instanceof LauncherSearchHomeItem.News) {
            return ((LauncherSearchHomeItem.News) item).style
                    == LauncherSearchHomeItem.NEWS_STYLE_COMPACT
                    ? TYPE_NEWS_COMPACT : TYPE_NEWS_HERO;
        }
        if (item instanceof LauncherSearchHomeItem.NewsCompactGroup) {
            return TYPE_NEWS_COMPACT_GROUP;
        }
        if (item instanceof LauncherSearchHomeItem.GameFeature) {
            return TYPE_GAME_FEATURE;
        }
        if (item instanceof LauncherSearchHomeItem.GamePicks) {
            return TYPE_GAME_PICKS;
        }
        if (item instanceof LauncherSearchHomeItem.WallpaperRow) {
            return TYPE_WALLPAPER;
        }
        if (item instanceof LauncherSearchHomeItem.NewsLoading) {
            return TYPE_NEWS_LOADING;
        }
        return TYPE_NEWS_ERROR;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case TYPE_CALENDAR:
                view = inflater.inflate(R.layout.item_launcher_search_calendar_weather_row, parent, false);
                break;
            case TYPE_AD:
                view = inflater.inflate(R.layout.item_launcher_search_ad, parent, false);
                break;
            case TYPE_NEWS_COMPACT:
                view = inflater.inflate(R.layout.item_launcher_search_news_compact, parent, false);
                break;
            case TYPE_NEWS_COMPACT_GROUP:
                view = inflater.inflate(R.layout.item_launcher_search_news_compact_group, parent, false);
                break;
            case TYPE_GROUP:
                view = inflater.inflate(R.layout.item_launcher_search_home_group_title, parent, false);
                break;
            case TYPE_GAME_FEATURE:
                view = inflater.inflate(R.layout.item_launcher_search_game_feature, parent, false);
                break;
            case TYPE_GAME_PICKS:
                view = inflater.inflate(R.layout.item_launcher_search_game_picks, parent, false);
                break;
            case TYPE_WALLPAPER:
                view = inflater.inflate(R.layout.item_launcher_search_wallpaper_row, parent, false);
                break;
            case TYPE_NEWS_LOADING:
            case TYPE_NEWS_ERROR:
                view = inflater.inflate(R.layout.item_launcher_search_news_state, parent, false);
                break;
            default:
                view = ItemNewsArticleBinding.inflate(inflater, parent, false).getRoot();
                break;
        }
        return new Holder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        LauncherSearchHomeItem item = items.get(position);
        switch (holder.viewType) {
            case TYPE_CALENDAR:
                if (calendarBinder != null) {
                    calendarBinder.bind(holder.itemView);
                }
                break;
            case TYPE_GROUP:
                bindGroup(holder.itemView, (LauncherSearchHomeItem.GroupTitle) item);
                break;
            case TYPE_NEWS_HERO:
                bindNewsHero(holder.itemView, ((LauncherSearchHomeItem.News) item).article);
                break;
            case TYPE_NEWS_COMPACT:
                bindNewsCompact(holder.itemView, ((LauncherSearchHomeItem.News) item).article);
                break;
            case TYPE_NEWS_COMPACT_GROUP:
                bindNewsCompactGroup(holder.itemView, ((LauncherSearchHomeItem.NewsCompactGroup) item).articles);
                break;
            case TYPE_GAME_FEATURE:
                bindGameFeature(holder.itemView, ((LauncherSearchHomeItem.GameFeature) item).game);
                break;
            case TYPE_GAME_PICKS:
                bindGamePicks(holder.itemView, ((LauncherSearchHomeItem.GamePicks) item).games);
                break;
            case TYPE_WALLPAPER:
                bindWallpapers(holder.itemView, ((LauncherSearchHomeItem.WallpaperRow) item).wallpapers);
                break;
            case TYPE_NEWS_LOADING:
                bindNewsLoading(holder.itemView);
                break;
            case TYPE_NEWS_ERROR:
                bindNewsError(holder.itemView);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void bindGroup(@NonNull View view, @NonNull LauncherSearchHomeItem.GroupTitle item) {
        TextView title = view.findViewById(R.id.search_home_group_title);
        TextView subtitle = view.findViewById(R.id.search_home_group_subtitle);
        title.setText(item.title);
        if (item.subtitle == null || item.subtitle.isEmpty()) {
            subtitle.setVisibility(View.GONE);
        } else {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(item.subtitle);
        }
    }

    private void bindNewsHero(@NonNull View view, @NonNull NewsArticle article) {
        ItemNewsArticleBinding binding = ItemNewsArticleBinding.bind(view);
        binding.newsArticleTitle.setText(article.title);
        String source = article.source_name == null ? "" : article.source_name;
        String time = article.pub_date == null ? "" : article.pub_date;
        String meta = source.isEmpty() ? time : time.isEmpty() ? source : source + " · " + time;
        binding.newsArticleMeta.setText(meta);
        Glide.with(binding.newsArticleImage)
                .load(article.imageUrl())
                .placeholder(R.drawable.icon_home_app_news_detail_img)
                .error(R.drawable.icon_home_app_news_detail_img)
                .centerCrop()
                .into(binding.newsArticleImage);
        view.setOnClickListener(v -> openNews(article));
    }

    private void bindNewsCompact(@NonNull View view, @NonNull NewsArticle article) {
        TextView title = view.findViewById(R.id.search_news_compact_title);
        ImageView image = view.findViewById(R.id.search_news_compact_image);
        TextView source = view.findViewById(R.id.search_news_compact_source);
        title.setText(article.title);
        source.setText(sourceLabel(view.getContext(), article));
        Glide.with(image)
                .load(article.imageUrl())
                .placeholder(R.drawable.icon_home_app_news_detail_img)
                .error(R.drawable.icon_home_app_news_detail_img)
                .centerCrop()
                .into(image);
        view.setOnClickListener(v -> openNews(article));
    }

    private void bindNewsCompactGroup(@NonNull View view, @NonNull List<NewsArticle> articles) {
        bindGroupRow(view.findViewById(R.id.search_news_group_row0),
                articles.size() > 0 ? articles.get(0) : null);
        bindGroupRow(view.findViewById(R.id.search_news_group_row1),
                articles.size() > 1 ? articles.get(1) : null);
    }

    private void bindGroupRow(@Nullable View row, @Nullable NewsArticle article) {
        if (row == null || article == null) {
            if (row != null) {
                row.setVisibility(View.GONE);
            }
            return;
        }
        row.setVisibility(View.VISIBLE);
        TextView title = row.findViewById(R.id.search_news_group_row_title);
        TextView meta = row.findViewById(R.id.search_news_group_row_meta);
        ImageView image = row.findViewById(R.id.search_news_group_row_image);
        title.setText(article.title);
        meta.setText(groupMeta(row.getContext(), article));
        Glide.with(image)
                .load(article.imageUrl())
                .placeholder(R.drawable.icon_home_app_news_detail_img)
                .error(R.drawable.icon_home_app_news_detail_img)
                .centerCrop()
                .into(image);
        row.setOnClickListener(v -> openNews(article));
    }

    @NonNull
    private String sourceLabel(@NonNull android.content.Context context, @NonNull NewsArticle article) {
        String source = article.source_name == null ? "" : article.source_name.trim();
        return source.isEmpty() ? context.getString(R.string.screen0_news) : source;
    }

    @NonNull
    private String groupMeta(@NonNull android.content.Context context, @NonNull NewsArticle article) {
        String tag = article.tag == null ? "" : article.tag.trim();
        if (tag.isEmpty()) {
            tag = context.getString(R.string.screen0_news);
        }
        return tag.toUpperCase(Locale.ROOT) + " · "
                + context.getString(R.string.search_news_quick_read);
    }

    private void openNews(@NonNull NewsArticle article) {
        if (listener != null) {
            listener.onNews(article);
        }
    }

    private void bindGameFeature(@NonNull View view, @NonNull GameItemUi game) {
        ImageView banner = view.findViewById(R.id.search_game_feature_banner);
        View action = view.findViewById(R.id.search_game_feature_action);
        String image = game.getBannerUrl().isEmpty() ? game.getIconUrl() : game.getBannerUrl();
        Glide.with(banner).load(image).centerCrop().into(banner);
        action.setOnClickListener(v -> openGame(game));
        view.setOnClickListener(v -> openGame(game));
    }

    private void bindGamePicks(@NonNull View view, @NonNull List<GameItemUi> games) {
        RecyclerView list = view.findViewById(R.id.search_game_picks_list);
        list.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
        list.setAdapter(new GamePickAdapter(games));
    }

    private void bindWallpapers(@NonNull View view, @NonNull List<WallpaperItemUi> wallpapers) {
        RecyclerView list = view.findViewById(R.id.search_wallpaper_row_list);
        list.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        list.setAdapter(new WallpaperAdapter(wallpapers));
    }

    private void bindNewsLoading(@NonNull View view) {
        view.findViewById(R.id.search_news_state_progress).setVisibility(View.VISIBLE);
        TextView message = view.findViewById(R.id.search_news_state_message);
        message.setText(R.string.news_loading);
        view.findViewById(R.id.search_news_state_action).setVisibility(View.GONE);
    }

    private void bindNewsError(@NonNull View view) {
        view.findViewById(R.id.search_news_state_progress).setVisibility(View.GONE);
        TextView message = view.findViewById(R.id.search_news_state_message);
        message.setText(R.string.news_loading_failed);
        TextView action = view.findViewById(R.id.search_news_state_action);
        action.setVisibility(View.VISIBLE);
        action.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewsRefresh();
            }
        });
    }

    private void openGame(@NonNull GameItemUi game) {
        if (listener != null) {
            listener.onGame(game);
        }
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final int viewType;

        Holder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
        }
    }

    private final class GamePickAdapter extends RecyclerView.Adapter<GamePickAdapter.Cell> {
        private final List<GameItemUi> games;

        GamePickAdapter(@NonNull List<GameItemUi> games) {
            this.games = games;
        }

        @NonNull
        @Override
        public Cell onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_launcher_search_game_pick_cell, parent, false);
            return new Cell(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Cell holder, int position) {
            GameItemUi game = games.get(position);
            holder.name.setText(game.getDisplayTitle().isEmpty() ? game.getName() : game.getDisplayTitle());
            Glide.with(holder.icon).load(game.getIconUrl()).into(holder.icon);
            holder.play.setOnClickListener(v -> openGame(game));
            holder.itemView.setOnClickListener(v -> openGame(game));
        }

        @Override
        public int getItemCount() {
            return games.size();
        }

        final class Cell extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView name;
            final TextView play;

            Cell(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.search_game_pick_icon);
                name = itemView.findViewById(R.id.search_game_pick_name);
                play = itemView.findViewById(R.id.search_game_pick_play);
            }
        }
    }

    private final class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.Cell> {
        private final List<WallpaperItemUi> wallpapers;

        WallpaperAdapter(@NonNull List<WallpaperItemUi> wallpapers) {
            this.wallpapers = wallpapers;
        }

        @NonNull
        @Override
        public Cell onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_launcher_search_wallpaper_cell, parent, false);
            return new Cell(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Cell holder, int position) {
            WallpaperItemUi item = wallpapers.get(position);
            Glide.with(holder.image).load(item.getThumbUrl()).centerCrop().into(holder.image);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWallpaper(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return wallpapers.size();
        }

        final class Cell extends RecyclerView.ViewHolder {
            final ImageView image;

            Cell(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.search_wallpaper_cell_image);
            }
        }
    }
}
