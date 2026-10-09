package com.demo.app.launcher.ui.adapter.wallpaper;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.databinding.ItemWallpaperCategoryBinding;
import com.demo.app.launcher.wallpaper.WallpaperCategories;
import com.demo.app.launcher.wallpaper.model.CategoryUi;

import java.util.List;

/** 壁纸左侧分类。 */
public final class WallpaperCategoryAdapter extends RecyclerView.Adapter<WallpaperCategoryAdapter.Holder> {

    public interface Listener {
        void onCategory(int position);
    }

    private final List<CategoryUi> categories;
    private final Listener listener;
    private int selected;

    public WallpaperCategoryAdapter(@NonNull List<CategoryUi> categories, @NonNull Listener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void setSelected(int position) {
        selected = position;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemWallpaperCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CategoryUi category = categories.get(position);
        boolean chosen = position == selected;
        holder.binding.wallpaperCategoryIcon.setImageResource(WallpaperCategories.icon(category.getId()));
        holder.binding.wallpaperCategoryName.setText(WallpaperCategories.label(category.getId()));
        holder.binding.wallpaperCategoryName.setTextColor(chosen ? Color.WHITE : Color.parseColor("#1C1C1E"));
        holder.binding.wallpaperCategoryRow.setBackgroundResource(
                chosen ? R.drawable.bg_wallpaper_category_selected : 0);
        holder.itemView.setOnClickListener(v -> listener.onCategory(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemWallpaperCategoryBinding binding;

        Holder(ItemWallpaperCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
