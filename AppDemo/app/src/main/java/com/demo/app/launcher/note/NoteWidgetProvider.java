package com.demo.app.launcher.note;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.demo.app.R;
import com.demo.app.launcher.ui.activity.note.NoteEditActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 桌面记事本小组件。点进去编辑绑定的那一条笔记。 */
public class NoteWidgetProvider extends AppWidgetProvider {

    public static void refresh(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
        int[] ids = manager.getAppWidgetIds(new ComponentName(appContext, NoteWidgetProvider.class));
        for (int id : ids) {
            manager.updateAppWidget(id, build(appContext, id));
        }
    }

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager manager, @NonNull int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            NoteWidgetStore.bindIfNew(id);
            manager.updateAppWidget(id, build(context, id));
        }
    }

    @Override
    public void onDeleted(@NonNull Context context, @NonNull int[] appWidgetIds) {
        NoteWidgetStore.remove(appWidgetIds);
    }

    @NonNull
    private static RemoteViews build(@NonNull Context context, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_note);
        NoteEntry note = NoteStore.find(NoteWidgetStore.noteIdOf(widgetId));
        int textColor = NoteStyles.resolvedTextColor(context, note == null ? 0 : note.textColor);
        views.setImageViewResource(
                R.id.note_widget_bg,
                NoteStyles.backgroundRes(note == null ? 0 : note.bgIndex));
        if (note == null) {
            views.setTextViewText(R.id.note_widget_title, context.getString(R.string.note_widget_empty));
            views.setTextViewText(R.id.note_widget_body, "");
            views.setTextViewText(R.id.note_widget_date, "");
        } else {
            views.setTextViewText(R.id.note_widget_title, note.title);
            views.setTextViewText(R.id.note_widget_body, note.body);
            views.setTextViewText(
                    R.id.note_widget_date,
                    new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                            .format(new Date(note.updatedMillis)));
        }
        views.setTextColor(R.id.note_widget_title, textColor);
        views.setTextColor(R.id.note_widget_body, textColor);
        views.setTextColor(R.id.note_widget_date, textColor);

        Intent intent = new Intent(context, NoteEditActivity.class);
        intent.putExtra(NoteEditActivity.EXTRA_ID, note == null ? 0L : note.id);
        intent.setData(Uri.parse("note://widget/" + widgetId));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.note_widget_root, pending);
        return views;
    }
}
