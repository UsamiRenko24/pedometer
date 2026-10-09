package com.demo.app.launcher.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.demo.app.http.DataRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 先用已选城市，没有则走 IP，有定位权限时用最近一次 GPS 覆盖。 */
public final class WeatherRepository {

    public interface WeatherCallback {
        void onResult(@Nullable WeatherLocation location, @Nullable WeatherSnapshot snapshot);
    }

    public interface CityCallback {
        void onResult(boolean success, @NonNull List<WeatherLocation> cities);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private WeatherRepository() {
    }

    public static void load(@NonNull WeatherCallback callback) {
        load(null, callback);
    }

    /**
     * 手动选过的城市保持不动。没有手动城市时，有定位权限就用 GPS，否则用 IP。
     * IP 也失败才是空数据。
     */
    public static void load(@Nullable Context context, @NonNull WeatherCallback callback) {
        Context app = context == null ? null : context.getApplicationContext();
        EXECUTOR.execute(() -> {
            WeatherLocation selected = WeatherStore.selected();
            WeatherLocation location = selected != null && selected.sid > 0 ? selected : null;
            if (location == null && app != null && hasLocation(app)) {
                location = lastGps(app);
                if (location != null) {
                    WeatherStore.select(location);
                }
            }
            if (location == null || !location.hasCoordinates()) {
                location = selected != null && selected.hasCoordinates() ? selected : WeatherClient.ipLocation();
                if (location != null && location.hasCoordinates() && (selected == null || !selected.sameAs(location))) {
                    WeatherStore.select(location);
                }
            }
            if (location == null || !location.hasCoordinates()) {
                post(null, null, callback);
                return;
            }
            WeatherLocation resolved = location;
            DataRepository.getWeatherData(false, resolved, (result, error) ->
                    callback.onResult(resolved, WeatherClient.parseWeatherBody(result)));
        });
    }

    /** 用户允许定位后，用最近一次 GPS 替换当前城市。没有缓存位置时仍走 IP。 */
    public static void locate(@NonNull Context context, @NonNull WeatherCallback callback) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            WeatherLocation location = hasLocation(app) ? lastGps(app) : null;
            if (location == null) {
                location = WeatherClient.ipLocation();
            }
            if (location != null) {
                WeatherStore.select(location);
            }
            if (location == null) {
                post(null, null, callback);
                return;
            }
            WeatherLocation resolved = location;
            DataRepository.getWeatherData(false, resolved, (result, error) ->
                    callback.onResult(resolved, WeatherClient.parseWeatherBody(result)));
        });
    }

    public static void loadCity(@NonNull WeatherLocation location, @NonNull WeatherCallback callback) {
        DataRepository.getWeatherData(false, location, (result, error) ->
                callback.onResult(location, WeatherClient.parseWeatherBody(result)));
    }

    public static void hotCities(@NonNull CityCallback callback) {
        DataRepository.getHotCity((result, error) -> deliverCities(result, callback));
    }

    public static void search(@NonNull String key, @NonNull CityCallback callback) {
        DataRepository.searchCity(key, (result, error) -> deliverCities(result, callback));
    }

    @Nullable
    private static WeatherLocation lastGps(@NonNull Context context) {
        LocationManager manager = context.getSystemService(LocationManager.class);
        if (manager == null) {
            return null;
        }
        Location best = null;
        try {
            for (String provider : manager.getProviders(true)) {
                Location location = manager.getLastKnownLocation(provider);
                if (location != null && (best == null || location.getTime() > best.getTime())) {
                    best = location;
                }
            }
        } catch (SecurityException ignored) {
            return null;
        }
        if (best == null) {
            return null;
        }
        WeatherLocation location = new WeatherLocation();
        location.latitude = String.valueOf(best.getLatitude());
        location.longitude = String.valueOf(best.getLongitude());
        location.location = 1;
        fillAddress(context, best, location);
        return location;
    }

    private static void fillAddress(@NonNull Context context, @NonNull Location gps,
                                    @NonNull WeatherLocation location) {
        if (!Geocoder.isPresent()) {
            return;
        }
        try {
            List<Address> addresses = new Geocoder(context, Locale.US)
                    .getFromLocation(gps.getLatitude(), gps.getLongitude(), 1);
            if (addresses == null || addresses.isEmpty()) {
                return;
            }
            Address address = addresses.get(0);
            location.city = safe(address.getLocality());
            location.state = safe(address.getAdminArea());
            location.country = safe(address.getCountryName());
            location.countryCode = safe(address.getCountryCode());
            location.subLocality = safe(address.getSubLocality());
        } catch (Exception ignored) {
        }
    }

    private static boolean hasLocation(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    private static String safe(@Nullable String value) {
        return value == null ? "" : value;
    }

    private static void post(@Nullable WeatherLocation location,
                             @Nullable WeatherSnapshot snapshot,
                             @NonNull WeatherCallback callback) {
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.post(() -> callback.onResult(location, snapshot));
    }

    private static void deliverCities(@Nullable String raw, @NonNull CityCallback callback) {
        List<WeatherLocation> cities = WeatherClient.parseCityBody(raw);
        if (cities == null) {
            deliver(false, new ArrayList<>(), callback);
        } else {
            deliver(true, cities, callback);
        }
    }

    private static void deliver(boolean success, @NonNull List<WeatherLocation> cities,
                                @NonNull CityCallback callback) {
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.post(() -> callback.onResult(success, cities));
    }
}
