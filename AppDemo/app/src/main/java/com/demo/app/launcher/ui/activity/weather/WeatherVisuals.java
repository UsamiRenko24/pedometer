package com.demo.app.launcher.ui.activity.weather;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.demo.app.R;

import java.util.Calendar;

/** 天气图标对应两套背景：xxx_bg 给首页，xxx_widget_bg 给桌面小组件。 */
public final class WeatherVisuals {

    public enum Kind {
        CLEAR(R.drawable.icon_home_app_weather_clear_day,
                R.drawable.icon_home_app_weather_clear_night,
                R.drawable.icon_home_app_weather_clear_day_widget,
                R.drawable.icon_home_app_weather_clear_night_widget,
                R.drawable.icon_home_app_weather_clear_day_bg,
                R.drawable.icon_home_app_weather_clear_night_bg,
                R.drawable.icon_home_app_weather_clear_cloudy_unknown_day_widget_bg,
                R.drawable.icon_home_app_weather_clear_cloudy_unknown_night_widget_bg,
                false),
        CLOUDY(R.drawable.icon_home_app_weather_cloudly_day,
                R.drawable.icon_home_app_weather_cloudly_night,
                R.drawable.icon_home_app_weather_cloudly_day_widget,
                R.drawable.icon_home_app_weather_cloudly_night_widget,
                R.drawable.icon_home_app_weather_cloudy_unknown_day_bg,
                R.drawable.icon_home_app_weather_cloudy_unknown_night_bg,
                R.drawable.icon_home_app_weather_clear_cloudy_unknown_day_widget_bg,
                R.drawable.icon_home_app_weather_clear_cloudy_unknown_night_widget_bg,
                false),
        UNKNOWN(R.drawable.icon_home_app_weather_unknown,
                R.drawable.icon_home_app_weather_unknown,
                R.drawable.icon_home_app_weather_unknown_widget,
                R.drawable.icon_home_app_weather_unknown_widget,
                R.drawable.icon_home_app_weather_cloudy_unknown_day_bg,
                R.drawable.icon_home_app_weather_cloudy_unknown_night_bg,
                R.drawable.icon_home_app_weather_clear_cloudy_unknown_day_widget_bg,
                R.drawable.icon_home_app_weather_clear_cloudy_unknown_night_widget_bg,
                false),
        FOG(R.drawable.icon_home_app_weather_fog,
                R.drawable.icon_home_app_weather_fog,
                R.drawable.icon_home_app_weather_fog_widget,
                R.drawable.icon_home_app_weather_fog_widget,
                R.drawable.icon_home_app_weather_fog_haze_bg,
                R.drawable.icon_home_app_weather_fog_haze_bg,
                R.drawable.icon_home_app_weather_fog_haze_widget_bg,
                R.drawable.icon_home_app_weather_fog_haze_widget_bg,
                true),
        HAZE(R.drawable.icon_home_app_weather_haze,
                R.drawable.icon_home_app_weather_haze,
                R.drawable.icon_home_app_weather_haze_widget,
                R.drawable.icon_home_app_weather_haze_widget,
                R.drawable.icon_home_app_weather_fog_haze_bg,
                R.drawable.icon_home_app_weather_fog_haze_bg,
                R.drawable.icon_home_app_weather_fog_haze_widget_bg,
                R.drawable.icon_home_app_weather_fog_haze_widget_bg,
                true),
        OVERCAST(R.drawable.icon_home_app_weather_overcast_sky,
                R.drawable.icon_home_app_weather_overcast_sky,
                R.drawable.icon_home_app_weather_overcast_sky_widget,
                R.drawable.icon_home_app_weather_overcast_sky_widget,
                R.drawable.icon_home_app_weather_overcast_sky_bg,
                R.drawable.icon_home_app_weather_overcast_sky_bg,
                R.drawable.icon_home_app_weather_overcast_sky_widget_bg,
                R.drawable.icon_home_app_weather_overcast_sky_widget_bg,
                true),
        LIGHT_RAIN(R.drawable.icon_home_app_weather_light_rain,
                R.drawable.icon_home_app_weather_light_rain,
                R.drawable.icon_home_app_weather_light_rain_widget,
                R.drawable.icon_home_app_weather_light_rain_widget,
                R.drawable.icon_home_app_weather_rain_bg,
                R.drawable.icon_home_app_weather_rain_bg,
                R.drawable.icon_home_app_weather_rain_widget_bg,
                R.drawable.icon_home_app_weather_rain_widget_bg,
                true),
        MODERATE_RAIN(R.drawable.icon_home_app_weather_moderate_rain,
                R.drawable.icon_home_app_weather_moderate_rain,
                R.drawable.icon_home_app_weather_moderate_rain_widget,
                R.drawable.icon_home_app_weather_moderate_rain_widget,
                R.drawable.icon_home_app_weather_rain_bg,
                R.drawable.icon_home_app_weather_rain_bg,
                R.drawable.icon_home_app_weather_rain_widget_bg,
                R.drawable.icon_home_app_weather_rain_widget_bg,
                true),
        HEAVY_RAIN(R.drawable.icon_home_app_weather_heavy_rain,
                R.drawable.icon_home_app_weather_heavy_rain,
                R.drawable.icon_home_app_weather_heavy_rain_widget,
                R.drawable.icon_home_app_weather_heavy_rain_widget,
                R.drawable.icon_home_app_weather_rain_bg,
                R.drawable.icon_home_app_weather_rain_bg,
                R.drawable.icon_home_app_weather_rain_widget_bg,
                R.drawable.icon_home_app_weather_rain_widget_bg,
                true),
        FREEZING_RAIN(R.drawable.icon_home_app_weather_freezing_rain,
                R.drawable.icon_home_app_weather_freezing_rain,
                R.drawable.icon_home_app_weather_freezing_rain_widget,
                R.drawable.icon_home_app_weather_freezing_rain_widget,
                R.drawable.icon_home_app_weather_rain_bg,
                R.drawable.icon_home_app_weather_rain_bg,
                R.drawable.icon_home_app_weather_rain_widget_bg,
                R.drawable.icon_home_app_weather_rain_widget_bg,
                true),
        THUNDERSHOWER(R.drawable.icon_home_app_weather_thundershower,
                R.drawable.icon_home_app_weather_thundershower,
                R.drawable.icon_home_app_weather_thundershower_widget,
                R.drawable.icon_home_app_weather_thundershower_widget,
                R.drawable.icon_home_app_weather_rain_bg,
                R.drawable.icon_home_app_weather_rain_bg,
                R.drawable.icon_home_app_weather_thundershower_hailstone_widget_bg,
                R.drawable.icon_home_app_weather_thundershower_hailstone_widget_bg,
                true),
        LIGHT_SNOW(R.drawable.icon_home_app_weather_light_snow,
                R.drawable.icon_home_app_weather_light_snow,
                R.drawable.icon_home_app_weather_light_snow_widget,
                R.drawable.icon_home_app_weather_light_snow_widget,
                R.drawable.icon_home_app_weather_snow_bg,
                R.drawable.icon_home_app_weather_snow_bg,
                R.drawable.icon_home_app_weather_snow_widget_bg,
                R.drawable.icon_home_app_weather_snow_widget_bg,
                true),
        MODERATE_SNOW(R.drawable.icon_home_app_weather_moderate_snow,
                R.drawable.icon_home_app_weather_moderate_snow,
                R.drawable.icon_home_app_weather_moderate_snow_widget,
                R.drawable.icon_home_app_weather_moderate_snow_widget,
                R.drawable.icon_home_app_weather_snow_bg,
                R.drawable.icon_home_app_weather_snow_bg,
                R.drawable.icon_home_app_weather_snow_widget_bg,
                R.drawable.icon_home_app_weather_snow_widget_bg,
                true),
        HEAVY_SNOW(R.drawable.icon_home_app_weather_heavy_snow,
                R.drawable.icon_home_app_weather_heavy_snow,
                R.drawable.icon_home_app_weather_heavy_snow_widget,
                R.drawable.icon_home_app_weather_heavy_snow_widget,
                R.drawable.icon_home_app_weather_snow_bg,
                R.drawable.icon_home_app_weather_snow_bg,
                R.drawable.icon_home_app_weather_snow_widget_bg,
                R.drawable.icon_home_app_weather_snow_widget_bg,
                true),
        SLEET(R.drawable.icon_home_app_weather_sleet,
                R.drawable.icon_home_app_weather_sleet,
                R.drawable.icon_home_app_weather_sleet_widget,
                R.drawable.icon_home_app_weather_sleet_widget,
                R.drawable.icon_home_app_weather_snow_bg,
                R.drawable.icon_home_app_weather_snow_bg,
                R.drawable.icon_home_app_weather_snow_widget_bg,
                R.drawable.icon_home_app_weather_snow_widget_bg,
                true),
        HAIL(R.drawable.icon_home_app_weather_hailstone,
                R.drawable.icon_home_app_weather_hailstone,
                R.drawable.icon_home_app_weather_hailstone_widget,
                R.drawable.icon_home_app_weather_hailstone_widget,
                R.drawable.icon_home_app_weather_hailstone_bg,
                R.drawable.icon_home_app_weather_hailstone_bg,
                R.drawable.icon_home_app_weather_thundershower_hailstone_widget_bg,
                R.drawable.icon_home_app_weather_thundershower_hailstone_widget_bg,
                true),
        WINDY(R.drawable.icon_home_app_weather_windy,
                R.drawable.icon_home_app_weather_windy,
                R.drawable.icon_home_app_weather_windy_widget,
                R.drawable.icon_home_app_weather_windy_widget,
                R.drawable.icon_home_app_weather_wind_bg,
                R.drawable.icon_home_app_weather_wind_bg,
                R.drawable.icon_home_app_weather_wind_widget_bg,
                R.drawable.icon_home_app_weather_wind_widget_bg,
                true),
        TYPHOON(R.drawable.icon_home_app_weather_typhoon,
                R.drawable.icon_home_app_weather_typhoon,
                R.drawable.icon_home_app_weather_typhoon_widget,
                R.drawable.icon_home_app_weather_typhoon_widget,
                R.drawable.icon_home_app_weather_wind_bg,
                R.drawable.icon_home_app_weather_wind_bg,
                R.drawable.icon_home_app_weather_wind_widget_bg,
                R.drawable.icon_home_app_weather_wind_widget_bg,
                true);

