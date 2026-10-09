package com.demo.app.launcher.calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.utils.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 日程保存在本地。重复规则按开始日期展开，不单独存每一次。 */
public final class CalendarStore {

    private static final String KEY = "calendar_events";
    private static final Gson GSON = new Gson();
    private static final Type LIST = new TypeToken<List<CalendarEvent>>() {
    }.getType();

    private CalendarStore() {
    }

    @NonNull
    public static List<CalendarEvent> load() {
        String json = SPUtils.getInstance().getString(KEY, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        List<CalendarEvent> list = GSON.fromJson(json, LIST);
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    @Nullable
    public static CalendarEvent find(long id) {
        for (CalendarEvent event : load()) {
            if (event.id == id) {
                return event;
            }
        }
        return null;
    }

    public static void save(@NonNull CalendarEvent event) {
        List<CalendarEvent> events = load();
        if (event.id <= 0) {
            event.id = System.currentTimeMillis();
            event.colorIndex = events.size() % 3;
        }
        boolean replaced = false;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).id == event.id) {
                event.colorIndex = events.get(i).colorIndex;
                events.set(i, event);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            events.add(event);
        }
        SPUtils.getInstance().put(KEY, GSON.toJson(events));
    }

    public static void delete(long id) {
        List<CalendarEvent> events = load();
        events.removeIf(event -> event.id == id);
        SPUtils.getInstance().put(KEY, GSON.toJson(events));
    }

    public static boolean occurs(@NonNull CalendarEvent event, @NonNull LocalDate day) {
        LocalDate start = startDate(event);
        if (day.isBefore(start)) {
            return false;
        }
        if (event.repeatType == CalendarEvent.DAILY) {
            return true;
        }
        if (event.repeatType == CalendarEvent.WEEKLY) {
            return (event.weekBits & bit(day.getDayOfWeek())) != 0;
        }
        return day.equals(start);
    }

    @NonNull
    public static List<CalendarEvent> onDay(@NonNull LocalDate day) {
        List<CalendarEvent> result = new ArrayList<>();
        for (CalendarEvent event : load()) {
            if (occurs(event, day)) {
                result.add(event);
            }
        }
        result.sort(Comparator
                .comparing((CalendarEvent event) -> !event.allDay)
                .thenComparingLong(event -> event.startMillis));
        return result;
    }

    /** 下一次该响铃的时间。没有则返回 0。 */
    public static long nextAlarmMillis(@NonNull CalendarEvent event, long afterMillis) {
        if (event.reminderMinutes < 0) {
            return 0L;
        }
        ZoneId zone = ZoneId.systemDefault();
        LocalDate start = startDate(event);
        LocalDate cursor = LocalDate.now(zone);
        if (cursor.isBefore(start)) {
            cursor = start;
        }
        LocalTime time = event.allDay
                ? LocalTime.of(9, 0)
                : LocalTime.ofInstant(Instant.ofEpochMilli(event.startMillis), zone);
        int limit = event.repeatType == CalendarEvent.ONCE ? 1 : 800;
        if (event.repeatType == CalendarEvent.ONCE) {
            cursor = start;
        }
        for (int i = 0; i < limit; i++) {
            if (occurs(event, cursor)) {
                long trigger = ZonedDateTime.of(cursor, time, zone).toInstant().toEpochMilli()
                        - event.reminderMinutes * 60_000L;
                if (trigger > afterMillis) {
                    return trigger;
                }
                if (event.repeatType == CalendarEvent.ONCE) {
                    return 0L;
                }
            }
            cursor = cursor.plusDays(1);
        }
        return 0L;
    }

    @NonNull
    public static LocalDate startDate(@NonNull CalendarEvent event) {
        return Instant.ofEpochMilli(event.startMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static int bit(@NonNull DayOfWeek day) {
        return 1 << (day.getValue() - 1);
    }
}
