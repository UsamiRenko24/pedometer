package com.demo.app.launcher.ui.adapter.weather;

import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.demo.app.R;
import com.demo.app.databinding.ItemWeatherDailyBinding;
import com.demo.app.launcher.ui.activity.weather.WeatherVisuals;
import com.demo.app.launcher.weather.WeatherSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** 首页 10 天预报。 */
public final class WeatherDailyAdapter {

    private WeatherDailyAdapter() {
    }

    public static void bind(@NonNull LinearLayout parent, @NonNull List<WeatherSnapshot.Day> days) {
        parent.removeAllViews();
        if (days.isEmpty()) {
            return;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (WeatherSnapshot.Day day : days) {
            min = Math.min(min, day.low);
            max = Math.max(max, day.high);
        }
        float span = Math.max(1, max - min);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SimpleDateFormat format = new SimpleDateFormat("EEE", Locale.getDefault());
        String today = parent.getContext().getString(R.string.weather_day_today);
        for (WeatherSnapshot.Day day : days) {
            ItemWeatherDailyBinding row = ItemWeatherDailyBinding.inflate(inflater, parent, true);
            row.weatherDayLabel.setText(sameDay(day.timeMillis) ? today : format.format(day.timeMillis));
            row.weatherDayIcon.setImageResource(WeatherVisuals.ofSkycon(day.skycon).icon);
            row.weatherDayLow.setText(day.low + "°");
            row.weatherDayHigh.setText(day.high + "°");
            float start = (day.low - min) / span;
            float end = (day.high - min) / span;
            row.weatherDayBar.setSpan(start, Math.max(start + 0.08f, end));
        }
    }

    private static boolean sameDay(long millis) {
        Calendar now = Calendar.getInstance();
        Calendar other = Calendar.getInstance();
        other.setTimeInMillis(millis);
        return now.get(Calendar.YEAR) == other.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR);
    }
}
