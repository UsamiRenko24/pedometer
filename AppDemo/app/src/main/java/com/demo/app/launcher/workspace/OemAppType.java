package com.demo.app.launcher.workspace;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;

/**
 * OEM 系统应用功能类型，枚举声明顺序即默认布局中的优先级（电话 → 计算器）。
 */
enum OemAppType {

    PHONE(
            OemPackages.PHONE_SAMSUNG, OemPackages.PHONE_XIAOMI, OemPackages.PHONE_OPPO_REALME,
            OemPackages.PHONE_VIVO, OemPackages.PHONE_ONEPLUS, OemPackages.PHONE_HONOR,
            OemPackages.PHONE_HUAWEI, OemPackages.PHONE_MOTOROLA, OemPackages.PHONE_GOOGLE_PIXEL,
            OemPackages.PHONE_MEIZU, OemPackages.PHONE_ASUS, OemPackages.PHONE_SONY,
            OemPackages.PHONE_LG, OemPackages.PHONE_COMMON),
    SMS(
            OemPackages.SMS_SAMSUNG, OemPackages.SMS_XIAOMI, OemPackages.SMS_OPPO_REALME,
            OemPackages.SMS_VIVO, OemPackages.SMS_ONEPLUS, OemPackages.SMS_HONOR,
            OemPackages.SMS_HUAWEI, OemPackages.SMS_MOTOROLA, OemPackages.SMS_GOOGLE_PIXEL,
            OemPackages.SMS_MEIZU, OemPackages.SMS_ASUS, OemPackages.SMS_SONY,
            OemPackages.SMS_LG, OemPackages.SMS_COMMON),
    BROWSER(
            OemPackages.BROWSER_SAMSUNG, OemPackages.BROWSER_XIAOMI, OemPackages.BROWSER_OPPO_REALME,
            OemPackages.BROWSER_VIVO, OemPackages.BROWSER_ONEPLUS, OemPackages.BROWSER_HONOR,
            OemPackages.BROWSER_HUAWEI, OemPackages.BROWSER_MOTOROLA, OemPackages.BROWSER_GOOGLE_PIXEL,
            OemPackages.BROWSER_MEIZU, OemPackages.BROWSER_ASUS, OemPackages.BROWSER_SONY,
            OemPackages.BROWSER_LG, OemPackages.BROWSER_COMMON),
    CAMERA(
            OemPackages.CAMERA_SAMSUNG, OemPackages.CAMERA_XIAOMI, OemPackages.CAMERA_OPPO_REALME,
            OemPackages.CAMERA_VIVO, OemPackages.CAMERA_ONEPLUS, OemPackages.CAMERA_HONOR,
            OemPackages.CAMERA_HUAWEI, OemPackages.CAMERA_MOTOROLA, OemPackages.CAMERA_GOOGLE_PIXEL,
            OemPackages.CAMERA_MEIZU, OemPackages.CAMERA_ASUS, OemPackages.CAMERA_SONY,
            OemPackages.CAMERA_LG, OemPackages.CAMERA_COMMON),
    CONTACTS(
            OemPackages.CONTACTS_SAMSUNG, OemPackages.CONTACTS_XIAOMI, OemPackages.CONTACTS_OPPO_REALME,
            OemPackages.CONTACTS_VIVO, OemPackages.CONTACTS_ONEPLUS, OemPackages.CONTACTS_HONOR,
            OemPackages.CONTACTS_HUAWEI, OemPackages.CONTACTS_MOTOROLA, OemPackages.CONTACTS_GOOGLE_PIXEL,
            OemPackages.CONTACTS_MEIZU, OemPackages.CONTACTS_ASUS, OemPackages.CONTACTS_SONY,
            OemPackages.CONTACTS_LG, OemPackages.CONTACTS_COMMON),
    GALLERY(
            OemPackages.GALLERY_SAMSUNG, OemPackages.GALLERY_XIAOMI, OemPackages.GALLERY_OPPO_REALME,
            OemPackages.GALLERY_VIVO, OemPackages.GALLERY_ONEPLUS, OemPackages.GALLERY_HONOR,
            OemPackages.GALLERY_HUAWEI, OemPackages.GALLERY_MOTOROLA, OemPackages.GALLERY_GOOGLE_PIXEL,
            OemPackages.GALLERY_MEIZU, OemPackages.GALLERY_ASUS, OemPackages.GALLERY_SONY,
            OemPackages.GALLERY_LG, OemPackages.GALLERY_COMMON),
    SETTINGS(
            OemPackages.SETTINGS_SAMSUNG, OemPackages.SETTINGS_XIAOMI, OemPackages.SETTINGS_OPPO_REALME,
            OemPackages.SETTINGS_VIVO, OemPackages.SETTINGS_ONEPLUS, OemPackages.SETTINGS_HONOR,
            OemPackages.SETTINGS_HUAWEI, OemPackages.SETTINGS_MOTOROLA, OemPackages.SETTINGS_GOOGLE_PIXEL,
            OemPackages.SETTINGS_MEIZU, OemPackages.SETTINGS_ASUS, OemPackages.SETTINGS_SONY,
            OemPackages.SETTINGS_LG, OemPackages.SETTINGS_COMMON),
    FILE_MANAGER(
            OemPackages.FILE_MANAGER_SAMSUNG, OemPackages.FILE_MANAGER_XIAOMI,
            OemPackages.FILE_MANAGER_OPPO_REALME, OemPackages.FILE_MANAGER_VIVO,
            OemPackages.FILE_MANAGER_ONEPLUS, OemPackages.FILE_MANAGER_HONOR,
            OemPackages.FILE_MANAGER_HUAWEI, OemPackages.FILE_MANAGER_MOTOROLA,
            OemPackages.FILE_MANAGER_GOOGLE_PIXEL, OemPackages.FILE_MANAGER_MEIZU,
            OemPackages.FILE_MANAGER_ASUS, OemPackages.FILE_MANAGER_SONY,
            OemPackages.FILE_MANAGER_LG, OemPackages.FILE_MANAGER_COMMON),
    CLOCK(
            OemPackages.CLOCK_SAMSUNG, OemPackages.CLOCK_XIAOMI, OemPackages.CLOCK_OPPO_REALME,
            OemPackages.CLOCK_VIVO, OemPackages.CLOCK_ONEPLUS, OemPackages.CLOCK_HONOR,
            OemPackages.CLOCK_HUAWEI, OemPackages.CLOCK_MOTOROLA, OemPackages.CLOCK_GOOGLE_PIXEL,
            OemPackages.CLOCK_MEIZU, OemPackages.CLOCK_ASUS, OemPackages.CLOCK_SONY,
            OemPackages.CLOCK_LG, OemPackages.CLOCK_COMMON),
    CALENDAR(
            OemPackages.CALENDAR_SAMSUNG, OemPackages.CALENDAR_XIAOMI, OemPackages.CALENDAR_OPPO_REALME,
            OemPackages.CALENDAR_VIVO, OemPackages.CALENDAR_ONEPLUS, OemPackages.CALENDAR_HONOR,
            OemPackages.CALENDAR_HUAWEI, OemPackages.CALENDAR_MOTOROLA, OemPackages.CALENDAR_GOOGLE_PIXEL,
            OemPackages.CALENDAR_MEIZU, OemPackages.CALENDAR_ASUS, OemPackages.CALENDAR_SONY,
            OemPackages.CALENDAR_LG, OemPackages.CALENDAR_COMMON),
    CALCULATOR(
            OemPackages.CALCULATOR_SAMSUNG, OemPackages.CALCULATOR_XIAOMI,
            OemPackages.CALCULATOR_OPPO_REALME, OemPackages.CALCULATOR_VIVO,
            OemPackages.CALCULATOR_ONEPLUS, OemPackages.CALCULATOR_HONOR,
            OemPackages.CALCULATOR_HUAWEI, OemPackages.CALCULATOR_MOTOROLA,
            OemPackages.CALCULATOR_GOOGLE_PIXEL, OemPackages.CALCULATOR_MEIZU,
            OemPackages.CALCULATOR_ASUS, OemPackages.CALCULATOR_SONY,
            OemPackages.CALCULATOR_LG, OemPackages.CALCULATOR_COMMON);

