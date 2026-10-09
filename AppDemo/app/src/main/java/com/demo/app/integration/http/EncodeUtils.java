package com.demo.app.integration.http;

import android.util.Base64;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;

public final class EncodeUtils {

    private EncodeUtils() {
    }

    public static String base64Encode2String(final byte[] input) {
        if (input == null || input.length == 0) {
            return "";
        }
        return Base64.encodeToString(input, Base64.NO_WRAP);
    }

    public static byte[] decode(@NonNull String input) {
        return decode(input.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decode(byte[] input) {
        if (input == null || input.length == 0) {
            return new byte[0];
        }
        return Base64.decode(input, Base64.NO_WRAP);
    }
}
