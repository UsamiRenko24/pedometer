package com.demo.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.data.enum2.Language;
import com.demo.app.databinding.ItemLanguageBinding;

import java.util.Arrays;
import java.util.List;

/** 语言列表。点某一行后由页面立刻切换语言。 */
public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.Holder> {

    public interface OnPickListener {
        void onPick(Language language);
    }

    private final List<Language> items = Arrays.asList(Language.values());
    private int selectedIndex;
    private OnPickListener listener;

    /** 按已保存的语言名定位当前行。找不到时选第一项。 */
    public LanguageAdapter(String savedName) {
        selectedIndex = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).name().equals(savedName)) {
                selectedIndex = i;
                break;
            }
        }
    }

    public void setOnPickListener(OnPickListener listener) {
        this.listener = listener;
    }

    public Language getSelected() {
        return items.get(selectedIndex);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLanguageBinding binding = ItemLanguageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Language language = items.get(position);
        holder.binding.tvName.setText(language.getStringRes());
        holder.binding.ivSelected.setSelected(position == selectedIndex);
        holder.itemView.setOnClickListener(v -> select(holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** 取消上一行，勾上新行。 */
    private void select(int position) {
        if (position == RecyclerView.NO_POSITION || position == selectedIndex) {
            return;
        }
        int previous = selectedIndex;
        selectedIndex = position;
        notifyItemChanged(previous);
        notifyItemChanged(selectedIndex);
        if (listener != null) {
            listener.onPick(items.get(selectedIndex));
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemLanguageBinding binding;

        Holder(ItemLanguageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
