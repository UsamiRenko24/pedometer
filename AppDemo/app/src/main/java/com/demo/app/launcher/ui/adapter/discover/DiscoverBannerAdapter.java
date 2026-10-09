package com.demo.app.launcher.ui.adapter.discover;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.databinding.ItemDiscoverBannerBinding;
import com.demo.app.launcher.gamecenter.model.GameItemUi;

import java.util.List;

/** Discover 顶部轮播。 */
public final class DiscoverBannerAdapter extends RecyclerView.Adapter<DiscoverBannerAdapter.Holder> {

    private final List<GameItemUi> items;
    private final int categoryId;

    public DiscoverBannerAdapter(@NonNull List<GameItemUi> items, int categoryId) {
        this.items = items;
        this.categoryId = categoryId;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemDiscoverBannerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        GameItemUi item = items.get(position);
        holder.binding.discoverBannerTitle.setText(DiscoverItemBinder.titleOf(item));
        holder.binding.discoverBannerDesc.setText(item.getDisplayDesc());
        holder.binding.discoverBannerAction.setText(categoryId == DiscoverFeedAdapter.CATEGORY_EXPLORE
                ? R.string.discover_get_for_free
                : R.string.discover_play);
        DiscoverItemBinder.loadBanner(holder.binding.discoverBannerImage, item);
        holder.binding.discoverBannerAction.setOnClickListener(v -> DiscoverItemBinder.openLink(v, item));
        holder.itemView.setOnClickListener(v -> DiscoverItemBinder.openLink(v, item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemDiscoverBannerBinding binding;

        Holder(ItemDiscoverBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
