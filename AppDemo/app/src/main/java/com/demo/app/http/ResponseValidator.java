package com.demo.app.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.kts.HttpKts;
import com.demo.app.launcher.gamecenter.model.GameCenterResponse;
import com.demo.app.launcher.gamecenter.model.GameCenterUiState;
import com.demo.app.launcher.gamecenter.model.GameDetailResponse;
import com.demo.app.launcher.gamecenter.model.GameItemUi;
import com.demo.app.launcher.gamecenter.model.GameSectionUi;
import com.demo.app.launcher.gamecenter.parser.GameCenterParser;
import com.demo.app.launcher.gamecenter.parser.GameDetailParser;
import com.demo.app.launcher.wallpaper.model.CategoriesDataDto;
import com.demo.app.launcher.wallpaper.model.CategoryDto;
import com.demo.app.launcher.wallpaper.model.CategoryUi;
import com.demo.app.launcher.wallpaper.model.WallpaperApiResponse;
import com.demo.app.launcher.wallpaper.model.WallpaperDetailDto;
import com.demo.app.launcher.wallpaper.model.WallpaperDetailUi;
import com.demo.app.launcher.wallpaper.model.WallpaperItemUi;
import com.demo.app.launcher.wallpaper.model.WallpaperListDataDto;
import com.demo.app.launcher.wallpaper.model.WallpaperListUi;
import com.demo.app.launcher.wallpaper.parser.WallpaperParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

/** 写入缓存前确认响应成功。明文看 code，加密响应先解密再看 code。 */
public final class ResponseValidator {

    private ResponseValidator() {
    }

    public static boolean isSuccessResponse(@Nullable String response, int successCode) {
        return getResponseCode(response) == successCode;
    }

    public static int getResponseCode(@Nullable String response) {
        if (TextUtils.isEmpty(response)) {
            return -1;
        }
        try {
            return new JSONObject(response).optInt("code", -1);
        } catch (Exception ignored) {
            return -1;
        }
    }

    @NonNull
    public static String getResponseMessage(@Nullable String response) {
        if (TextUtils.isEmpty(response)) {
            return "";
        }
        try {
            JSONObject json = new JSONObject(response);
            return json.optString("msg", json.optString("message", ""));
        } catch (Exception ignored) {
            return "";
        }
    }

    public static boolean isSuccessEncryptedResponse(@Nullable String encryptedResponse, int successCode) {
        if (TextUtils.isEmpty(encryptedResponse)) {
            return false;
        }
        try {
            Type type = new TypeToken<HttpResponse<Object>>() {
            }.getType();
            HttpResponse<Object> response = HttpKts.decode(encryptedResponse, type);
            return response != null && response.getCode() == successCode;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean shouldCacheGameCenterHome(@Nullable String encryptedResponse) {
        if (TextUtils.isEmpty(encryptedResponse)) {
            return false;
        }
        try {
            GameCenterResponse response = HttpKts.decodeRaw(encryptedResponse, GameCenterResponse.class);
            if (response == null) {
                return false;
            }
            GameCenterUiState state = GameCenterParser.INSTANCE.parse(response);
            if (state.isEmpty() || TextUtils.isEmpty(state.getAssetBaseUrl())) {
                return false;
            }
            for (GameSectionUi section : state.getSections()) {
                for (GameItemUi item : section.getItems()) {
                    if (!TextUtils.isEmpty(item.getBid())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean shouldCacheGameCenterDetail(@Nullable String encryptedResponse) {
        if (TextUtils.isEmpty(encryptedResponse)) {
            return false;
        }
        try {
            GameDetailResponse response = HttpKts.decodeRaw(encryptedResponse, GameDetailResponse.class);
            return response != null && GameDetailParser.INSTANCE.parse(response, "") != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean shouldCacheWallpaperCategories(@Nullable String encryptedResponse) {
        if (TextUtils.isEmpty(encryptedResponse)) {
            return false;
        }
        try {
            Type listType = new TypeToken<WallpaperApiResponse<List<CategoryDto>>>() {
            }.getType();
            WallpaperApiResponse<List<CategoryDto>> listResponse = HttpKts.decodeRaw(encryptedResponse, listType);
            if (listResponse != null && listResponse.getData() != null && !listResponse.getData().isEmpty()) {
                List<CategoryUi> categories = WallpaperParser.INSTANCE.parseCategoriesList(listResponse);
                return categories != null && !categories.isEmpty();
            }
            Type wrappedType = new TypeToken<WallpaperApiResponse<CategoriesDataDto>>() {
            }.getType();
            WallpaperApiResponse<CategoriesDataDto> response = HttpKts.decodeRaw(encryptedResponse, wrappedType);
            if (response == null || !isWallpaperSuccessCode(response.getCode(), response.getData() != null)) {
                return false;
            }
            List<CategoryUi> categories = WallpaperParser.INSTANCE.parseCategories(response);
            return categories != null && !categories.isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean shouldCacheWallpaperList(@Nullable String encryptedResponse) {
        if (TextUtils.isEmpty(encryptedResponse)) {
            return false;
        }
        try {
            Type type = new TypeToken<WallpaperApiResponse<WallpaperListDataDto>>() {
            }.getType();
            WallpaperApiResponse<WallpaperListDataDto> response = HttpKts.decodeRaw(encryptedResponse, type);
            if (response == null || response.getData() == null) {
                return false;
            }
            if (!isWallpaperSuccessCode(response.getCode(), true)) {
                return false;
            }
            WallpaperListUi listUi = WallpaperParser.INSTANCE.parseWallpaperList(response, "");
            if (listUi.getItems() == null) {
                return false;
            }
            for (WallpaperItemUi item : listUi.getItems()) {
                if (!TextUtils.isEmpty(item.getId())) {
                    return true;
                }
            }
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean shouldCacheWallpaperDetail(@Nullable String encryptedResponse) {
        if (TextUtils.isEmpty(encryptedResponse)) {
            return false;
        }
        try {
            Type type = new TypeToken<WallpaperApiResponse<WallpaperDetailDto>>() {
            }.getType();
            WallpaperApiResponse<WallpaperDetailDto> response = HttpKts.decodeRaw(encryptedResponse, type);
            if (response == null || response.getData() == null) {
                return false;
            }
            if (!isWallpaperSuccessCode(response.getCode(), true)) {
                return false;
            }
            WallpaperDetailUi detail = WallpaperParser.INSTANCE.parseDetail(response, "");
            return detail != null
                    && (!TextUtils.isEmpty(detail.getImageUrl())
                    || !TextUtils.isEmpty(detail.getThumbUrl()));
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isWallpaperSuccessCode(int code, boolean hasData) {
        return code == 0 || (code == -1 && hasData);
    }
}
