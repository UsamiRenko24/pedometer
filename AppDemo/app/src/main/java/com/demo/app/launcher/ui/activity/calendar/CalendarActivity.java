package com.demo.app.launcher.ui.activity.calendar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.databinding.ActivityCalendarBinding;
import com.demo.app.launcher.ads.LauncherBackInsertPlaceholder;
import com.demo.app.launcher.ads.LauncherBackPressAds;
import com.demo.app.launcher.ads.LauncherBuiltInAds;
import com.demo.app.launcher.calendar.CalendarAlarms;
import com.demo.app.launcher.calendar.CalendarEvent;
import com.demo.app.launcher.calendar.CalendarStore;
import com.demo.app.launcher.ui.adapter.calendar.CalendarDayAdapter;
import com.demo.app.launcher.ui.adapter.calendar.CalendarEventAdapter;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 鏈堣鍥俱€傚乏鍙虫粦鍔ㄦ崲鏈堬紝鐐规棩鏈熺湅褰撳ぉ鏃ョ▼銆?*/
public class CalendarActivity extends BaseActivity<ActivityCalendarBinding> {

    private YearMonth displayed;
    private LocalDate selected;
    private final List<CalendarEvent> events = new ArrayList<>();
    private final List<LocalDate> cells = new ArrayList<>();
    private final CalendarDayAdapter dayAdapter = new CalendarDayAdapter(this::onDay);
    private final CalendarEventAdapter eventAdapter = new CalendarEventAdapter(new CalendarEventAdapter.Listener() {
        @Override
        public void onOpen(long eventId) {
            CalendarEditActivity.edit(CalendarActivity.this, eventId);
        }

        @Override
        public void onDelete(long eventId) {
            confirmDelete(eventId);
        }
    });

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, CalendarActivity.class);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityCalendarBinding getViewBinding() {
        return ActivityCalendarBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        applyImmersive();
        int headerStart = binding.calendarHeader.getPaddingStart();
        int headerTop = binding.calendarHeader.getPaddingTop();
        int headerEnd = binding.calendarHeader.getPaddingEnd();
        int headerBottom = binding.calendarHeader.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(binding.calendarRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypes());
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            binding.calendarHeader.setPadding(headerStart, headerTop + bars.top, headerEnd, headerBottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), false, true);
    }

    @Override
    protected void initViews() {
        selected = LocalDate.now();
        displayed = YearMonth.from(selected);
        binding.calendarDays.setLayoutManager(new GridLayoutManager(this, 7));
        binding.calendarDays.setAdapter(dayAdapter);
        binding.calendarEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.calendarEvents.setAdapter(eventAdapter);
        fillWeekdays();
        GestureDetector detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || Math.abs(e1.getX() - e2.getX()) < dp(48)) {
                    return false;
                }
                shiftMonth(e1.getX() > e2.getX() ? 1 : -1);
                return true;
            }
        });
        binding.calendarDays.setOnTouchListener((v, event) -> {
            detector.onTouchEvent(event);
            return false;
        });
        LauncherBuiltInAds.show(binding.calendarBottomAd.getRoot());
        LauncherBackInsertPlaceholder.preload(this);
        LauncherBackPressAds.install(this, this::finish);
    }

    @Override
    protected void initEvents() {
        binding.calendarToday.setOnClickListener(v -> {
            selected = LocalDate.now();
            displayed = YearMonth.from(selected);
            bind();
        });
        binding.calendarAdd.setOnClickListener(v ->
                CalendarEditActivity.create(this, selected.toEpochDay()));
        binding.calendarDayBadge.setOnClickListener(v -> showJump());
    }

    @Override
    protected void onResume() {
        super.onResume();
        CalendarAlarms.rescheduleAll(this);
        bind();
    }

    private void shiftMonth(int delta) {
        displayed = displayed.plusMonths(delta);
        int day = Math.min(selected.getDayOfMonth(), displayed.lengthOfMonth());
        selected = displayed.atDay(day);
        bind();
    }

    private void bind() {
        events.clear();
        events.addAll(CalendarStore.load());
        Locale locale = Locale.getDefault();
        binding.calendarMonth.setText(displayed.getMonth().getDisplayName(TextStyle.FULL, locale));
        binding.calendarYear.setText(String.valueOf(displayed.getYear()));
        binding.calendarDayBadge.setText(String.valueOf(LocalDate.now().getDayOfMonth()));
        cells.clear();
        LocalDate first = displayed.atDay(1);
        LocalDate cursor = first.minusDays(first.getDayOfWeek().getValue() - 1L);
        for (int i = 0; i < 42; i++) {
            cells.add(cursor);
            cursor = cursor.plusDays(1);
        }
        dayAdapter.submit(cells, displayed, selected, events);
        eventAdapter.submit(CalendarStore.onDay(selected));
        binding.calendarToday.setVisibility(
                selected.equals(LocalDate.now()) ? View.GONE : View.VISIBLE);
    }

    private void onDay(@NonNull LocalDate date, boolean inMonth) {
        selected = date;
        if (!inMonth) {
            displayed = YearMonth.from(date);
        }
        bind();
    }

    private void fillWeekdays() {
        binding.calendarWeekdays.removeAllViews();
        Locale locale = Locale.getDefault();
        for (int i = 0; i < 7; i++) {
            TextView label = new TextView(this);
            label.setText(DayOfWeek.MONDAY.plus(i).getDisplayName(TextStyle.SHORT, locale));
            label.setGravity(android.view.Gravity.CENTER);
            label.setTextColor(getColor(R.color.color_9A9A9A));
            label.setTextSize(12);
            binding.calendarWeekdays.addView(label, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        }
    }

    private void showJump() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_calendar_jump, null, false);
        NumberPicker year = view.findViewById(R.id.calendar_jump_year);
        NumberPicker month = view.findViewById(R.id.calendar_jump_month);
        NumberPicker day = view.findViewById(R.id.calendar_jump_day);
        stylePicker(year);
        stylePicker(month);
        stylePicker(day);
        TextView subtitle = view.findViewById(R.id.calendar_jump_subtitle);
        year.setMaxValue(2100);
        year.setMinValue(1970);
        year.setValue(selected.getYear());
        month.setMaxValue(12);
        month.setMinValue(1);
        month.setDisplayedValues(new String[]{
                "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"
        });
        month.setValue(selected.getMonthValue());
        day.setMaxValue(selected.lengthOfMonth());
        day.setMinValue(1);
        day.setValue(selected.getDayOfMonth());
        Runnable refresh = () -> {
            int max = YearMonth.of(year.getValue(), month.getValue()).lengthOfMonth();
            int current = Math.min(Math.max(day.getValue(), 1), max);
            day.setMaxValue(max);
            day.setValue(current);
            LocalDate date = LocalDate.of(year.getValue(), month.getValue(), day.getValue());
            subtitle.setText(date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                    + " "
                    + date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
        };
        year.setOnValueChangedListener((picker, oldVal, newVal) -> refresh.run());
        month.setOnValueChangedListener((picker, oldVal, newVal) -> refresh.run());
        day.setOnValueChangedListener((picker, oldVal, newVal) -> refresh.run());
        refresh.run();
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.setOnShowListener(shown -> {
            FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet == null) {
                return;
            }
            sheet.setBackgroundColor(Color.TRANSPARENT);
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(sheet);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            int side = dp(24);
            lp.leftMargin = side;
            lp.rightMargin = side;
            ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
                lp.bottomMargin = dp(16) + insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                v.setLayoutParams(lp);
                return insets;
            });
            ViewCompat.requestApplyInsets(view);
        });
        view.findViewById(R.id.calendar_jump_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.calendar_jump_confirm).setOnClickListener(v -> {
            selected = LocalDate.of(year.getValue(), month.getValue(), day.getValue());
            displayed = YearMonth.from(selected);
            dialog.dismiss();
            bind();
        });
        dialog.show();
    }

    private void stylePicker(@NonNull NumberPicker picker) {
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setWrapSelectorWheel(false);
        picker.setSelectionDividerHeight(0);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void confirmDelete(long id) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.calendar_delete_confirm)
                .setNegativeButton(R.string.calendar_cancel, null)
                .setPositiveButton(R.string.calendar_confirm, (dialog, which) -> {
                    CalendarStore.delete(id);
                    CalendarAlarms.cancel(this, id);
                    bind();
                })
                .show();
    }
}
