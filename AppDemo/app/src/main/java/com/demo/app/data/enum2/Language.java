package com.demo.app.data.enum2;


import com.demo.app.R;

import java.util.Locale;

public enum Language {
    DEFAULT(new Locale(""), R.string.string_language_default),
    ENGLISH(new Locale("en"), R.string.string_language_english),
    GERMAN(new Locale("de"), R.string.string_language_german),
    GREEK(new Locale("el"), R.string.string_language_greek),
    SPANISH(new Locale("es"), R.string.string_language_spanish),
    FRENCH(new Locale("fr"), R.string.string_language_french),
    HINDI(new Locale("hi"), R.string.string_language_hindi),
    HUNGARIAN(new Locale("hu"), R.string.string_language_hungarian),
    ITALIAN(new Locale("it"), R.string.string_language_italian),
    JAPANESE(new Locale("ja"), R.string.string_language_japanese),
    KOREAN(new Locale("ko"), R.string.string_language_korean),
    DUTCH(new Locale("nl"), R.string.string_language_dutch),
    POLISH(new Locale("pl"), R.string.string_language_polish),
    PORTUGUESE(new Locale("pt"), R.string.string_language_portuguese),
    RUSSIAN(new Locale("ru"), R.string.string_language_russian),
    SWEDISH(new Locale("sv"), R.string.string_language_swedish),
    THAI(new Locale("th"), R.string.string_language_thai),
    TURKISH(new Locale("tr"), R.string.string_language_turkish),
    VIETNAMESE(new Locale("vi"), R.string.string_language_vietnamese),
    TRADITIONAL(Locale.TRADITIONAL_CHINESE, R.string.string_language_traditional_chinese),
    CHINESE(Locale.SIMPLIFIED_CHINESE, R.string.string_language_chinese);

    private final Locale locale;
    private final int stringRes;

    Language(Locale locale, int stringRes) {
        this.locale = locale;
        this.stringRes = stringRes;
    }

    public Locale getLocale() {
        return locale;
    }

    public int getStringRes() {
        return stringRes;
    }

}
