package com.demo.app.kts;

import android.util.Base64;

import androidx.annotation.Nullable;

import com.demo.app.http.HttpResponse;
import com.demo.app.integration.http.GsonUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** 加密响应：前 16 位是 AES 密钥，后面是 Base64 密文。 */
public final class HttpKts {

    private static final int KEY_LENGTH = 16;

    private HttpKts() {
    }

    @Nullable
    public static <T> HttpResponse<T> decode(@Nullable String result, Type type) {
        String json = decryptToJson(result);
        return json == null ? null : GsonUtils.fromJson(json, type);
    }

    @Nullable
    public static <T> T decodeRaw(@Nullable String result, Type type) {
        String json = decryptToJson(result);
        return json == null ? null : GsonUtils.fromJson(json, type);
    }

    @Nullable
    public static String decryptToJson(@Nullable String result) {
        if (result == null || result.length() <= KEY_LENGTH) {
            return null;
        }
        try {
            String key = result.substring(0, KEY_LENGTH);
            byte[] cipherBytes = Base64.decode(result.substring(KEY_LENGTH), Base64.DEFAULT);
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(keyBytes, "AES"),
                    new IvParameterSpec(keyBytes));
            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return null;
        }
    }
}
