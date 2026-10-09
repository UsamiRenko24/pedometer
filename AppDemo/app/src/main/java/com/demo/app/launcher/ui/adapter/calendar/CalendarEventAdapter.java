package com.demo.app.launcher.ui.adapter.calendar;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.launcher.calendar.CalendarEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** 选中日期下面的日程列表。 */
public final class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.Holder> {

    public interface Listener {
        void onOpen(long eventId);

        void onDelete(long eventId);
    }

    private static final int[] CARD_COLORS = {
            R.color.color_E7E4FF, R.color.color_FFEAD7, R.color.color_E7F8EE
    };
    private static final int[] MARK_COLORS = {
            R.color.color_5C7AFE, R.color.color_FF8A5B, R.color.color_34C759
    };

    private final List<CalendarEvent> items = new ArrayList<>();
    private final Listener listener;
    private final DateTimeFormatter clock = DateTimeFormatter.ofPattern("HH:mm");
    private CalendarEventRow opened;

    public CalendarEventAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submit(@NonNull List<CalendarEvent> source) {
        opened = null;
        items.clear();
        items.addAll(source);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_event, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CalendarEvent event = items.get(position);
        float density = holder.itemView.getResources().getDisplayMetrics().density;
        int mark = holder.itemView.getContext().getColor(
                MARK_COLORS[Math.floorMod(event.colorIndex, MARK_COLORS.length)]);
        GradientDrawable card = new GradientDrawable();
        card.setCornerRadius(16 * density);
        card.setColor(holder.itemView.getContext().getColor(
                CARD_COLORS[Math.floorMod(event.colorIndex, CARD_COLORS.length)]));
        holder.card.setBackground(card);
        GradientDrawable bar = new GradientDrawable();
        bar.setCornerRadius(2 * density);
        bar.setColor(mark);
        holder.bar.setBackground(bar);
        holder.title.setText(event.title);
        if (event.allDay) {
            holder.time.setText(R.string.calendar_all_day);
        } else {
            holder.time.setText(clock.format(
                    Instant.ofEpochMilli(event.startMillis).atZone(ZoneId.systemDefault())));
        }
        long eventId = event.id;
        holder.row.reset();
        if (opened == holder.row) {
            opened = null;
        }
        holder.delete.setOnClickListener(v -> listener.onDelete(eventId));
        holder.card.setOnClickListener(v -> {
            if (holder.card.getTranslationX() < 0f) {
                holder.row.reset();
                if (opened == holder.row) {
                    opened = null;
                }
                return;
            }
            listener.onOpen(eventId);
        });
        holder.row.setCallback(revealed -> {
                if (revealed) {
                    if (opened != null && opened != holder.row) {
                        opened.reset();
                    }
                    opened = holder.row;
                } else if (opened == holder.row) {
                    opened = null;
                }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final CalendarEventRow row;
        final View card;
        final View bar;
        final TextView title;
        final TextView time;
        final ImageView delete;

        Holder(@NonNull View itemView) {
            super(itemView);
            row = (CalendarEventRow) itemView;
            card = itemView.findViewById(R.id.calendar_event_card);
            bar = itemView.findViewById(R.id.calendar_event_bar);
            title = itemView.findViewById(R.id.calendar_event_title);
            time = itemView.findViewById(R.id.calendar_event_time);
            delete = itemView.findViewById(R.id.calendar_event_delete);
        }
    }
}