        @DrawableRes final int iconDay;
        @DrawableRes final int iconNight;
        @DrawableRes final int widgetIconDay;
        @DrawableRes final int widgetIconNight;
        @DrawableRes final int backgroundDay;
        @DrawableRes final int backgroundNight;
        @DrawableRes final int widgetDay;
        @DrawableRes final int widgetNight;
        final boolean darkWhenDay;

        Kind(int iconDay, int iconNight, int widgetIconDay, int widgetIconNight,
                int backgroundDay, int backgroundNight, int widgetDay, int widgetNight,
                boolean darkWhenDay) {
            this.iconDay = iconDay;
            this.iconNight = iconNight;
            this.widgetIconDay = widgetIconDay;
            this.widgetIconNight = widgetIconNight;
            this.backgroundDay = backgroundDay;
            this.backgroundNight = backgroundNight;
            this.widgetDay = widgetDay;
            this.widgetNight = widgetNight;
            this.darkWhenDay = darkWhenDay;
        }
    }

    @DrawableRes public final int icon;
    @DrawableRes public final int background;
    public final boolean night;
    public final boolean darkBackground;

    private WeatherVisuals(int icon, int background, boolean night, boolean darkBackground) {
        this.icon = icon;
        this.background = background;
        this.night = night;
        this.darkBackground = darkBackground;
    }

