package com.demo.app.launcher.ui.activity.news;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.databinding.ActivityNewsBinding;
import com.demo.app.launcher.ads.LauncherBackInsertPlaceholder;
import com.demo.app.launcher.ads.LauncherBackPressAds;
import com.demo.app.launcher.ads.LauncherBuiltInAds;
import com.demo.app.launcher.news.NewsArticle;
import com.demo.app.launcher.news.NewsRepository;
import com.demo.app.launcher.news.NewsStore;
import com.demo.app.launcher.news.NewsTag;
import com.demo.app.launcher.ui.adapter.news.NewsArticleAdapter;
import com.demo.app.launcher.ui.adapter.news.NewsTabAdapter;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

import java.util.ArrayList;
import java.util.List;

/** 新闻首页。顶部分类，下面是列表；加载时骨架闪动，失败时可以刷新。 */
public class NewsActivity extends BaseActivity<ActivityNewsBinding> {

    private final List<NewsTag> tags = new ArrayList<>();
    private final NewsArticleAdapter articles = new NewsArticleAdapter(this::openArticle);
    private final NewsTabAdapter tabs = new NewsTabAdapter(tags, this::onTab);

    private ObjectAnimator shimmer;
    private String areaName = NewsStore.area().name();
    private int selected;
    private int page = 1;
    private int total;
    private boolean loading;
    private int requestToken;

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, NewsActivity.class);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityNewsBinding getViewBinding() {
        return ActivityNewsBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.newsRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypes());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), true, true);
    }

    @Override
    protected void initViews() {
        binding.newsTabs.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.newsTabs.setAdapter(tabs);
        binding.newsList.setLayoutManager(new LinearLayoutManager(this));
        binding.newsList.setAdapter(articles);
        shimmer = ObjectAnimator.ofFloat(binding.newsSkeletonInclude.getRoot(), View.ALPHA, 0.35f, 1f);
        shimmer.setDuration(700L);
        shimmer.setRepeatMode(ValueAnimator.REVERSE);
        shimmer.setRepeatCount(ValueAnimator.INFINITE);
        LauncherBuiltInAds.show(binding.newsFeedAd.getRoot());
        if (binding.newsBottomBannerView != null) {
            binding.newsBottomBannerView.refreshVisibility();
        }
        LauncherBackInsertPlaceholder.preload(this);
        LauncherBackPressAds.install(this, this::finishAndRemoveTask);
    }

    @Override
    protected void initEvents() {
        binding.newsLocation.setOnClickListener(v -> NewsAreaActivity.start(this));
        binding.newsRefresh.setOnClickListener(v -> loadTags());
        binding.newsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0 || loading || tags.isEmpty()) {
                    return;
                }
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (manager == null) {
                    return;
                }
                int last = manager.findLastVisibleItemPosition();
                if (articles.size() - last <= 2 && articles.size() < total) {
                    loadArticles(page + 1, true);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String latest = NewsStore.area().name();
        if (!latest.equals(areaName)) {
            areaName = latest;
            loadTags();
        }
    }

    @Override
    protected void loadData() {
        loadTags();
    }

    private void loadTags() {
        showLoading();
        int token = ++requestToken;
        NewsRepository.tags(result -> {
            if (token != requestToken || isFinishing()) {
                return;
            }
            tags.clear();
            if (result != null) {
                tags.addAll(result);
            }
            selected = 0;
            tabs.resetSelection();
            tabs.notifyDataSetChanged();
            if (tags.isEmpty()) {
                showError();
                return;
            }
            loadArticles(1, false);
        });
    }

    private void loadArticles(int nextPage, boolean append) {
        if (tags.isEmpty()) {
            showError();
            return;
        }
        loading = true;
        if (!append) {
            showLoading();
            page = 1;
            total = 0;
        }
        int token = requestToken;
        NewsTag tag = tags.get(selected);
        NewsRepository.articles(tag.tag, nextPage, (result, count) -> {
            loading = false;
            if (token != requestToken || isFinishing()) {
                return;
            }
            if (result == null) {
                if (!append) {
                    showError();
                }
                return;
            }
            page = nextPage;
            total = count;
            if (append) {
                articles.addArticles(result);
            } else {
                articles.setArticles(result);
            }
            showContent();
        });
    }

    private void openArticle(@NonNull NewsArticle article) {
        if (article.isVideo()) {
            NewsVideoActivity.start(this, article);
        } else {
            NewsDetailActivity.start(this, article);
        }
    }

    private void showLoading() {
        binding.newsError.setVisibility(View.GONE);
        binding.newsList.setVisibility(View.GONE);
        binding.newsSkeletonInclude.getRoot().setVisibility(View.VISIBLE);
        binding.newsSkeletonInclude.getRoot().setAlpha(0.35f);
        shimmer.start();
    }

    private void showContent() {
        shimmer.cancel();
        binding.newsSkeletonInclude.getRoot().setVisibility(View.GONE);
        binding.newsError.setVisibility(View.GONE);
        binding.newsList.setVisibility(View.VISIBLE);
    }

    private void showError() {
        shimmer.cancel();
        binding.newsSkeletonInclude.getRoot().setVisibility(View.GONE);
        binding.newsList.setVisibility(View.GONE);
        binding.newsError.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (shimmer != null) {
            shimmer.cancel();
        }
        super.onDestroy();
    }

    private void onTab(int position) {
        selected = position;
        tabs.setSelected(position);
        loadArticles(1, false);
    }
}
