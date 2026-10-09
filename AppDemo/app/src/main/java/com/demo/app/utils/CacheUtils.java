package com.demo.app.utils;


import android.text.TextUtils;

import com.demo.app.data.AppConstant;
import com.demo.app.data.enum2.Language;

import java.util.Objects;

public final class CacheUtils {
    private static final String REPORT_FIRST_SHOW_SPLASH = "REPORT_FIRST_SHOW_SPLASH";
    private static final String REPORT_FIRST_LANGUAGE_DONE_USE = "REPORT_FIRST_LANGUAGE_DONE_USE";
    private static final String REPORT_FIRST_SPLASH_ADVERT = "REPORT_FIRST_SPLASH_ADVERT";
    private static final String REPORT_FIRST_LANGUAGE_SHOW = "REPORT_FIRST_LANGUAGE_SHOW";
    public static final String CACHE_LANGUAGE_NAME = "CACHE_LANGUAGE_NAME";
    private static final String CACHE_SHOW_GUIDE = "CACHE_SHOW_GUIDE";
    private static final String CACHE_SHOW_GESTURE = "CACHE_SHOW_GESTURE";
    private static final String CACHE_GESTURE_STEP = "CACHE_GESTURE_STEP";
    private static final String CACHE_GUIDE_PAGE = "CACHE_GUIDE_PAGE";
    /** 第一页正在申请默认桌面。授权后回来要进通知页，不能再停在欢迎页。 */
    private static final String CACHE_GUIDE_CONTINUE_NOTIFY = "CACHE_GUIDE_CONTINUE_NOTIFY";
    public static final String CACHE_REFERRER_SOURCE = "CACHE_REFERRER_SOURCE";
    public static final String CACHE_FIRST_SELECT_LANGUAGE = "CACHE_FIRST_SELECT_LANGUAGE";

    /**
     * 当前语种
     */
    public static String getCurLanguageName() {
        return SPUtils.getInstance().getString(CACHE_LANGUAGE_NAME, Language.DEFAULT.name());
    }

    public static void setCurLanguageName(String value) {
        SPUtils.getInstance().put(CACHE_LANGUAGE_NAME, value);
    }


    /**
     * @return 是否首次展示开屏页上报
     */
    public static boolean isFirstSplashPageReport() {
        final boolean canReport = SPUtils.getInstance().getBoolean(REPORT_FIRST_SHOW_SPLASH, true);
        if (canReport) {
            SPUtils.getInstance().put(REPORT_FIRST_SHOW_SPLASH, false);
        }
        return canReport;
    }




    /**
     * @return 是否首次语言页点击完成按钮
     */
    public static boolean isFirstLanguageDoneUse() {
        final boolean isFirst = SPUtils.getInstance().getBoolean(REPORT_FIRST_LANGUAGE_DONE_USE, true);
        if (isFirst) {
            updateFirstLanguageDoneUse(false);
        }
        return isFirst;
    }

    public static void updateFirstLanguageDoneUse(boolean value) {
        SPUtils.getInstance().put(REPORT_FIRST_LANGUAGE_DONE_USE, value);
    }

    public static boolean isFirstLanguageShow() {
        return SPUtils.getInstance().getBoolean(REPORT_FIRST_LANGUAGE_SHOW, true);
    }


    public static void updateFirstLanguageShow(boolean value) {
        SPUtils.getInstance().put(REPORT_FIRST_LANGUAGE_SHOW, value);
    }

    public static boolean isFirstSplashAdvert() {
        final boolean isFirst = SPUtils.getInstance().getBoolean(REPORT_FIRST_SPLASH_ADVERT, true);
        if (isFirst) {
            SPUtils.getInstance().put(REPORT_FIRST_SPLASH_ADVERT, false);
        }
        return isFirst;
    }
    public static void setFirstSelectLanguage(boolean value) {
        SPUtils.getInstance().put(CACHE_FIRST_SELECT_LANGUAGE, value);
    }

    /**
     * 是否展示引导页
     */
    public static boolean isShowGuidePage() {
        return SPUtils.getInstance().getBoolean(CACHE_SHOW_GUIDE, true);
    }

    public static void setShowGuidePage(boolean value) {
        SPUtils.getInstance().put(CACHE_SHOW_GUIDE, value);
    }

    public static boolean isShowGestureGuide() {
        return SPUtils.getInstance().getBoolean(CACHE_SHOW_GESTURE, true);
    }

    public static void setShowGestureGuide(boolean value) {
        SPUtils.getInstance().put(CACHE_SHOW_GESTURE, value);
    }

    /** 0 屏手势引导停在哪一步：0 右滑，1 左滑，2 上滑。 */
    public static int getGestureGuideStep() {
        if (!isShowGestureGuide()) {
            return 3;
        }
        return SPUtils.getInstance().getInt(CACHE_GESTURE_STEP, 0);
    }

    public static void setGestureGuideStep(int step) {
        SPUtils.getInstance().put(CACHE_GESTURE_STEP, step);
    }

    /** 引导当前停在哪一页。设默认桌面后进程被系统拉起时用来续上。 */
    public static int getGuidePage() {
        return SPUtils.getInstance().getInt(CACHE_GUIDE_PAGE, 0);
    }

    public static void setGuidePage(int page) {
        SPUtils.getInstance().put(CACHE_GUIDE_PAGE, page);
    }

    public static boolean isGuideContinueNotify() {
        return SPUtils.getInstance().getBoolean(CACHE_GUIDE_CONTINUE_NOTIFY, false);
    }

    public static void setGuideContinueNotify(boolean value) {
        SPUtils.getInstance().put(CACHE_GUIDE_CONTINUE_NOTIFY, value);
    }

    // 是否为其他归因渠道 买量
    public static boolean isOtherSource() {
        final String referrerSource = getReferrerSource();
        if (Objects.equals(referrerSource, AppConstant.REFERRER_ALL)) {
            return true;
        }
        final boolean isOther = !(TextUtils.isEmpty(referrerSource) || Objects.equals(referrerSource, AppConstant.REFERRER_GOOGLE_PLAY));
        return isOther;
    }
    // 获取归因渠道
    public static String getReferrerSource() {
        return SPUtils.getInstance().getString(CACHE_REFERRER_SOURCE, "");
    }
    // 更新归因渠道
    public static void updateReferrerSource(String referrerSource) {
        SPUtils.getInstance().put(CACHE_REFERRER_SOURCE, referrerSource);
    }
}