    public static WeatherVisuals of(Kind kind, boolean night) {
        return create(kind, night, false);
    }

    /** 桌面小组件使用带 widget 的天气图标，以及圆角的 widget_bg。 */
    public static WeatherVisuals widget(Kind kind, boolean night) {
        return create(kind, night, true);
    }

    private static WeatherVisuals create(Kind kind, boolean night, boolean widget) {
        return new WeatherVisuals(
                widget
                        ? (night ? kind.widgetIconNight : kind.widgetIconDay)
                        : (night ? kind.iconNight : kind.iconDay),
                widget
                        ? (night ? kind.widgetNight : kind.widgetDay)
                        : (night ? kind.backgroundNight : kind.backgroundDay),
                night,
                night || kind.darkWhenDay);
    }

    /** 6:00–18:00 视为白天，其余视为夜间。 */
    public static boolean isNightNow() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour < 6 || hour >= 18;
    }

    public static WeatherVisuals ofSkycon(@Nullable String skycon) {
        return of(kindOf(skycon), nightOf(skycon));
    }

    public static WeatherVisuals widgetSkycon(@Nullable String skycon) {
        return widget(kindOf(skycon), nightOf(skycon));
    }

    @StringRes
    public static int label(Kind kind) {
        switch (kind) {
            case CLEAR:
                return R.string.weather_sky_clear;
            case CLOUDY:
                return R.string.weather_sky_cloudy;
            case FOG:
                return R.string.weather_sky_fog;
            case HAZE:
                return R.string.weather_sky_haze;
            case OVERCAST:
                return R.string.weather_sky_overcast;
            case LIGHT_RAIN:
                return R.string.weather_sky_light_rain;
            case MODERATE_RAIN:
                return R.string.weather_sky_moderate_rain;
            case HEAVY_RAIN:
                return R.string.weather_sky_heavy_rain;
            case FREEZING_RAIN:
                return R.string.weather_sky_freezing_rain;
            case THUNDERSHOWER:
                return R.string.weather_sky_thunder;
            case LIGHT_SNOW:
                return R.string.weather_sky_light_snow;
            case MODERATE_SNOW:
                return R.string.weather_sky_moderate_snow;
            case HEAVY_SNOW:
                return R.string.weather_sky_heavy_snow;
            case SLEET:
                return R.string.weather_sky_sleet;
            case HAIL:
                return R.string.weather_sky_hail;
            case WINDY:
                return R.string.weather_sky_wind;
            case TYPHOON:
                return R.string.weather_sky_typhoon;
            case UNKNOWN:
            default:
                return R.string.weather_sky_unknown;
        }
    }

    public static Kind kindOf(@Nullable String skycon) {
        if (matches(skycon,
                "THUNDERSTORMS", "ISOLATED_THUNDERSTORMS", "SCATTERED_THUNDERSTORMS",
                "PARTLY_SUNNY_W_WITH_T-STORMS", "PARTLY_CLOUDY_W_WITH_T-STORMS",
                "MOSTLY_CLOUDY_W_WITH_T-STORMS")) {
            return Kind.THUNDERSHOWER;
        }
        if (matches(skycon, "HAIL", "ICE")) {
            return Kind.HAIL;
        }
        if (matches(skycon, "TORNADO", "TROPICAL_STORM", "HURRICANE")) {
            return Kind.TYPHOON;
        }
        if (matches(skycon, "WIND", "WINDY", "BREEZY", "BLOWING_SPRAY_WITH_WINDY")) {
            return Kind.WINDY;
        }
        if (matches(skycon, "FREEZING_RAIN")) {
            return Kind.FREEZING_RAIN;
        }
        if (matches(skycon, "SLEET", "RAIN_WITH_SLEET", "MIXED_RAIN_AND_HAIL", "RAIN_AND_SNOW",
                "WINTRY_MIX_SNOW_WITH_SLEET", "RAIN_TO_SNOW_SHOWERS")) {
            return Kind.SLEET;
        }
        if (matches(skycon, "RAIN_H", "HEAVY_RAIN", "THUNDER_AND_HAIL", "STRONG_STORMS")) {
            return Kind.HEAVY_RAIN;
        }
        if (matches(skycon, "RAIN", "RAIN_M", "MODERATE_RAIN", "SHOWERS", "PARTLY_CLOUDY_W_WITH_SHOWERS")) {
            return Kind.MODERATE_RAIN;
        }
        if (matches(skycon, "RAIN_L", "LIGHT_RAIN", "DRIZZLE", "PARTLY_SUNNY_W_WITH_SHOWERS",
                "SCATTERED_SHOWERS", "MOSTLY_CLOUDY_W_WITH_SHOWERS", "FREEZING_DRIZZLE")) {
            return Kind.LIGHT_RAIN;
        }
        if (matches(skycon, "SNOW_H", "BLIZZARD", "HEAVY_SNOW")) {
            return Kind.HEAVY_SNOW;
        }
        if (matches(skycon, "SNOW", "SNOW_M", "BLOWING_WITH_DRIFTING_SNOW", "FRIGID_WITH_ICE_CRYSTALS")) {
            return Kind.MODERATE_SNOW;
        }
        if (matches(skycon, "SNOW_L", "SCATTERED_SNOW_SHOWERS", "LIGHT_SNOW", "FLURRIES",
                "SCATTERED_FLURRIES", "MOSTLY_CLOUDY_W_WITH_FLURRIES", "PARTLY_SUNNY_W_WITH_FLURRIES",
                "MOSTLY_CLOUDY_W_WITH_SNOW")) {
            return Kind.LIGHT_SNOW;
        }
        if (matches(skycon, "FOG", "FOGGY", "HAZY_MOONLIGHT")) {
            return Kind.FOG;
        }
        if (matches(skycon, "HAZE", "BLOWING_DUST_WITH_SANDSTORM", "SMOKE_WITH_WINDY", "HAZE_WITH_WINDY")) {
            return Kind.HAZE;
        }
        if (matches(skycon, "CLOUDY", "MOSTLY_CLOUDY", "DREARY_OVERCAST", "COLD")) {
            return Kind.OVERCAST;
        }
        if (matches(skycon, "PARTLY_CLOUDY", "INTERMITTENT_CLOUDS", "NOT_AVAILABLE", "PARTLY_SUNNY")) {
            return Kind.CLOUDY;
        }
        if (matches(skycon, "CLEAR", "SUNNY", "HOT", "FAIR_WITH_MOSTLY_CLEAR", "MOSTLY_SUNNY",
                "MOSTLY_CLEAR", "FAIR_WITH_MOSTLY_SUNNY", "HAZY_SUNSHINE")) {
            return Kind.CLEAR;
        }
        return Kind.UNKNOWN;
    }

    public static boolean nightOf(@Nullable String skycon) {
        if (skycon != null && skycon.endsWith("_NIGHT")) {
            return true;
        }
        if (skycon != null && skycon.endsWith("_DAY")) {
            return false;
        }
        return isNightNow();
    }

    private static boolean matches(@Nullable String skycon, @NonNull String... codes) {
        if (skycon == null || skycon.isEmpty()) {
            return false;
        }
        String base = skycon;
        if (base.endsWith("_NIGHT")) {
            base = base.substring(0, base.length() - 6);
        } else if (base.endsWith("_DAY")) {
            base = base.substring(0, base.length() - 4);
        }
        for (String code : codes) {
            if (base.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
