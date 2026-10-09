package com.demo.app.launcher.ui.adapter.discover;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.databinding.ItemDiscoverCardBinding;
import com.demo.app.launcher.gamecenter.model.GameItemUi;

import java.util.List;

/** Discover 横向小卡片。 */
public final class DiscoverCardAdapter extends RecyclerView.Adapter<DiscoverCardAdapter.Holder> {

    private final List<GameItemUi> items;

    public DiscoverCardAdapter(@NonNull List<GameItemUi> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemDiscoverCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        GameItemUi item = items.get(position);
        holder.binding.discoverCardName.setText(DiscoverItemBinder.titleOf(item));
        String rating = DiscoverItemBinder.ratingOf(item);
        holder.binding.discoverCardRating.setText(rating);
        holder.binding.discoverCardRating.setVisibility(rating.isEmpty() ? View.GONE : View.VISIBLE);
        DiscoverItemBinder.loadIcon(holder.binding.discoverCardIcon, item);
        holder.binding.discoverCardPlay.setOnClickListener(v -> DiscoverItemBinder.openLink(v, item));
        holder.itemView.setOnClickListener(v -> DiscoverItemBinder.openLink(v, item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemDiscoverCardBinding binding;

        Holder(ItemDiscoverCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
