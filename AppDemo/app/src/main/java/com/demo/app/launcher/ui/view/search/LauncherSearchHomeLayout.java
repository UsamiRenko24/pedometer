package com.demo.app.launcher.ui.view.search;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.launcher.gamecenter.GameCenterSession;
import com.demo.app.launcher.gamecenter.model.GameCenterConfig;
import com.demo.app.launcher.gamecenter.model.GameCenterUiState;
import com.demo.app.launcher.gamecenter.model.GameItemUi;
import com.demo.app.launcher.gamecenter.model.GameSectionUi;
import com.demo.app.launcher.news.NewsArticle;
import com.demo.app.launcher.news.NewsRepository;
import com.demo.app.launcher.ui.activity.calendar.CalendarActivity;
import com.demo.app.launcher.ui.activity.discover.DiscoverDetailActivity;
import com.demo.app.launcher.ui.activity.news.NewsDetailActivity;
import com.demo.app.launcher.ui.activity.news.NewsVideoActivity;
import com.demo.app.launcher.ui.activity.wallpaper.WallpaperPreviewActivity;
import com.demo.app.launcher.ads.SplashFeedPlaceholder;
import com.demo.app.launcher.ui.activity.weather.WeatherLauncherActivity;
import com.demo.app.launcher.ui.activity.weather.WeatherVisuals;
import com.demo.app.launcher.ui.adapter.search.LauncherSearchHomeFlatAdapter;
import com.demo.app.launcher.utils.GameCenterDataUtils;
import com.demo.app.launcher.utils.WallpaperDataUtils;
import com.demo.app.launcher.wallpaper.model.WallpaperItemUi;
import com.demo.app.launcher.weather.WeatherRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/** 桌面搜索首页可滚动区：日历天气 + 新闻/游戏/壁纸穿插。 */
public final class LauncherSearchHomeLayout extends FrameLayout {

    private static final int NEWS_PAGE_SIZE = 12;
    private static final int WALLPAPER_ROW_SIZE = 3;
    private static final int GAME_PICK_COUNT = 4;

    private static final int SLOT_AD_AFTER_FIRST = 0;
    private static final int SLOT_WALLPAPER = 1;
    private static final int SLOT_GAME_FEATURE = 3;
    private static final int SLOT_GAME_PICKS = 4;

    private final LauncherSearchHomeFlatAdapter flatAdapter = new LauncherSearchHomeFlatAdapter();
    private RecyclerView feedList;
    private View top;
    private boolean newsLoading;
    private boolean newsLoaded;
    private boolean weatherRequested;
    @NonNull
    private List<NewsArticle> newsArticles = new ArrayList<>();
    @NonNull
    private String newsSectionTitle = "";
    @NonNull
    private List<GameItemUi> gamePool = new ArrayList<>();
    @NonNull
    private List<WallpaperItemUi> wallpaperPool = new ArrayList<>();
    private long feedGeneration;

    public LauncherSearchHomeLayout(@NonNull Context context) {
        this(context, null);
    }

