package com.demo.app.launcher.ui.activity.calendar;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.demo.app.R;
import com.demo.app.databinding.ActivityCalendarEditBinding;
import com.demo.app.databinding.DialogCalendarRepeatBinding;
import com.demo.app.launcher.calendar.CalendarAlarms;
import com.demo.app.launcher.calendar.CalendarEvent;
import com.demo.app.launcher.calendar.CalendarStore;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** 新建或修改日程。重复、提示音和提醒从这里选。 */
public class CalendarEditActivity extends BaseActivity<ActivityCalendarEditBinding> {

    private static final String EXTRA_DAY = "calendar_day";
    private static final String EXTRA_ID = "calendar_id";
    private static final int[] REMINDER_MINUTES = {0, -1, 5, 10, 15, 30, 60, 1440};
    private static final int[] REMINDER_LABELS = {
            R.string.calendar_reminder_mine,
            R.string.calendar_reminder_none,
            R.string.calendar_reminder_5,
            R.string.calendar_reminder_10,
            R.string.calendar_reminder_15,
            R.string.calendar_reminder_30,
            R.string.calendar_reminder_60,
            R.string.calendar_reminder_day
    };
    private static final int[] SOUND_LABELS = {
            R.string.calendar_sound_low,
            R.string.calendar_sound_middle,
            R.string.calendar_sound_high
    };

    private long eventId;
    private LocalDate date;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private int repeatType;
    private int weekBits;
    private int soundLevel = 1;
    private int reminderMinutes;
    private boolean saveAfterPermission;

