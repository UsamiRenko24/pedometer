package com.demo.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.demo.app.data.enum2.Language;
import com.demo.app.integration.http.RxHttp;
import com.demo.app.integration.image.NewsImageLoader;
import com.demo.app.integration.language.MultiLanguages;
import com.demo.app.launcher.workspace.LauncherBusinessDelegateImpl;
import com.demo.app.utils.CacheUtils;
import com.demo.app.utils.CrashUtils;
import com.demo.app.utils.FirebaseUtils;
import com.demo.app.utils.SPUtils;

import m9e778y.LauncherSDK;

import java.util.Locale;
import java.util.Objects;

public class MyApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        try {
            if (base != null) {
                useEnglishUntilLanguageChosen(base);
            }
            super.attachBaseContext((base != null) ? MultiLanguages.attach(base) : null);
        } catch (Throwable ignored) {
            super.attachBaseContext(base);
        }
    }

    /**
     * 还没选过语言时，启动页到语言页都用 English。
     * 设置里选过「默认语言」之后仍跟随系统。
     */
    private static void useEnglishUntilLanguageChosen(Context base) {
        SharedPreferences languagePrefs = base.getSharedPreferences("language_setting", Context.MODE_PRIVATE);
        if (!TextUtils.isEmpty(languagePrefs.getString("key_language", ""))) {
            return;
        }
        SharedPreferences appPrefs = base.getSharedPreferences(
                SPUtils.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if (appPrefs.contains(CacheUtils.CACHE_LANGUAGE_NAME)
                && Language.DEFAULT.name().equals(appPrefs.getString(CacheUtils.CACHE_LANGUAGE_NAME, ""))) {
            return;
        }
        MultiLanguages.setAppLanguage(base, Locale.ENGLISH);
        appPrefs.edit().putString(CacheUtils.CACHE_LANGUAGE_NAME, Language.ENGLISH.name()).apply();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Objects.equals(getProcessName(), getPackageName())) {
            // TODO 正式发布时调用
            //initFirebase();

            //ReferrerUtils.init(this);
            MultiLanguages.init(this);
            RxHttp.init(this);
            NewsImageLoader.install(this);
            initLauncher();
            //Toaster.init(this);
            // Admob 聚合初始化
            //AbAdvertManager.init(this);
        }
    }

    private void initLauncher() {
        LauncherSDK.init(this);
        LauncherSDK.setWorkspaceLayoutVersion(2);
        LauncherSDK.setIconTextSizeScale(1.2f);
        LauncherSDK.setBusinessDelegate(new LauncherBusinessDelegateImpl());
    }

    private void initFirebase() {
        FirebaseUtils.init(this);
    }

    private void initCrash() {
        CrashUtils.init();
    }
}
