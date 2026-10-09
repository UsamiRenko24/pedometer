package com.demo.app.launcher.ui.adapter.wallpaper;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.app.R;
import com.demo.app.launcher.utils.WallpaperDataUtils;
import com.demo.app.launcher.wallpaper.model.WallpaperDetailUi;
import com.demo.app.launcher.wallpaper.model.WallpaperItemUi;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.Unit;

/** 壁纸详情左右滑动页。 */
public class WallpaperPreviewPagerAdapter extends RecyclerView.Adapter<WallpaperPreviewPagerAdapter.PageHolder> {

    public interface DetailListener {
        void onDetailLoaded(int position, @NonNull WallpaperDetailUi detail);
    }

    private final List<WallpaperItemUi> items;
    private final Map<String, WallpaperDetailUi> detailCache;
    private final Set<String> loadingIds;
    @Nullable
    private final DetailListener detailListener;

    public WallpaperPreviewPagerAdapter(
            @NonNull List<WallpaperItemUi> items,
            @NonNull Map<String, WallpaperDetailUi> detailCache,
            @Nullable DetailListener detailListener) {
        this.items = items;
        this.detailCache = detailCache;
        this.detailListener = detailListener;
        this.loadingIds = new HashSet<>();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wallpaper_preview_page, parent, false);
        return new PageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        WallpaperItemUi item = items.get(position);
        WallpaperDetailUi cached = detailCache.get(item.getId());
        String displayUrl = cached != null && !cached.getImageUrl().isEmpty()
                ? cached.getImageUrl()
                : item.getThumbUrl();
        holder.show(displayUrl);
        if (cached != null) {
            return;
        }
        requestDetail(holder, position, item);
    }

    public void preload(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        WallpaperItemUi item = items.get(position);
        if (detailCache.containsKey(item.getId()) || loadingIds.contains(item.getId())) {
            return;
        }
        requestDetail(null, position, item);
    }

    private void requestDetail(@Nullable PageHolder holder, int position, @NonNull WallpaperItemUi item) {
        if (TextUtils.isEmpty(item.getId()) || loadingIds.contains(item.getId())) {
            return;
        }
        loadingIds.add(item.getId());
        WallpaperDataUtils.INSTANCE.loadDetail(item.getId(), (detail, error) -> {
            loadingIds.remove(item.getId());
            if (detail == null) {
                return Unit.INSTANCE;
            }
            detailCache.put(item.getId(), detail);
            if (detailListener != null) {
                detailListener.onDetailLoaded(position, detail);
            }
            if (holder != null && holder.getBindingAdapterPosition() == position) {
                String url = !detail.getImageUrl().isEmpty() ? detail.getImageUrl() : item.getThumbUrl();
                holder.show(url);
            } else {
                notifyItemChanged(position);
            }
            return Unit.INSTANCE;
        });
    }

    static final class PageHolder extends RecyclerView.ViewHolder {
        private final ImageView image;

        PageHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.wallpaper_preview_page_image);
        }

        void show(@NonNull String url) {
            if (url.isEmpty()) {
                return;
            }
            Glide.with(image).load(url).centerCrop().into(image);
        }
    }
}
