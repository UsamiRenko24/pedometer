package com.demo.app.launcher.ui.adapter.news;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.databinding.ItemNewsTabBinding;
import com.demo.app.launcher.news.NewsTag;

import java.util.List;

/** 新闻顶部分类。 */
public final class NewsTabAdapter extends RecyclerView.Adapter<NewsTabAdapter.Holder> {

    public interface Listener {
        void onTab(int position);
    }

    private final List<NewsTag> tags;
    private final Listener listener;
    private int selected;

    public NewsTabAdapter(@NonNull List<NewsTag> tags, @NonNull Listener listener) {
        this.tags = tags;
        this.listener = listener;
    }

    public void resetSelection() {
        selected = 0;
    }

    public void setSelected(int position) {
        if (position == selected || position < 0 || position >= tags.size()) {
            return;
        }
        int previous = selected;
        selected = position;
        notifyItemChanged(previous);
        notifyItemChanged(selected);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemNewsTabBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NewsTag tag = tags.get(position);
        String label = tag.name == null || tag.name.isEmpty() ? tag.tag : tag.name;
        holder.binding.newsTabLabel.setText(label);
        boolean on = position == selected;
        holder.binding.newsTabLabel.setBackgroundResource(on
                ? R.drawable.bg_news_tab_selected
                : R.drawable.bg_news_tab_normal);
        holder.binding.newsTabLabel.setTextColor(on ? Color.WHITE : 0xFF8A8F99);
        holder.binding.newsTabLabel.setOnClickListener(v -> {
            if (selected != position) {
                listener.onTab(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemNewsTabBinding binding;

        Holder(ItemNewsTabBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
