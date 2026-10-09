package com.demo.app.launcher.workspace;

/**
 * 各品牌及通用的系统应用包名映射，按 {@link OemAppType} 功能分类。
 */
final class OemPackages {

    static final String[] PHONE_SAMSUNG = {"com.samsung.android.dialer"};
    static final String[] PHONE_XIAOMI = {"com.android.contacts"};
    static final String[] PHONE_OPPO_REALME = {"com.coloros.dialer", "com.oppo.dialer"};
    static final String[] PHONE_VIVO = {"com.vivo.dialer"};
    static final String[] PHONE_ONEPLUS = {"com.oneplus.dialer", "com.oplus.dialer"};
    static final String[] PHONE_HONOR = {"com.hihonor.contacts"};
    static final String[] PHONE_HUAWEI = {"com.huawei.contacts"};
    static final String[] PHONE_MOTOROLA = {"com.motorola.dialer"};
    static final String[] PHONE_GOOGLE_PIXEL = {"com.google.android.dialer"};
    static final String[] PHONE_MEIZU = {"com.meizu.dialer"};
    static final String[] PHONE_ASUS = {"com.asus.dialer"};
    static final String[] PHONE_SONY = {"com.sonyericsson.android.socialphonebook"};
    static final String[] PHONE_LG = {"com.lge.dialer"};
    static final String[] PHONE_COMMON = {
            "com.google.android.dialer", "com.android.dialer",
            "com.android.contacts"};

    static final String[] SMS_SAMSUNG = {
            "com.samsung.android.messaging", "com.samsung.android.message"};
    static final String[] SMS_XIAOMI = {"com.android.mms"};
    static final String[] SMS_OPPO_REALME = {"com.coloros.messaging", "com.oppo.mms"};
    static final String[] SMS_VIVO = {"com.vivo.messaging"};
    static final String[] SMS_ONEPLUS = {"com.oneplus.mms", "com.oplus.mms"};
    static final String[] SMS_HONOR = {"com.hihonor.mms"};
    static final String[] SMS_HUAWEI = {"com.huawei.message"};
    static final String[] SMS_MOTOROLA = {"com.motorola.messaging"};
    static final String[] SMS_GOOGLE_PIXEL = {"com.google.android.apps.messaging"};
    static final String[] SMS_MEIZU = {"com.meizu.mms"};
    static final String[] SMS_ASUS = {"com.asus.message"};
    static final String[] SMS_SONY = {"com.sonyericsson.conversations"};
    static final String[] SMS_LG = {"com.lge.message"};
    static final String[] SMS_COMMON = {
            "com.google.android.apps.messaging", "com.android.mms", "com.android.messaging"};

    static final String[] CAMERA_SAMSUNG = {
            "com.samsung.android.camera", "com.sec.android.app.camera"};
    static final String[] CAMERA_XIAOMI = {"com.miui.camera", "com.xiaomi.camera"};
    static final String[] CAMERA_OPPO_REALME = {
            "com.oppo.camera", "com.coloros.camera", "com.heytap.camera"};
    static final String[] CAMERA_VIVO = {"com.vivo.camera"};
    static final String[] CAMERA_ONEPLUS = {"com.oneplus.camera", "com.oplus.camera"};
    static final String[] CAMERA_HONOR = {"com.hihonor.camera"};
    static final String[] CAMERA_HUAWEI = {"com.huawei.camera", "com.huawei.camera2"};
    static final String[] CAMERA_MOTOROLA = {
            "com.motorola.camera", "com.motorola.camera3", "com.motorola.camera2"};
    static final String[] CAMERA_GOOGLE_PIXEL = {
            "com.google.android.GoogleCamera", "com.google.android.GoogleCameraGo"};
    static final String[] CAMERA_MEIZU = {"com.meizu.media.camera"};
    static final String[] CAMERA_ASUS = {"com.asus.camera"};
    static final String[] CAMERA_SONY = {"com.sonyericsson.android.camera"};
    static final String[] CAMERA_LG = {"com.lge.camera"};
    static final String[] CAMERA_COMMON = {
            "com.google.android.GoogleCamera", "com.android.camera",
            "com.android.camera2", "org.codeaurora.snapcam"};

    static final String[] BROWSER_SAMSUNG = {
            "com.android.chrome",
            "com.samsung.android.app.sbrowser", "com.sec.android.app.sbrowser"};
    static final String[] BROWSER_XIAOMI = {
            "com.android.chrome", "com.miui.browser", "com.mi.globalbrowser"};
    static final String[] BROWSER_OPPO_REALME = {
            "com.android.chrome",
            "com.oppo.browser", "com.coloros.browser", "com.heytap.browser"};
    static final String[] BROWSER_ONEPLUS = {
            "com.android.chrome", "com.oneplus.browser", "com.oplus.browser"};
    static final String[] BROWSER_VIVO = {"com.android.chrome", "com.vivo.browser"};
    static final String[] BROWSER_HONOR = {"com.android.chrome", "com.hihonor.browser"};
    static final String[] BROWSER_HUAWEI = {"com.android.chrome", "com.huawei.browser"};
    static final String[] BROWSER_MOTOROLA = {"com.android.chrome", "com.motorola.browser"};
    static final String[] BROWSER_GOOGLE_PIXEL = {"com.android.chrome"};
    static final String[] BROWSER_MEIZU = {"com.android.chrome", "com.meizu.browser"};
    static final String[] BROWSER_ASUS = {"com.android.chrome", "com.asus.browser"};
    static final String[] BROWSER_SONY = {"com.android.chrome", "com.sonymobile.browser"};
    static final String[] BROWSER_LG = {"com.android.chrome", "com.lge.browser"};
    static final String[] BROWSER_COMMON = {
            "com.android.chrome", "com.android.browser", "org.chromium.chrome"};

