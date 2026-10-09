package com.demo.app.launcher.workspace;

import androidx.annotation.NonNull;

/**
 * 根据 {@link android.os.Build#MANUFACTURER} / {@link android.os.Build#BRAND} 识别设备品牌。
 */
final class OemBrand {

    private static final String SAMSUNG = "samsung";
    private static final String XIAOMI = "xiaomi";
    private static final String REDMI = "redmi";
    private static final String POCO = "poco";
    private static final String OPPO = "oppo";
    private static final String REALME = "realme";
    private static final String VIVO = "vivo";
    private static final String ONEPLUS = "oneplus";
    private static final String HONOR = "honor";
    private static final String HUAWEI = "huawei";
    private static final String MOTO = "moto";
    private static final String MOTOROLA = "motorola";
    private static final String GOOGLE = "google";
    private static final String PIXEL = "pixel";
    private static final String MEIZU = "meizu";
    private static final String ASUS = "asus";
    private static final String SONY = "sony";
    private static final String SONYERICSSON = "sonyericsson";
    private static final String LG = "lg";
    private static final String LGE = "lge";

    private OemBrand() {
    }

    static boolean isSamsung(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, SAMSUNG) || contains(brand, SAMSUNG);
    }

    static boolean isXiaomiFamily(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, XIAOMI) || contains(brand, XIAOMI)
                || contains(brand, REDMI) || contains(brand, POCO);
    }

    static boolean isOppoRealme(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, OPPO) || contains(brand, OPPO)
                || contains(brand, REALME);
    }

    static boolean isVivo(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, VIVO) || contains(brand, VIVO);
    }

    static boolean isOnePlus(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, ONEPLUS) || contains(brand, ONEPLUS);
    }

    static boolean isHonor(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, HONOR) || contains(brand, HONOR);
    }

    static boolean isHuawei(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, HUAWEI) || contains(brand, HUAWEI);
    }

    static boolean isMotorola(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, MOTOROLA) || contains(manufacturer, MOTO)
                || contains(brand, MOTOROLA) || contains(brand, MOTO);
    }

    static boolean isGooglePixel(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, GOOGLE) || contains(brand, GOOGLE)
                || contains(brand, PIXEL);
    }

    static boolean isMeizu(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, MEIZU) || contains(brand, MEIZU);
    }

    static boolean isAsus(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, ASUS) || contains(brand, ASUS);
    }

    static boolean isSony(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, SONY) || contains(manufacturer, SONYERICSSON)
                || contains(brand, SONY);
    }

    static boolean isLG(@NonNull String manufacturer, @NonNull String brand) {
        return contains(manufacturer, LGE) || contains(manufacturer, LG)
                || contains(brand, LG);
    }

    private static boolean contains(@NonNull String value, @NonNull String token) {
        return value.contains(token);
    }
}
