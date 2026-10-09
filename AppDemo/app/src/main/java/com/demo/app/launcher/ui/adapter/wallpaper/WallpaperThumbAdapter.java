package com.demo.app.launcher.ui.adapter.wallpaper;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.app.databinding.ItemWallpaperThumbBinding;
import com.demo.app.launcher.wallpaper.model.WallpaperItemUi;

import java.util.List;

/** 壁纸缩略图网格。 */
public final class WallpaperThumbAdapter extends RecyclerView.Adapter<WallpaperThumbAdapter.Holder> {

    public interface Listener {
        void onThumb(int position);
    }

    private final List<WallpaperItemUi> wallpapers;
    private final Listener listener;

    public WallpaperThumbAdapter(@NonNull List<WallpaperItemUi> wallpapers, @NonNull Listener listener) {
        this.wallpapers = wallpapers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemWallpaperThumbBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        WallpaperItemUi item = wallpapers.get(position);
        Glide.with(holder.binding.wallpaperThumb)
                .load(item.getThumbUrl())
                .centerCrop()
                .into(holder.binding.wallpaperThumb);
        holder.itemView.setOnClickListener(v -> {
            int index = holder.getBindingAdapterPosition();
            if (index != RecyclerView.NO_POSITION) {
                listener.onThumb(index);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wallpapers.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemWallpaperThumbBinding binding;

        Holder(ItemWallpaperThumbBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
