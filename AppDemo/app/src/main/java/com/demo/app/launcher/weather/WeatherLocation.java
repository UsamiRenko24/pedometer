package com.demo.app.launcher.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

/** 天气请求里的城市。字段名与接口返回一致。 */
public final class WeatherLocation {

    public String city = "";
    public String country = "";
    public String countryCode = "";
    public String latitude = "";
    public String longitude = "";
    public int sid;
    public String state = "";
    public String subLocality = "";
    /** 0 表示 IP 定位，1 表示 GPS。 */
    public int location;

    @NonNull
    public String displayName() {
        if (!isEmpty(subLocality)) {
            return subLocality;
        }
        if (!isEmpty(city)) {
            return city;
        }
        if (!isEmpty(state)) {
            return state;
        }
        if (!isEmpty(country)) {
            return country;
        }
        return "";
    }

    @NonNull
    public String detailLine() {
        StringBuilder builder = new StringBuilder();
        append(builder, subLocality);
        append(builder, city);
        append(builder, state);
        append(builder, country);
        return builder.toString();
    }

    @NonNull
    public JsonObject toAddressJson() {
        JsonObject object = new JsonObject();
        object.addProperty("state", state);
        object.addProperty("subLocality", subLocality);
        object.addProperty("longitude", isEmpty(longitude) ? "0" : longitude);
        object.addProperty("street", "");
        object.addProperty("countryCode", countryCode);
        object.addProperty("thoroughfare", "");
        object.addProperty("latitude", isEmpty(latitude) ? "0" : latitude);
        object.addProperty("sid", sid);
        object.addProperty("city", city);
        object.addProperty("country", country);
        object.addProperty("name", "");
        object.addProperty("adCode", "0");
        object.addProperty("location", location);
        return object;
    }

    public boolean hasCoordinates() {
        return coordinate(latitude) != null && coordinate(longitude) != null;
    }

    public boolean sameAs(@Nullable WeatherLocation other) {
        if (other == null) {
            return false;
        }
        if (sid > 0 && sid == other.sid) {
            return true;
        }
        return java.util.Objects.equals(latitude, other.latitude)
                && java.util.Objects.equals(longitude, other.longitude)
                && java.util.Objects.equals(city, other.city)
                && java.util.Objects.equals(subLocality, other.subLocality);
    }

    private static void append(@NonNull StringBuilder builder, @Nullable String part) {
        if (isEmpty(part)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(part);
    }

    private static boolean isEmpty(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }

    @Nullable
    private static Double coordinate(@Nullable String value) {
        if (isEmpty(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
