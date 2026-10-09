package com.demo.app.launcher.calendar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.demo.app.R;
import com.demo.app.launcher.ui.activity.calendar.CalendarActivity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** 到点弹出通知，并为重复日程安排下一次。 */
public class CalendarReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_REMIND = "com.demo.app.calendar.REMIND";
    public static final String EXTRA_ID = "event_id";
    private static final String CHANNEL = "calendar_reminders";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            CalendarAlarms.rescheduleAll(context);
            return;
        }
        if (!ACTION_REMIND.equals(intent.getAction())) {
            return;
        }
        long id = intent.getLongExtra(EXTRA_ID, 0L);
        CalendarEvent event = CalendarStore.find(id);
        if (event == null || event.reminderMinutes < 0) {
            return;
        }
        show(context, event);
        CalendarAlarms.schedule(context, event);
    }

    private static void show(@NonNull Context context, @NonNull CalendarEvent event) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL,
                    context.getString(R.string.calendar_channel),
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        String when = event.allDay
                ? context.getString(R.string.calendar_all_day)
                : DateTimeFormatter.ofPattern("HH:mm")
                .format(Instant.ofEpochMilli(event.startMillis).atZone(ZoneId.systemDefault()));
        Intent open = new Intent(context, CalendarActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(
                context,
                CalendarAlarms.notificationId(event.id),
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_calendar_notify)
                .setContentTitle(event.title)
                .setContentText(when)
                .setAutoCancel(true)
                .setContentIntent(content);
        if (event.soundLevel <= 0) {
            builder.setSilent(true);
        }
        manager.notify(CalendarAlarms.notificationId(event.id), builder.build());
    }
}
