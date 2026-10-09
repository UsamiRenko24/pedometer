package com.demo.app.utils;

import android.content.Context;
import android.content.SharedPreferences;


public final class SPUtils {
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    private static SPUtils spUtils;
    public static final String SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME";

    private SPUtils() {
        // Private constructor to prevent instantiation
    }

    public static synchronized SPUtils getInstance() {
        if (spUtils == null) {
            sp = Utils.getApp().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            editor = sp.edit();
            spUtils = new SPUtils();
        }
        return spUtils;
    }

    public void put(String key, String value) {
        editor.putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public void put(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    // 保存 boolean 类型
    public void put(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    // 获取 boolean 类型
    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    // 保存 float 类型
    public void put(String key, float value) {
        editor.putFloat(key, value).apply();
    }

    // 获取 float 类型
    public float getFloat(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    // 保存 long 类型
    public void put(String key, long value) {
        editor.putLong(key, value).apply();
    }

    // 获取 long 类型
    public long getLong(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    // 移除某个 key
    public void remove(String key) {
        editor.remove(key).apply();
    }

    // 清除所有数据
    public void clear() {
        editor.clear().apply();
    }

    // 判断某个 key 是否存在
    public boolean contains(String key) {
        return sp.contains(key);
    }
}