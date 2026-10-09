package com.demo.app.launcher.calendar;

/** 一条日程。repeatType：0 单次，1 每天，2 每周。reminderMinutes：-1 不提醒，0 准时。 */
public final class CalendarEvent {
    public static final int ONCE = 0;
    public static final int DAILY = 1;
    public static final int WEEKLY = 2;

    public long id;
    public String title = "";
    public long startMillis;
    public long endMillis;
    public boolean allDay;
    public int repeatType;
    public int weekBits;
    public int reminderMinutes;
    public int soundLevel = 1;
    public int colorIndex;
}
