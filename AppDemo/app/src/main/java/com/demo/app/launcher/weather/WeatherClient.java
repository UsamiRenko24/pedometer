package com.demo.app.launcher.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.integration.http.RxHttp;
import com.demo.app.kts.HttpKts;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;

/** IP 定位，以及把天气、城市接口的原文解析成页面数据。 */
final class WeatherClient {

    private static final String IP = "https://apip.cc/json";
    private static final Gson GSON = new Gson();

    private WeatherClient() {
    }

    @Nullable
    static WeatherLocation ipLocation() {
        Request request = new Request.Builder().url(IP).get().build();
        try (Response response = RxHttp.getOrCreateClient().newCall(request).execute()) {
            if (response.body() == null || !response.isSuccessful()) {
                return null;
            }
            JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();
            if (!"success".equalsIgnoreCase(text(object, "status"))) {
                return null;
            }
            WeatherLocation location = new WeatherLocation();
            location.city = first(object, "City", "city");
            location.subLocality = location.city;
            location.state = first(object, "RegionName", "regionName");
            location.country = first(object, "CountryName", "countryName");
            location.countryCode = first(object, "CountryCode", "countryCode");
            location.latitude = first(object, "Latitude", "latitude");
            location.longitude = first(object, "Longitude", "longitude");
            location.location = 0;
            location.sid = -1;
            return location.hasCoordinates() ? location : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    static WeatherSnapshot parseWeatherBody(@Nullable String raw) {
        JsonElement data = dataOf(raw);
        if (data == null || !data.isJsonObject()) {
            return null;
        }
        return parseWeather(data.getAsJsonObject());
    }

    @Nullable
    static List<WeatherLocation> parseCityBody(@Nullable String raw) {
        JsonElement data = dataOf(raw);
        return data == null ? null : parseCities(data);
    }

    @Nullable
    private static JsonElement dataOf(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        String json = raw.startsWith("{") ? raw : HttpKts.decryptToJson(raw);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            JsonObject envelope = JsonParser.parseString(json).getAsJsonObject();
            if (code(envelope) != 0) {
                return null;
            }
            return envelope.get("data");
        } catch (Exception ignored) {
            return null;
        }
    }

    @NonNull
    private static List<WeatherLocation> parseCities(@Nullable JsonElement data) {
        if (data == null) {
            return Collections.emptyList();
        }
        JsonArray array = null;
        if (data.isJsonArray()) {
            array = data.getAsJsonArray();
        } else if (data.isJsonObject()) {
            JsonObject object = data.getAsJsonObject();
            if (object.has("list") && object.get("list").isJsonArray()) {
                array = object.getAsJsonArray("list");
            }
        }
        if (array == null) {
            return Collections.emptyList();
        }
        List<WeatherLocation> cities = new ArrayList<>();
        for (JsonElement item : array) {
            if (!item.isJsonObject()) {
                continue;
            }
            WeatherLocation location = GSON.fromJson(item, WeatherLocation.class);
            if (location != null && !location.displayName().isEmpty()) {
                cities.add(location);
            }
        }
        return cities;
    }

    @Nullable
    private static WeatherSnapshot parseWeather(@NonNull JsonObject data) {
        JsonObject current = data.has("cur") && data.get("cur").isJsonObject()
                ? data.getAsJsonObject("cur") : new JsonObject();
        double humidity = number(current, "humidity");
        int humidityPercent = humidity <= 1 ? (int) Math.round(humidity * 100) : (int) Math.round(humidity);
        List<WeatherSnapshot.Hour> hours = new ArrayList<>();
        if (data.has("hourly") && data.get("hourly").isJsonObject()) {
            JsonObject hourly = data.getAsJsonObject("hourly");
            if (hourly.has("list") && hourly.get("list").isJsonArray()) {
                for (JsonElement item : hourly.getAsJsonArray("list")) {
                    if (hours.size() >= 24) {
                        break;
                    }
                    if (!item.isJsonObject()) {
                        continue;
                    }
                    JsonObject hour = item.getAsJsonObject();
                    hours.add(new WeatherSnapshot.Hour(
                            epoch(number(hour, "time")),
                            text(hour, "skycon"),
                            (int) Math.round(number(hour, "temperature"))));
                }
            }
        }
        List<WeatherSnapshot.Day> days = new ArrayList<>();
        if (data.has("daily") && data.get("daily").isJsonArray()) {
            for (JsonElement item : data.getAsJsonArray("daily")) {
                if (days.size() >= 10) {
                    break;
                }
                if (!item.isJsonObject()) {
                    continue;
                }
                JsonObject day = item.getAsJsonObject();
                days.add(new WeatherSnapshot.Day(
                        epoch(number(day, "time")),
                        text(day, "skycon"),
                        (int) Math.round(number(day, "ltemp")),
                        (int) Math.round(number(day, "htemp"))));
            }
        }
        return new WeatherSnapshot(
                text(current, "skycon"),
                (int) Math.round(number(current, "temperature")),
                humidityPercent,
                (int) Math.round(number(current, "windSpeed")),
                number(current, "windDirection"),
                epoch(number(data, "timestamp")),
                hours,
                days);
    }

    private static long epoch(double value) {
        long raw = (long) value;
        if (raw <= 0) {
            return System.currentTimeMillis();
        }
        return raw < 10_000_000_000L ? raw * 1000L : raw;
    }

    @NonNull
    private static String first(@NonNull JsonObject object, @NonNull String... keys) {
        for (String key : keys) {
            String value = text(object, key);
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private static int code(@NonNull JsonObject envelope) {
        if (!envelope.has("code") || envelope.get("code").isJsonNull()) {
            return -1;
        }
        try {
            return envelope.get("code").getAsInt();
        } catch (Exception ignored) {
            return -1;
        }
    }

    @NonNull
    private static String text(@NonNull JsonObject object, @NonNull String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }
        JsonElement element = object.get(key);
        return element.isJsonPrimitive() ? element.getAsString() : "";
    }

    private static double number(@NonNull JsonObject object, @NonNull String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return 0;
        }
        try {
            return object.get(key).getAsDouble();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
