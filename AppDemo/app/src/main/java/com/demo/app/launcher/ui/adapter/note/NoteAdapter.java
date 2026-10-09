package com.demo.app.launcher.ui.adapter.note;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.launcher.note.NoteEntry;
import com.demo.app.launcher.note.NoteStyles;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** 笔记卡片。点卡片打开，点圆圈选中。 */
public final class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.Holder> {

    public interface Listener {
        void onOpen(long noteId);

        void onToggle(long noteId);
    }

    private final List<NoteEntry> notes = new ArrayList<>();
    private final Set<Long> selected = new HashSet<>();
    private final Listener listener;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

    public NoteAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submit(@NonNull List<NoteEntry> next, @NonNull Set<Long> selectedIds) {
        notes.clear();
        notes.addAll(next);
        selected.clear();
        selected.addAll(selectedIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NoteEntry note = notes.get(position);
        int textColor = NoteStyles.resolvedTextColor(holder.itemView.getContext(), note.textColor);
        holder.card.setBackgroundResource(NoteStyles.backgroundRes(note.bgIndex));
        holder.title.setText(note.title);
        holder.title.setTextColor(textColor);
        holder.date.setText(format.format(new Date(note.updatedMillis)));
        holder.body.setText(note.body);
        holder.body.setTextColor(textColor);
        holder.check.setImageResource(selected.contains(note.id)
                ? R.drawable.icon_home_app_note_select
                : R.drawable.icon_home_app_note_unselect);
        holder.itemView.setOnClickListener(v -> listener.onOpen(note.id));
        holder.check.setOnClickListener(v -> listener.onToggle(note.id));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final LinearLayout card;
        final TextView title;
        final TextView date;
        final TextView body;
        final ImageView check;

        Holder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.note_item_card);
            title = itemView.findViewById(R.id.note_item_title);
            date = itemView.findViewById(R.id.note_item_date);
            body = itemView.findViewById(R.id.note_item_body);
            check = itemView.findViewById(R.id.note_item_check);
        }
    }
}