    @NonNull
    private final String[] samsung;
    @NonNull
    private final String[] xiaomi;
    @NonNull
    private final String[] oppoRealme;
    @NonNull
    private final String[] vivo;
    @NonNull
    private final String[] onePlus;
    @NonNull
    private final String[] honor;
    @NonNull
    private final String[] huawei;
    @NonNull
    private final String[] motorola;
    @NonNull
    private final String[] googlePixel;
    @NonNull
    private final String[] meizu;
    @NonNull
    private final String[] asus;
    @NonNull
    private final String[] sony;
    @NonNull
    private final String[] lg;
    @NonNull
    private final String[] common;

    OemAppType(
            @NonNull String[] samsung,
            @NonNull String[] xiaomi,
            @NonNull String[] oppoRealme,
            @NonNull String[] vivo,
            @NonNull String[] onePlus,
            @NonNull String[] honor,
            @NonNull String[] huawei,
            @NonNull String[] motorola,
            @NonNull String[] googlePixel,
            @NonNull String[] meizu,
            @NonNull String[] asus,
            @NonNull String[] sony,
            @NonNull String[] lg,
            @NonNull String[] common) {
        this.samsung = samsung;
        this.xiaomi = xiaomi;
        this.oppoRealme = oppoRealme;
        this.vivo = vivo;
        this.onePlus = onePlus;
        this.honor = honor;
        this.huawei = huawei;
        this.motorola = motorola;
        this.googlePixel = googlePixel;
        this.meizu = meizu;
        this.asus = asus;
        this.sony = sony;
        this.lg = lg;
        this.common = common;
    }

