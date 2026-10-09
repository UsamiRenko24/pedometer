package com.demo.app.utils;


import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.demo.app.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {

    /**
     * Return whether the string is null or white space.
     *
     * @param s The string.
     * @return {@code true}: yes<br> {@code false}: no
     */
    public static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static SpannableStringBuilder getSpannable(String text) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        // 使用正则表达式来匹配字符串中的数字和小数点部分
        String regex = "\\d+\\.\\d+|\\d+";  // 匹配数字部分以及小数点（例如：123.45 或 123）

        // 用于处理匹配到的数字部分
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            spannable.setSpan(new ForegroundColorSpan(Utils.getApp().getColor(R.color.color_6200ed)), start, end, 0);
        }
        return spannable;
    }

    public static String getString(int ids) {
        return Utils.getApp().getString(ids);
    }
}