    static final String[] CONTACTS_SAMSUNG = {
            "com.samsung.android.contacts", "com.samsung.android.app.contacts"};
    static final String[] CONTACTS_XIAOMI = {"com.android.contacts"};
    static final String[] CONTACTS_OPPO_REALME = {
            "com.oppo.contacts", "com.coloros.contacts", "com.heytap.contacts"};
    static final String[] CONTACTS_VIVO = {"com.vivo.contacts"};
    static final String[] CONTACTS_ONEPLUS = {"com.oneplus.contacts", "com.oplus.contacts"};
    static final String[] CONTACTS_HONOR = {"com.hihonor.contacts"};
    static final String[] CONTACTS_HUAWEI = {"com.huawei.contacts"};
    static final String[] CONTACTS_MOTOROLA = {"com.motorola.contacts"};
    static final String[] CONTACTS_GOOGLE_PIXEL = {"com.google.android.contacts"};
    static final String[] CONTACTS_MEIZU = {"com.meizu.contacts"};
    static final String[] CONTACTS_ASUS = {"com.asus.contacts"};
    static final String[] CONTACTS_SONY = {"com.sonyericsson.android.socialphonebook"};
    static final String[] CONTACTS_LG = {"com.lge.contacts"};
    static final String[] CONTACTS_COMMON = {
            "com.google.android.contacts", "com.android.contacts"};

    static final String[] GALLERY_SAMSUNG = {
            "com.samsung.android.gallery", "com.sec.android.gallery3d"};
    static final String[] GALLERY_XIAOMI = {"com.miui.gallery", "com.xiaomi.gallery"};
    static final String[] GALLERY_OPPO_REALME = {
            "com.oppo.gallery3d", "com.coloros.gallery3d", "com.heytap.gallery"};
    static final String[] GALLERY_VIVO = {"com.vivo.gallery", "com.bbk.gallery"};
    static final String[] GALLERY_ONEPLUS = {"com.oneplus.gallery", "com.oplus.gallery"};
    static final String[] GALLERY_HONOR = {"com.hihonor.photos", "com.hihonor.gallery"};
    static final String[] GALLERY_HUAWEI = {"com.huawei.photos", "com.huawei.gallery"};
    static final String[] GALLERY_MOTOROLA = {"com.motorola.gallery"};
    static final String[] GALLERY_GOOGLE_PIXEL = {"com.google.android.apps.photos"};
    static final String[] GALLERY_MEIZU = {"com.meizu.media.gallery"};
    static final String[] GALLERY_ASUS = {"com.asus.gallery"};
    static final String[] GALLERY_SONY = {"com.sonyericsson.album"};
    static final String[] GALLERY_LG = {"com.lge.gallery"};
    static final String[] GALLERY_COMMON = {
            "com.google.android.apps.photos", "com.android.gallery3d"};

    static final String[] SETTINGS_SAMSUNG = {};
    static final String[] SETTINGS_XIAOMI = {"com.xiaomi.misettings"};
    static final String[] SETTINGS_OPPO_REALME = {"com.oppo.settings", "com.coloros.settings"};
    static final String[] SETTINGS_VIVO = {"com.vivo.settings"};
    static final String[] SETTINGS_ONEPLUS = {"com.oneplus.settings", "com.oplus.settings"};
    static final String[] SETTINGS_HONOR = {"com.hihonor.settings"};
    static final String[] SETTINGS_HUAWEI = {};
    static final String[] SETTINGS_MOTOROLA = {"com.motorola.settings"};
    static final String[] SETTINGS_GOOGLE_PIXEL = {"com.android.settings"};
    static final String[] SETTINGS_MEIZU = {"com.meizu.settings"};
    static final String[] SETTINGS_ASUS = {"com.asus.settings"};
    static final String[] SETTINGS_SONY = {};
    static final String[] SETTINGS_LG = {};
    static final String[] SETTINGS_COMMON = {"com.android.settings"};

