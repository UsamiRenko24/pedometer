package com.demo.app.launcher.ui.activity.folder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.databinding.ItemFolderFileBinding;

import java.util.ArrayList;
import java.util.List;

/** 文件行。 */
public final class FolderFileAdapter extends RecyclerView.Adapter<FolderFileAdapter.Holder> {

    public interface Click {
        void onFile(@NonNull FolderEntry entry);
    }

    private final List<FolderEntry> items = new ArrayList<>();
    private final Click click;

    public FolderFileAdapter(@NonNull Click click) {
        this.click = click;
    }

    public void submit(@NonNull List<FolderEntry> next) {
        items.clear();
        items.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFolderFileBinding binding = ItemFolderFileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        FolderEntry entry = items.get(position);
        holder.binding.folderFileIcon.setImageResource(entry.kind.icon);
        holder.binding.folderFileName.setText(entry.name);
        holder.binding.folderFileMeta.setText(entry.meta(holder.itemView.getContext()));
        holder.itemView.setOnClickListener(v -> click.onFile(entry));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemFolderFileBinding binding;

        Holder(@NonNull ItemFolderFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /** 首页最近文件不用列表控件，直接塞进行布局。 */
    public static void bindRow(@NonNull View row, @NonNull FolderEntry entry, @NonNull Click click) {
        ItemFolderFileBinding binding = ItemFolderFileBinding.bind(row);
        binding.folderFileIcon.setImageResource(entry.kind.icon);
        binding.folderFileName.setText(entry.name);
        binding.folderFileMeta.setText(entry.meta(row.getContext()));
        row.setOnClickListener(v -> click.onFile(entry));
    }
}
