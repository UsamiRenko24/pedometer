package com.demo.app.launcher.weather;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/** 解密后的天气，只保留页面和小组件要用的字段。 */
public final class WeatherSnapshot {

    public final String skycon;
    public final int temperature;
    public final int humidityPercent;
    public final int windSpeed;
    public final double windDirection;
    public final long timeMillis;
    @NonNull public final List<Hour> hours;
    @NonNull public final List<Day> days;

    public WeatherSnapshot(String skycon, int temperature, int humidityPercent, int windSpeed,
                           double windDirection, long timeMillis,
                           @NonNull List<Hour> hours, @NonNull List<Day> days) {
        this.skycon = skycon == null ? "" : skycon;
        this.temperature = temperature;
        this.humidityPercent = humidityPercent;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.timeMillis = timeMillis;
        this.hours = hours;
        this.days = days;
    }

    @NonNull
    public static WeatherSnapshot empty() {
        return new WeatherSnapshot("", 0, 0, 0, 0, 0,
                Collections.emptyList(), Collections.emptyList());
    }

    public static final class Hour {
        public final long timeMillis;
        public final String skycon;
        public final int temperature;

        public Hour(long timeMillis, String skycon, int temperature) {
            this.timeMillis = timeMillis;
            this.skycon = skycon == null ? "" : skycon;
            this.temperature = temperature;
        }
    }

    public static final class Day {
        public final long timeMillis;
        public final String skycon;
        public final int low;
        public final int high;

        public Day(long timeMillis, String skycon, int low, int high) {
            this.timeMillis = timeMillis;
            this.skycon = skycon == null ? "" : skycon;
            this.low = low;
            this.high = high;
        }
    }
}
