package com.demo.app.launcher.ui.adapter.news;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.app.R;
import com.demo.app.databinding.ItemNewsArticleBinding;
import com.demo.app.launcher.news.NewsArticle;

import java.util.ArrayList;
import java.util.List;

public final class NewsArticleAdapter extends RecyclerView.Adapter<NewsArticleAdapter.Holder> {

    public interface ClickListener {
        void onArticle(@NonNull NewsArticle article);
    }

    private final List<NewsArticle> articles = new ArrayList<>();
    private final ClickListener listener;

    public NewsArticleAdapter(@NonNull ClickListener listener) {
        this.listener = listener;
    }

    public void setArticles(@NonNull List<NewsArticle> next) {
        articles.clear();
        articles.addAll(next);
        notifyDataSetChanged();
    }

    public void addArticles(@NonNull List<NewsArticle> more) {
        int start = articles.size();
        articles.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    public int size() {
        return articles.size();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemNewsArticleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NewsArticle article = articles.get(position);
        holder.binding.newsArticleTitle.setText(article.title);
        String source = article.source_name == null ? "" : article.source_name;
        String time = article.pub_date == null ? "" : article.pub_date;
        String meta = source.isEmpty() ? time : time.isEmpty() ? source : source + " · " + time;
        holder.binding.newsArticleMeta.setText(meta);
        Glide.with(holder.binding.newsArticleImage)
                .load(article.imageUrl())
                .placeholder(R.drawable.icon_home_app_news_detail_img)
                .error(R.drawable.icon_home_app_news_detail_img)
                .centerCrop()
                .into(holder.binding.newsArticleImage);
        holder.binding.getRoot().setOnClickListener(v -> listener.onArticle(article));
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemNewsArticleBinding binding;

        Holder(ItemNewsArticleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
