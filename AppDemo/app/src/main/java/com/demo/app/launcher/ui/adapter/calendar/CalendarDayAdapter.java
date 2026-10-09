package com.demo.app.launcher.ui.adapter.calendar;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.launcher.calendar.CalendarEvent;
import com.demo.app.launcher.calendar.CalendarStore;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** 月视图里的 42 个日期格。 */
public final class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.Holder> {

    public interface Listener {
        void onDay(@NonNull LocalDate date, boolean inMonth);
    }

    private static final int[] MARK_COLORS = {
            R.color.color_5C7AFE, R.color.color_FF8A5B, R.color.color_34C759
    };

    private final List<LocalDate> cells = new ArrayList<>();
    private final List<CalendarEvent> events = new ArrayList<>();
    private final Listener listener;
    private YearMonth displayed = YearMonth.now();
    private LocalDate selected = LocalDate.now();

    public CalendarDayAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void submit(@NonNull List<LocalDate> nextCells, @NonNull YearMonth month,
                       @NonNull LocalDate selectedDate, @NonNull List<CalendarEvent> nextEvents) {
        cells.clear();
        cells.addAll(nextCells);
        displayed = month;
        selected = selectedDate;
        events.clear();
        events.addAll(nextEvents);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        LocalDate date = cells.get(position);
        boolean inMonth = YearMonth.from(date).equals(displayed);
        boolean isSelected = date.equals(selected);
        holder.text.setText(String.valueOf(date.getDayOfMonth()));
        holder.text.setBackgroundResource(isSelected ? R.drawable.bg_calendar_selected : 0);
        holder.text.setTextColor(holder.itemView.getContext().getColor(isSelected
                ? R.color.color_white
                : (inMonth ? R.color.color_1C1C1E : R.color.color_B0B4BE)));
        holder.dots.removeAllViews();
        int shown = 0;
        float density = holder.itemView.getResources().getDisplayMetrics().density;
        int dot = Math.round(5 * density);
        int gap = Math.round(2 * density);
        for (CalendarEvent event : events) {
            if (!CalendarStore.occurs(event, date)) {
                continue;
            }
            View mark = new View(holder.itemView.getContext());
            GradientDrawable oval = new GradientDrawable();
            oval.setShape(GradientDrawable.OVAL);
            oval.setColor(holder.itemView.getContext().getColor(
                    MARK_COLORS[Math.floorMod(event.colorIndex, MARK_COLORS.length)]));
            mark.setBackground(oval);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dot, dot);
            if (shown > 0) {
                params.setMarginStart(gap);
            }
            holder.dots.addView(mark, params);
            shown++;
            if (shown == 3) {
                break;
            }
        }
        holder.itemView.setOnClickListener(v -> listener.onDay(date, inMonth));
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView text;
        final LinearLayout dots;

        Holder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.calendar_day_text);
            dots = itemView.findViewById(R.id.calendar_day_dots);
        }
    }
}