    @NonNull
    String[] resolvePackages(@NonNull String manufacturer, @NonNull String brand) {
        return merge(brandList(manufacturer, brand), common);
    }

    @NonNull
    String shortcutId() {
        return "workspace.shortcut.oem." + name().toLowerCase(Locale.US);
    }

    @NonNull
    private String[] brandList(@NonNull String manufacturer, @NonNull String brand) {
        if (OemBrand.isSamsung(manufacturer, brand)) return samsung;
        if (OemBrand.isXiaomiFamily(manufacturer, brand)) return xiaomi;
        if (OemBrand.isOppoRealme(manufacturer, brand)) return oppoRealme;
        if (OemBrand.isVivo(manufacturer, brand)) return vivo;
        if (OemBrand.isOnePlus(manufacturer, brand)) return onePlus;
        if (OemBrand.isHonor(manufacturer, brand)) return honor;
        if (OemBrand.isHuawei(manufacturer, brand)) return huawei;
        if (OemBrand.isMotorola(manufacturer, brand)) return motorola;
        if (OemBrand.isGooglePixel(manufacturer, brand)) return googlePixel;
        if (OemBrand.isMeizu(manufacturer, brand)) return meizu;
        if (OemBrand.isAsus(manufacturer, brand)) return asus;
        if (OemBrand.isSony(manufacturer, brand)) return sony;
        if (OemBrand.isLG(manufacturer, brand)) return lg;
        return new String[0];
    }

    @NonNull
    private static String[] merge(@NonNull String[] brandPackages, @NonNull String[] commonPackages) {
        if (brandPackages.length == 0) {
            return commonPackages;
        }
        LinkedHashSet<String> merged = new LinkedHashSet<>(brandPackages.length + commonPackages.length);
        Collections.addAll(merged, brandPackages);
        Collections.addAll(merged, commonPackages);
        return merged.toArray(new String[0]);
    }
}