    private final ActivityResultLauncher<String> notifyPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!saveAfterPermission) {
                    return;
                }
                saveAfterPermission = false;
                if (!granted) {
                    Toast.makeText(this, R.string.calendar_notify_denied, Toast.LENGTH_SHORT).show();
                }
                commit();
            });

    public static void create(@NonNull Context context, long epochDay) {
        Intent intent = new Intent(context, CalendarEditActivity.class);
        intent.putExtra(EXTRA_DAY, epochDay);
        context.startActivity(intent);
    }

    public static void edit(@NonNull Context context, long eventId) {
        Intent intent = new Intent(context, CalendarEditActivity.class);
        intent.putExtra(EXTRA_ID, eventId);
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityCalendarEditBinding getViewBinding() {
        return ActivityCalendarEditBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.calendarEditRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypesWithIme()
                            | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), true, true);
    }

    @Override
    protected void initData(@Nullable Bundle bundle) {
        eventId = getIntent().getLongExtra(EXTRA_ID, 0L);
        CalendarEvent event = CalendarStore.find(eventId);
        ZoneId zone = ZoneId.systemDefault();
        if (event == null) {
            eventId = 0L;
            date = LocalDate.ofEpochDay(getIntent().getLongExtra(EXTRA_DAY, LocalDate.now().toEpochDay()));
            ZonedDateTime now = ZonedDateTime.now(zone);
            startHour = now.getHour();
            startMinute = now.getMinute();
            endHour = startHour;
            endMinute = startMinute;
            return;
        }
        date = CalendarStore.startDate(event);
        ZonedDateTime start = Instant.ofEpochMilli(event.startMillis).atZone(zone);
        ZonedDateTime end = Instant.ofEpochMilli(event.endMillis).atZone(zone);
        startHour = start.getHour();
        startMinute = start.getMinute();
        endHour = end.getHour();
        endMinute = end.getMinute();
        repeatType = event.repeatType;
        weekBits = event.weekBits;
        soundLevel = event.soundLevel;
        reminderMinutes = event.reminderMinutes;
        binding.calendarEditTitle.setText(event.title);
        binding.calendarEditAllDay.setChecked(event.allDay);
    }

    @Override
    protected void initViews() {
        refreshTimes();
        refreshRepeat();
        refreshSound();
        refreshReminder();
        applyAllDay();
    }

    @Override
    protected void initEvents() {
        binding.calendarEditBack.setOnClickListener(v -> finish());
        binding.calendarEditCancel.setOnClickListener(v -> finish());
        binding.calendarEditStart.setOnClickListener(v -> pickTime(true));
        binding.calendarEditEnd.setOnClickListener(v -> pickTime(false));
        binding.calendarEditAllDay.setOnCheckedChangeListener((button, checked) -> applyAllDay());
        binding.calendarEditRepeat.setOnClickListener(v -> showRepeat());
        binding.calendarEditSound.setOnClickListener(v -> showSound());
        binding.calendarEditReminder.setOnClickListener(v -> showReminder());
        binding.calendarEditSave.setOnClickListener(v -> onSave());
    }

    private void pickTime(boolean start) {
        boolean is24 = DateFormat.is24HourFormat(this);
        int hour = start ? startHour : endHour;
        int minute = start ? startMinute : endMinute;
        new TimePickerDialog(this, (view, pickedHour, pickedMinute) -> {
            if (start) {
                startHour = pickedHour;
                startMinute = pickedMinute;
                if (minutes(endHour, endMinute) < minutes(startHour, startMinute)) {
                    endHour = startHour;
                    endMinute = startMinute;
                }
            } else {
                endHour = pickedHour;
                endMinute = pickedMinute;
            }
            refreshTimes();
        }, hour, minute, is24).show();
    }

    private void applyAllDay() {
        int visibility = binding.calendarEditAllDay.isChecked() ? View.GONE : View.VISIBLE;
        binding.calendarEditStart.setVisibility(visibility);
        binding.calendarEditEnd.setVisibility(visibility);
    }

    private void refreshTimes() {
        binding.calendarEditStart.setText(formatTime(startHour, startMinute));
        binding.calendarEditEnd.setText(formatTime(endHour, endMinute));
    }

    private void refreshRepeat() {
        int label = R.string.calendar_repeat_none;
        if (repeatType == CalendarEvent.DAILY) {
            label = R.string.calendar_daily;
        } else if (repeatType == CalendarEvent.WEEKLY) {
            label = R.string.calendar_weekly;
        }
        binding.calendarEditRepeatLabel.setText(label);
    }

    private void refreshSound() {
        int index = Math.max(0, Math.min(soundLevel, SOUND_LABELS.length - 1));
        binding.calendarEditSoundLabel.setText(SOUND_LABELS[index]);
    }

    private void refreshReminder() {
        binding.calendarEditReminderLabel.setText(reminderLabel(reminderMinutes));
    }

    private int reminderLabel(int minutes) {
        for (int i = 0; i < REMINDER_MINUTES.length; i++) {
            if (REMINDER_MINUTES[i] == minutes) {
                return REMINDER_LABELS[i];
            }
        }
        return R.string.calendar_reminder_mine;
    }

    private void showRepeat() {
        DialogCalendarRepeatBinding dialogBinding = DialogCalendarRepeatBinding.inflate(getLayoutInflater());
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(dialogBinding.getRoot());
        if (repeatType == CalendarEvent.DAILY) {
            dialogBinding.calendarRepeatDaily.setChecked(true);
        } else if (repeatType == CalendarEvent.WEEKLY) {
            dialogBinding.calendarRepeatWeekly.setChecked(true);
        } else {
            dialogBinding.calendarRepeatOnce.setChecked(true);
        }
        int[] bits = {weekBits};
        buildWeekdays(dialogBinding.calendarRepeatDays, bits);
        dialogBinding.calendarRepeatDays.setVisibility(
                repeatType == CalendarEvent.WEEKLY ? View.VISIBLE : View.GONE);
        dialogBinding.calendarRepeatGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            boolean weekly = checkedId == R.id.calendar_repeat_weekly;
            dialogBinding.calendarRepeatDays.setVisibility(weekly ? View.VISIBLE : View.GONE);
            if (weekly && bits[0] == 0) {
                bits[0] = CalendarStore.bit(date.getDayOfWeek());
                buildWeekdays(dialogBinding.calendarRepeatDays, bits);
            }
        });
        dialog.setOnDismissListener(d -> {
            int checked = dialogBinding.calendarRepeatGroup.getCheckedRadioButtonId();
            if (checked == R.id.calendar_repeat_daily) {
                repeatType = CalendarEvent.DAILY;
            } else if (checked == R.id.calendar_repeat_weekly) {
                repeatType = CalendarEvent.WEEKLY;
                weekBits = bits[0];
            } else {
                repeatType = CalendarEvent.ONCE;
            }
            refreshRepeat();
        });
        dialog.show();
        sizeDialog(dialog);
    }

    private void buildWeekdays(@NonNull LinearLayout row, @NonNull int[] bits) {
        row.removeAllViews();
        Locale locale = Locale.getDefault();
        boolean english = locale.getLanguage().startsWith("en");
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = DayOfWeek.MONDAY.plus(i);
            int bit = CalendarStore.bit(day);
            TextView chip = new TextView(this);
            chip.setText(day.getDisplayName(english ? TextStyle.SHORT : TextStyle.NARROW, locale));
            chip.setGravity(Gravity.CENTER);
            chip.setTextSize(12);
            boolean on = (bits[0] & bit) != 0;
            chip.setBackgroundResource(on ? R.drawable.bg_calendar_week_on : 0);
            chip.setTextColor(getColor(on ? R.color.color_white : R.color.color_B0B4BE));
            int size = dp(32);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            if (i > 0) {
                params.setMarginStart(dp(4));
            }
            chip.setOnClickListener(v -> {
                if ((bits[0] & bit) != 0) {
                    bits[0] &= ~bit;
                } else {
                    bits[0] |= bit;
                }
                buildWeekdays(row, bits);
            });
            row.addView(chip, params);
        }
    }

    private void showSound() {
        String[] labels = new String[SOUND_LABELS.length];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = getString(SOUND_LABELS[i]);
        }
        showChoices(R.string.calendar_sound, labels, soundLevel, index -> {
            soundLevel = index;
            refreshSound();
        });
    }

    private void showReminder() {
        String[] labels = new String[REMINDER_LABELS.length];
        int selected = 0;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = getString(REMINDER_LABELS[i]);
            if (REMINDER_MINUTES[i] == reminderMinutes) {
                selected = i;
            }
        }
        showChoices(R.string.calendar_reminder, labels, selected, index -> {
            reminderMinutes = REMINDER_MINUTES[index];
            refreshReminder();
        });
    }

    private void showChoices(int title, @NonNull String[] labels, int selected, @NonNull ChoiceListener listener) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_calendar_dialog);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);
        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setGravity(Gravity.CENTER);
        heading.setTextColor(getColor(R.color.color_1C1C1E));
        heading.setTextSize(17);
        heading.setTypeface(heading.getTypeface(), android.graphics.Typeface.BOLD);
        root.addView(heading);
        android.app.Dialog dialog = new android.app.Dialog(this);
        for (int i = 0; i < labels.length; i++) {
            TextView row = new TextView(this);
            row.setText(labels[i]);
            row.setTextSize(16);
            row.setPadding(0, dp(12), 0, dp(12));
            row.setTextColor(getColor(i == selected ? R.color.color_4F7CFB : R.color.color_1C1C1E));
            int index = i;
            row.setOnClickListener(v -> {
                listener.onChoose(index);
                dialog.dismiss();
            });
            root.addView(row);
        }
        dialog.setContentView(root);
        dialog.show();
        sizeDialog(dialog);
    }

    private void sizeDialog(@NonNull android.app.Dialog dialog) {
        if (dialog.getWindow() == null) {
            return;
        }
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.86f),
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void onSave() {
        if (!valid()) {
            return;
        }
        if (reminderMinutes >= 0
                && Build.VERSION.SDK_INT >= 33
                && !CalendarAlarms.canNotify(this)) {
            saveAfterPermission = true;
            notifyPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        if (reminderMinutes >= 0 && !CalendarAlarms.canNotify(this)) {
            Toast.makeText(this, R.string.calendar_notify_denied, Toast.LENGTH_SHORT).show();
        }
        commit();
    }

    private boolean valid() {
        if (TextUtils.isEmpty(binding.calendarEditTitle.getText().toString().trim())) {
            Toast.makeText(this, R.string.calendar_title_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!binding.calendarEditAllDay.isChecked()
                && minutes(endHour, endMinute) < minutes(startHour, startMinute)) {
            Toast.makeText(this, R.string.calendar_time_invalid, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (repeatType == CalendarEvent.WEEKLY && weekBits == 0) {
            Toast.makeText(this, R.string.calendar_week_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void commit() {
        CalendarEvent event = new CalendarEvent();
        event.id = eventId;
        event.title = binding.calendarEditTitle.getText().toString().trim();
        event.allDay = binding.calendarEditAllDay.isChecked();
        event.repeatType = repeatType;
        event.weekBits = weekBits;
        event.soundLevel = soundLevel;
        event.reminderMinutes = reminderMinutes;
        ZoneId zone = ZoneId.systemDefault();
        if (event.allDay) {
            event.startMillis = date.atStartOfDay(zone).toInstant().toEpochMilli();
            event.endMillis = date.atTime(23, 59).atZone(zone).toInstant().toEpochMilli();
        } else {
            event.startMillis = ZonedDateTime.of(date, LocalTime.of(startHour, startMinute), zone)
                    .toInstant().toEpochMilli();
            event.endMillis = ZonedDateTime.of(date, LocalTime.of(endHour, endMinute), zone)
                    .toInstant().toEpochMilli();
        }
        CalendarStore.save(event);
        if (event.reminderMinutes < 0) {
            CalendarAlarms.cancel(this, event.id);
        } else {
            CalendarAlarms.schedule(this, event);
        }
        finish();
    }

    @NonNull
    private String formatTime(int hour, int minute) {
        LocalTime time = LocalTime.of(hour, minute);
        String pattern = DateFormat.is24HourFormat(this) ? "HH:mm" : "h:mm a";
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()));
    }

    private static int minutes(int hour, int minute) {
        return hour * 60 + minute;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private interface ChoiceListener {
        void onChoose(int index);
    }
}
