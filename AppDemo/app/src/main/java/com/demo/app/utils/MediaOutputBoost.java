package com.demo.app.utils;

import android.content.Context;
import android.media.audiofx.LoudnessEnhancer;

/**
 * 把音量控制里超过 100% 的增益加到整机输出，而不只加在试听播放器上。
 */
public final class MediaOutputBoost {

    /** 0 表示主输出混音，效果会作用到当前正在播放的声音。 */
    private static final int OUTPUT_MIX_SESSION = 0;
    private static final int MAX_BOOST_MB = 1000;
    private static final String PREF = "media_output_boost";
    private static final String KEY_PROGRESS = "progress";

    private static LoudnessEnhancer enhancer;

    private MediaOutputBoost() {
    }

    public static int savedProgress(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getInt(KEY_PROGRESS, 0);
    }

    public static void apply(Context context, int progress) {
        int clamped = Math.max(0, Math.min(200, progress));
        context.getApplicationContext()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_PROGRESS, clamped)
                .apply();
        int boostMb = clamped <= 100 ? 0 : Math.round((clamped - 100) / 100f * MAX_BOOST_MB);
        if (boostMb <= 0) {
            release();
            return;
        }
        try {
            if (enhancer == null) {
                enhancer = new LoudnessEnhancer(OUTPUT_MIX_SESSION);
            }
            enhancer.setEnabled(true);
            enhancer.setTargetGain(boostMb);
        } catch (Throwable ignored) {
            release();
        }
    }

    private static void release() {
        try {
            if (enhancer != null) {
                enhancer.release();
            }
        } catch (Throwable ignored) {
        }
        enhancer = null;
    }
}
