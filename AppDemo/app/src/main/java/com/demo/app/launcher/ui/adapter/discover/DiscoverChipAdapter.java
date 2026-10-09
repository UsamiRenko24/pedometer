package com.demo.app.launcher.ui.adapter.discover;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.databinding.ItemDiscoverChipBinding;
import com.demo.app.launcher.gamecenter.model.GameSectionUi;
import com.demo.app.launcher.gamecenter.model.SectionTitleResolver;

import java.util.List;

/** Discover 分类芯片。 */
public final class DiscoverChipAdapter extends RecyclerView.Adapter<DiscoverChipAdapter.Holder> {

    public interface Listener {
        void onChip(int position);
    }

    private final int categoryId;
    private final List<GameSectionUi> sections;
    private final Listener listener;
    private int selected;

    public DiscoverChipAdapter(
            int categoryId,
            @NonNull List<GameSectionUi> sections,
            int selected,
            @NonNull Listener listener) {
        this.categoryId = categoryId;
        this.sections = sections;
        this.selected = selected;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemDiscoverChipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        boolean chosen = position == selected;
        TextView label = holder.binding.discoverChipLabel;
        label.setText(SectionTitleResolver.INSTANCE.resolveChipTab(
                label.getContext(), categoryId, position, sections.get(position)));
        label.setBackgroundResource(chosen ? R.drawable.bg_discover_chip_on : R.drawable.bg_discover_chip_off);
        label.setTextColor(label.getContext().getColor(chosen ? R.color.color_white : R.color.color_4F7CFB));
        label.setOnClickListener(v -> {
            int index = holder.getBindingAdapterPosition();
            if (index == RecyclerView.NO_POSITION || index == selected) {
                return;
            }
            listener.onChip(index);
        });
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemDiscoverChipBinding binding;

        Holder(ItemDiscoverChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
