package com.demo.app.launcher.ui.adapter.news;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.databinding.ItemNewsAreaBinding;
import com.demo.app.launcher.news.NewsArea;

/** 新闻地区列表。 */
public final class NewsAreaAdapter extends RecyclerView.Adapter<NewsAreaAdapter.Holder> {

    public interface Listener {
        void onArea(@NonNull NewsArea area);
    }

    private final NewsArea[] areas = NewsArea.values();
    private final Listener listener;
    private NewsArea selected;

    public NewsAreaAdapter(@NonNull NewsArea selected, @NonNull Listener listener) {
        this.selected = selected;
        this.listener = listener;
    }

    public void setSelected(@NonNull NewsArea area) {
        selected = area;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemNewsAreaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NewsArea area = areas[position];
        boolean on = area == selected;
        holder.binding.newsAreaLabel.setText(area.labelRes);
        holder.binding.newsAreaLabel.setTextColor(on ? 0xFFFF8201 : 0xFF1C2430);
        holder.binding.getRoot().setBackgroundResource(on
                ? R.drawable.bg_news_area_selected
                : R.drawable.bg_news_area_normal);
        holder.binding.newsAreaCheck.setVisibility(on ? View.VISIBLE : View.GONE);
        holder.binding.getRoot().setOnClickListener(v -> listener.onArea(area));
    }

    @Override
    public int getItemCount() {
        return areas.length;
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemNewsAreaBinding binding;

        Holder(ItemNewsAreaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