    static final String[] FILE_MANAGER_SAMSUNG = {
            "com.samsung.android.myfiles", "com.sec.android.app.myfiles"};
    static final String[] FILE_MANAGER_XIAOMI = {
            "com.android.fileexplorer", "com.mi.android.globalFileexplorer"};
    static final String[] FILE_MANAGER_OPPO_REALME = {
            "com.oppo.filemanager", "com.coloros.filemanager", "com.heytap.filemanager"};
    static final String[] FILE_MANAGER_VIVO = {"com.vivo.filemanager", "com.bbk.filemanager"};
    static final String[] FILE_MANAGER_ONEPLUS = {
            "com.oneplus.filemanager", "com.oplus.filemanager"};
    static final String[] FILE_MANAGER_HONOR = {"com.hihonor.filemanager"};
    static final String[] FILE_MANAGER_HUAWEI = {"com.huawei.filemanager", "com.huawei.hidisk"};
    static final String[] FILE_MANAGER_MOTOROLA = {"com.motorola.filemanager"};
    static final String[] FILE_MANAGER_GOOGLE_PIXEL = {"com.google.android.apps.nbu.files"};
    static final String[] FILE_MANAGER_MEIZU = {"com.meizu.filemanager"};
    static final String[] FILE_MANAGER_ASUS = {"com.asus.filemanager"};
    static final String[] FILE_MANAGER_SONY = {"com.sonymobile.filemanager"};
    static final String[] FILE_MANAGER_LG = {"com.lge.filemanager"};
    static final String[] FILE_MANAGER_COMMON = {
            "com.google.android.apps.nbu.files", "com.android.documentsui"};

    static final String[] CLOCK_SAMSUNG = {"com.sec.android.app.clockpackage"};
    static final String[] CLOCK_XIAOMI = {};
    static final String[] CLOCK_OPPO_REALME = {"com.coloros.alarmclock", "com.heytap.clock"};
    static final String[] CLOCK_VIVO = {"com.bbk.alarmclock"};
    static final String[] CLOCK_ONEPLUS = {"com.oneplus.deskclock", "com.oplus.alarmclock"};
    static final String[] CLOCK_HONOR = {"com.hihonor.deskclock"};
    static final String[] CLOCK_HUAWEI = {"com.huawei.deskclock"};
    static final String[] CLOCK_MOTOROLA = {};
    static final String[] CLOCK_GOOGLE_PIXEL = {"com.google.android.deskclock"};
    static final String[] CLOCK_MEIZU = {"com.meizu.flyme.clock"};
    static final String[] CLOCK_ASUS = {"com.asus.deskclock"};
    static final String[] CLOCK_SONY = {"com.sonyericsson.organizer"};
    static final String[] CLOCK_LG = {"com.lge.clock"};
    static final String[] CLOCK_COMMON = {"com.google.android.deskclock", "com.android.deskclock"};

    static final String[] CALENDAR_SAMSUNG = {"com.samsung.android.calendar"};
    static final String[] CALENDAR_XIAOMI = {"com.android.calendar"};
    static final String[] CALENDAR_OPPO_REALME = {"com.coloros.calendar", "com.heytap.calendar"};
    static final String[] CALENDAR_VIVO = {"com.bbk.calendar"};
    static final String[] CALENDAR_ONEPLUS = {"com.oneplus.calendar", "com.oplus.calendar"};
    static final String[] CALENDAR_HONOR = {"com.hihonor.calendar"};
    static final String[] CALENDAR_HUAWEI = {"com.huawei.calendar"};
    static final String[] CALENDAR_MOTOROLA = {"com.motorola.calendar"};
    static final String[] CALENDAR_GOOGLE_PIXEL = {"com.google.android.calendar"};
    static final String[] CALENDAR_MEIZU = {"com.meizu.flyme.calendar"};
    static final String[] CALENDAR_ASUS = {"com.asus.calendar"};
    static final String[] CALENDAR_SONY = {"com.sonyericsson.calendar"};
    static final String[] CALENDAR_LG = {"com.lge.calendar"};
    static final String[] CALENDAR_COMMON = {
            "com.google.android.calendar", "com.android.calendar"};

    static final String[] CALCULATOR_SAMSUNG = {"com.sec.android.app.popupcalculator"};
    static final String[] CALCULATOR_XIAOMI = {"com.miui.calculator"};
    static final String[] CALCULATOR_OPPO_REALME = {"com.coloros.calculator", "com.heytap.calculator"};
    static final String[] CALCULATOR_VIVO = {"com.bbk.calculator"};
    static final String[] CALCULATOR_ONEPLUS = {"com.oneplus.calculator", "com.oplus.calculator"};
    static final String[] CALCULATOR_HONOR = {"com.hihonor.calculator"};
    static final String[] CALCULATOR_HUAWEI = {"com.huawei.calculator"};
    static final String[] CALCULATOR_MOTOROLA = {"com.motorola.calculator"};
    static final String[] CALCULATOR_GOOGLE_PIXEL = {"com.google.android.calculator"};
    static final String[] CALCULATOR_MEIZU = {"com.meizu.flyme.calculator"};
    static final String[] CALCULATOR_ASUS = {"com.asus.calculator"};
    static final String[] CALCULATOR_SONY = {"com.sonyericsson.calculator"};
    static final String[] CALCULATOR_LG = {"com.lge.calculator"};
    static final String[] CALCULATOR_COMMON = {
            "com.google.android.calculator", "com.android.calculator2"};

    private OemPackages() {
    }
}