    public LauncherSearchHomeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.layout_launcher_search_home, this);
        feedList = findViewById(R.id.launcher_search_feed);
        feedList.setLayoutManager(new LinearLayoutManager(context));
        feedList.setAdapter(flatAdapter);
        flatAdapter.setCalendarBinder(this::bindCalendarWeatherRow);
        flatAdapter.setListener(new LauncherSearchHomeFlatAdapter.Listener() {
            @Override
            public void onNews(@NonNull NewsArticle article) {
                openArticle(article);
            }

            @Override
            public void onNewsRefresh() {
                loadNewsList();
            }

            @Override
            public void onGame(@NonNull GameItemUi game) {
                if (game.getBid().isEmpty()) {
                    return;
                }
                CharSequence title = game.getDisplayTitle().isEmpty()
                        ? game.getName()
                        : game.getDisplayTitle();
                SplashFeedPlaceholder.runFromContext(
                        getContext(),
                        title,
                        () -> DiscoverDetailActivity.start(getContext(), game.getBid()));
            }

            @Override
            public void onWallpaper(@NonNull WallpaperItemUi wallpaper) {
                if (wallpaper.getId().isEmpty()) {
                    return;
                }
                SplashFeedPlaceholder.runFromContext(
                        getContext(),
                        () -> WallpaperPreviewActivity.start(
                                getContext(), wallpaper.getId(), wallpaper.getThumbUrl()));
            }
        });
        top = findViewById(R.id.launcher_search_home_top);
        top.setOnClickListener(v -> feedList.smoothScrollToPosition(0));
        feedList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int first = manager == null ? 0 : manager.findFirstVisibleItemPosition();
                top.setVisibility(first > 1 ? VISIBLE : GONE);
            }
        });
        flatAdapter.submit(buildFlatItems());
    }

    private void bindCalendarWeatherRow(@NonNull View root) {
        Calendar now = Calendar.getInstance();
        Locale locale = Locale.getDefault();
        TextView day = root.findViewById(R.id.launcher_search_calendar_day);
        TextView month = root.findViewById(R.id.launcher_search_calendar_month);
        TextView week = root.findViewById(R.id.launcher_search_calendar_week);
        TextView year = root.findViewById(R.id.launcher_search_calendar_year);
        String weekName = now.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale);
        day.setText(String.valueOf(now.get(Calendar.DAY_OF_MONTH)));
        month.setText(now.getDisplayName(Calendar.MONTH, Calendar.LONG, locale));
        week.setText(weekName == null ? "" : weekName.toUpperCase(locale));
        year.setText(String.valueOf(now.get(Calendar.YEAR)));
        bindWeekStrip(root, now, locale);
        root.findViewById(R.id.launcher_search_calendar).setOnClickListener(v -> CalendarActivity.start(getContext()));
        root.findViewById(R.id.launcher_search_weather).setOnClickListener(v -> {
            android.content.Context ctx = getContext();
            if (ctx instanceof android.app.Activity) {
                SplashFeedPlaceholder.run(
                        (android.app.Activity) ctx,
                        ctx.getString(R.string.screen0_weather),
                        () -> WeatherLauncherActivity.start(ctx));
            } else {
                WeatherLauncherActivity.start(ctx);
            }
        });
        if (weatherRequested) {
            return;
        }
        weatherRequested = true;
        WeatherRepository.load(getContext(), (location, snapshot) -> {
            TextView temp = root.findViewById(R.id.launcher_search_weather_temp);
            TextView condition = root.findViewById(R.id.launcher_search_weather_condition);
            ImageView icon = root.findViewById(R.id.launcher_search_weather_icon);
            if (temp == null || condition == null || icon == null) {
                return;
            }
            if (snapshot == null) {
                temp.setText("--");
                condition.setText("");
                return;
            }
            temp.setText(snapshot.temperature + "°");
            condition.setText(WeatherVisuals.label(WeatherVisuals.kindOf(snapshot.skycon)));
            icon.setImageResource(WeatherVisuals.ofSkycon(snapshot.skycon).icon);
        });
    }

    private void bindWeekStrip(@NonNull View root, @NonNull Calendar now, @NonNull Locale locale) {
        LinearLayout strip = root.findViewById(R.id.launcher_search_calendar_week_strip);
        if (strip == null) {
            return;
        }
        strip.removeAllViews();
        Calendar cursor = (Calendar) now.clone();
        cursor.setFirstDayOfWeek(now.getFirstDayOfWeek());
        cursor.set(Calendar.DAY_OF_WEEK, now.getFirstDayOfWeek());
        SimpleDateFormat narrow = new SimpleDateFormat("EEEEE", locale);
        float density = getResources().getDisplayMetrics().density;
        int cellSize = (int) (22 * density);
        int gap = (int) (3 * density);
        int todayYear = now.get(Calendar.YEAR);
        int todayMonth = now.get(Calendar.MONTH);
        int todayDay = now.get(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < 7; i++) {
            boolean isToday = cursor.get(Calendar.YEAR) == todayYear
                    && cursor.get(Calendar.MONTH) == todayMonth
                    && cursor.get(Calendar.DAY_OF_MONTH) == todayDay;
            TextView label = new TextView(getContext());
            label.setText(narrow.format(cursor.getTime()));
            label.setGravity(android.view.Gravity.CENTER);
            label.setIncludeFontPadding(false);
            label.setTextSize(10);
            label.setTextColor(getContext().getColor(
                    isToday ? R.color.color_white : R.color.color_E6FFFFFF));
            if (isToday) {
                label.setBackgroundResource(R.drawable.bg_launcher_search_weekday_on);
                label.setTypeface(label.getTypeface(), android.graphics.Typeface.BOLD);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cellSize, cellSize);
            if (i > 0) {
                params.setMarginStart(gap);
            }
            strip.addView(label, params);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    public void loadNewsList() {
        if (newsLoading) {
            return;
        }
        newsLoading = true;
        newsLoaded = false;
        weatherRequested = false;
        long generation = ++feedGeneration;
        gamePool = new ArrayList<>();
        wallpaperPool = new ArrayList<>();
        flatAdapter.submit(buildFlatItems());
        startGameLoad(generation);
        startWallpaperLoad(generation);
        NewsRepository.articles("top", 1, (articles, total) -> {
            if (generation != feedGeneration) {
                return;
            }
            if (articles == null || articles.isEmpty()) {
                finishNewsLoad(generation, new ArrayList<>());
                return;
            }
            List<NewsArticle> merged = new ArrayList<>(articles);
            if (merged.size() >= NEWS_PAGE_SIZE) {
                finishNewsLoad(generation, merged.subList(0, NEWS_PAGE_SIZE));
                return;
            }
            NewsRepository.articles("top", 2, (more, total2) -> {
                if (generation != feedGeneration) {
                    return;
                }
                if (more != null) {
                    merged.addAll(more);
                }
                finishNewsLoad(generation,
                        merged.subList(0, Math.min(NEWS_PAGE_SIZE, merged.size())));
            });
        });
    }

    private void finishNewsLoad(long generation, @NonNull List<NewsArticle> articles) {
        if (generation != feedGeneration) {
            return;
        }
        newsLoading = false;
        if (articles.isEmpty()) {
            newsArticles = new ArrayList<>();
            newsLoaded = false;
        } else {
            newsArticles = new ArrayList<>(articles);
            newsSectionTitle = getContext().getString(R.string.search_home_news_section_title);
            newsLoaded = true;
        }
        flatAdapter.submit(buildFlatItems());
    }

    private void startGameLoad(long generation) {
        GameCenterDataUtils.INSTANCE.loadHome(
                new GameCenterConfig("com.rwapp.fm.tvremote", 2),
                (state, error) -> {
                    if (generation != feedGeneration) {
                        return kotlin.Unit.INSTANCE;
                    }
                    applyGames(state);
                    flatAdapter.submit(buildFlatItems());
                    return kotlin.Unit.INSTANCE;
                });
    }

    private void startWallpaperLoad(long generation) {
        WallpaperDataUtils.INSTANCE.loadCategories((categories, catError) -> {
            String categoryId = categories.isEmpty() ? "" : categories.get(0).getId();
            if (categoryId.isEmpty()) {
                return kotlin.Unit.INSTANCE;
            }
            WallpaperDataUtils.INSTANCE.loadWallpapers(categoryId, 1, 9, true,
                    (listUi, err) -> {
                        if (generation != feedGeneration) {
                            return kotlin.Unit.INSTANCE;
                        }
                        if (listUi.getItems().isEmpty()) {
                            return kotlin.Unit.INSTANCE;
                        }
                        if (!listUi.getAssetBaseUrl().isEmpty()) {
                            com.demo.app.launcher.wallpaper.WallpaperSession.INSTANCE
                                    .updateAssetBaseUrl(listUi.getAssetBaseUrl());
                        }
                        List<WallpaperItemUi> picked = new ArrayList<>();
                        for (WallpaperItemUi item : listUi.getItems()) {
                            if (!item.getId().isEmpty()) {
                                picked.add(item);
                            }
                            if (picked.size() >= WALLPAPER_ROW_SIZE) {
                                break;
                            }
                        }
                        wallpaperPool = picked;
                        flatAdapter.submit(buildFlatItems());
                        return kotlin.Unit.INSTANCE;
                    });
            return kotlin.Unit.INSTANCE;
        });
    }

    private void applyGames(@Nullable GameCenterUiState state) {
        if (state == null) {
            gamePool = new ArrayList<>();
            return;
        }
        if (!state.getAssetBaseUrl().isEmpty()) {
            GameCenterSession.INSTANCE.updateAssetBaseUrl(state.getAssetBaseUrl());
        }
        List<GameItemUi> pool = new ArrayList<>();
        for (GameSectionUi section : state.getSections()) {
            for (GameItemUi item : section.getItems()) {
                if (!item.getBid().isEmpty()) {
                    pool.add(item);
                }
            }
        }
        Collections.shuffle(pool);
        gamePool = pool.size() > 8 ? new ArrayList<>(pool.subList(0, 8)) : pool;
    }

    @NonNull
    private List<LauncherSearchHomeItem> buildFlatItems() {
        List<LauncherSearchHomeItem> items = new ArrayList<>();
        items.add(new LauncherSearchHomeItem.CalendarHeader());
        if (newsLoading && newsArticles.isEmpty()) {
            items.add(new LauncherSearchHomeItem.NewsLoading());
            return items;
        }
        if (!newsLoaded && newsArticles.isEmpty()) {
            items.add(new LauncherSearchHomeItem.NewsError());
            return items;
        }
        items.add(new LauncherSearchHomeItem.Ad());
        items.add(new LauncherSearchHomeItem.GroupTitle(
                newsSectionTitle.isEmpty()
                        ? getContext().getString(R.string.search_home_news_section_title)
                        : newsSectionTitle,
                getContext().getString(R.string.search_home_news_section_subtitle)));
        Iterator<GameItemUi> gameIterator = gamePool.iterator();
        int slot = 0;
        int index = 0;
        while (index < newsArticles.size()) {
            int phase = slot % 5;
            if (phase == 0) {
                items.add(new LauncherSearchHomeItem.News(
                        newsArticles.get(index), LauncherSearchHomeItem.NEWS_STYLE_HERO));
                index++;
            } else if (phase == 1) {
                items.add(new LauncherSearchHomeItem.News(
                        newsArticles.get(index), LauncherSearchHomeItem.NEWS_STYLE_COMPACT));
                index++;
            } else if (phase == 2 && index + 1 < newsArticles.size()) {
                items.add(new LauncherSearchHomeItem.NewsCompactGroup(
                        Arrays.asList(newsArticles.get(index), newsArticles.get(index + 1))));
                index += 2;
            } else {
                items.add(new LauncherSearchHomeItem.News(
                        newsArticles.get(index), LauncherSearchHomeItem.NEWS_STYLE_COMPACT));
                index++;
            }
            appendSlotExtras(items, slot, gameIterator);
            slot++;
        }
        return items;
    }

    private void appendSlotExtras(
            @NonNull List<LauncherSearchHomeItem> items,
            int slot,
            @NonNull Iterator<GameItemUi> gameIterator) {
        switch (slot) {
            case SLOT_AD_AFTER_FIRST:
                items.add(new LauncherSearchHomeItem.Ad());
                break;
            case SLOT_WALLPAPER:
                if (wallpaperPool.size() >= WALLPAPER_ROW_SIZE) {
                    items.add(new LauncherSearchHomeItem.WallpaperRow(
                            new ArrayList<>(wallpaperPool.subList(0, WALLPAPER_ROW_SIZE))));
                }
                items.add(new LauncherSearchHomeItem.Ad());
                break;
            case SLOT_GAME_FEATURE:
                takeGame(gameIterator).ifPresent(game ->
                        items.add(new LauncherSearchHomeItem.GameFeature(game)));
                items.add(new LauncherSearchHomeItem.Ad());
                break;
            case SLOT_GAME_PICKS:
                List<GameItemUi> picks = takeGames(gameIterator, GAME_PICK_COUNT);
                if (!picks.isEmpty()) {
                    items.add(new LauncherSearchHomeItem.GamePicks(picks));
                }
                break;
            default:
                break;
        }
    }

    @NonNull
    private java.util.Optional<GameItemUi> takeGame(@NonNull Iterator<GameItemUi> iterator) {
        if (!iterator.hasNext()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(iterator.next());
    }

    @NonNull
    private List<GameItemUi> takeGames(@NonNull Iterator<GameItemUi> iterator, int count) {
        List<GameItemUi> result = new ArrayList<>();
        while (result.size() < count && iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    private void openArticle(@NonNull NewsArticle article) {
        if (article.isVideo()) {
            NewsVideoActivity.start(getContext(), article);
        } else {
            NewsDetailActivity.start(getContext(), article);
        }
    }
}
