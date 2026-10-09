package com.demo.app.launcher.ui.view.search;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.launcher.news.NewsArticle;
import com.demo.app.launcher.news.NewsRepository;
import com.demo.app.launcher.news.NewsTag;
import com.demo.app.launcher.ui.activity.news.NewsDetailActivity;
import com.demo.app.launcher.ui.activity.news.NewsVideoActivity;
import com.demo.app.launcher.ui.adapter.news.NewsArticleAdapter;

import java.util.ArrayList;
import java.util.List;

/** 桌面搜索返回页：新闻分类和对应列表。 */
public final class LauncherSearchBackLayout extends FrameLayout {

    private final List<NewsTag> tags = new ArrayList<>();
    private final TagAdapter tagAdapter = new TagAdapter();
    private final NewsArticleAdapter newsAdapter = new NewsArticleAdapter(this::openArticle);
    private ProgressBar loading;
    private View empty;
    private int selected;
    private boolean loaded;

    public LauncherSearchBackLayout(@NonNull Context context) {
        this(context, null);
    }

    public LauncherSearchBackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_launcher_search_back, this, true);
        RecyclerView tagList = findViewById(R.id.launcher_search_back_tags);
        tagList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        tagList.setAdapter(tagAdapter);
        RecyclerView news = findViewById(R.id.launcher_search_back_news);
        news.setLayoutManager(new LinearLayoutManager(context));
        news.setAdapter(newsAdapter);
        loading = findViewById(R.id.launcher_search_back_loading);
        empty = findViewById(R.id.launcher_search_back_empty);
        findViewById(R.id.launcher_search_back_refresh).setOnClickListener(v -> load(true));
    }

    public void loadNewsDataIfNeeded() {
        load(false);
    }

    private void load(boolean force) {
        if (loaded && !force) {
            return;
        }
        loading.setVisibility(VISIBLE);
        empty.setVisibility(GONE);
        NewsRepository.tags(result -> {
            tags.clear();
            if (result != null) {
                tags.addAll(result);
            }
            tagAdapter.notifyDataSetChanged();
            if (tags.isEmpty()) {
                loaded = false;
                loading.setVisibility(GONE);
                empty.setVisibility(VISIBLE);
                newsAdapter.setArticles(new ArrayList<>());
                return;
            }
            if (selected >= tags.size()) {
                selected = 0;
            }
            loadArticles(tags.get(selected).tag);
        });
    }

    private void loadArticles(@NonNull String tag) {
        loading.setVisibility(VISIBLE);
        empty.setVisibility(GONE);
        NewsRepository.articles(tag, 1, (articles, total) -> {
            loading.setVisibility(GONE);
            if (articles == null) {
                loaded = false;
                newsAdapter.setArticles(new ArrayList<>());
                empty.setVisibility(VISIBLE);
                return;
            }
            loaded = true;
            empty.setVisibility(articles.isEmpty() ? VISIBLE : GONE);
            newsAdapter.setArticles(articles);
        });
    }

    private void openArticle(@NonNull NewsArticle article) {
        if (article.isVideo()) {
            NewsVideoActivity.start(getContext(), article);
        } else {
            NewsDetailActivity.start(getContext(), article);
        }
    }

    private final class TagAdapter extends RecyclerView.Adapter<TagAdapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_tab, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            NewsTag tag = tags.get(position);
            boolean on = position == selected;
            holder.label.setText(tag.name);
            holder.label.setBackgroundResource(on
                    ? R.drawable.bg_news_tab_selected
                    : R.drawable.bg_news_tab_normal);
            holder.label.setTextColor(on ? Color.WHITE : 0xFF8A8F99);
            holder.itemView.setOnClickListener(v -> {
                if (selected == position) {
                    return;
                }
                selected = position;
                notifyDataSetChanged();
                loadArticles(tag.tag);
            });
        }

        @Override
        public int getItemCount() {
            return tags.size();
        }

        final class Holder extends RecyclerView.ViewHolder {
            final TextView label;

            Holder(@NonNull View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.news_tab_label);
            }
        }
    }
}
