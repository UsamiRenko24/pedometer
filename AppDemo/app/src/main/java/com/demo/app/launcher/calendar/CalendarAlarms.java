package com.demo.app.launcher.calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/** 按每条日程的下一次发生时间注册或取消提醒。 */
public final class CalendarAlarms {

    private CalendarAlarms() {
    }

    public static void rescheduleAll(@NonNull Context context) {
        for (CalendarEvent event : CalendarStore.load()) {
            schedule(context, event);
        }
    }

    public static void schedule(@NonNull Context context, @NonNull CalendarEvent event) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            return;
        }
        PendingIntent pending = pending(context, event.id);
        manager.cancel(pending);
        if (!canNotify(context)) {
            return;
        }
        long trigger = CalendarStore.nextAlarmMillis(event, System.currentTimeMillis());
        if (trigger <= 0L) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending);
        } else {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending);
        }
    }

    public static void cancel(@NonNull Context context, long eventId) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(pending(context, eventId));
        }
        NotificationManagerCompat.from(context).cancel(notificationId(eventId));
    }

    public static boolean canNotify(@NonNull Context context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    static int notificationId(long eventId) {
        return (int) (eventId ^ (eventId >>> 32));
    }

    @NonNull
    private static PendingIntent pending(@NonNull Context context, long eventId) {
        Intent intent = new Intent(context, CalendarReminderReceiver.class);
        intent.setAction(CalendarReminderReceiver.ACTION_REMIND);
        intent.putExtra(CalendarReminderReceiver.EXTRA_ID, eventId);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, notificationId(eventId), intent, flags);
    }
}
