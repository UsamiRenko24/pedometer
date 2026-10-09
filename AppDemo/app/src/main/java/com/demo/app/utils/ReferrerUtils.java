package com.demo.app.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.demo.app.data.AppConstant;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReferrerUtils {

    public static void init(Context context) {
        // 已经获取过渠道归因时，无需重新请求
        if (!TextUtils.isEmpty(CacheUtils.getReferrerSource())) {
            return;
        }
        try {
            final InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(context).build();
            referrerClient.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    switch (responseCode) {
                        case InstallReferrerClient.InstallReferrerResponse.OK:
                            // Connection established.
                            try {
                                ReferrerDetails referrerDetails = referrerClient.getInstallReferrer();
                                //已安装软件包的引荐来源网址。
                                String installReferrer = referrerDetails.getInstallReferrer();
                                pareReferrer(installReferrer);
                                referrerClient.endConnection();
                            } catch (Throwable ignored) {
                            }
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                            // API not available on the current Play Store app.
                            // 当前Play Store应用程序上没有API。
                            // 默认归因成功
                            CacheUtils.updateReferrerSource(AppConstant.REFERRER_NO_API);
                          //  ReportUtils.reportReferrer(AppConstant.REFERRER_NO_API);
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                            // Connection couldn't be established.
                            //无法建立连接。
                        default:
                            break;
                    }
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                }
            });
        } catch (Throwable ignored) {
        }
    }

    //解析归因数据
    private static void pareReferrer(String referrer) {
        final AtomicReference<String> referrerReference = new AtomicReference<>(referrer);
        try {
            referrerReference.set(urlDecode(referrer));
        } catch (Throwable ignored) {
        }
        String referrerUrl = referrerReference.get();
        if (TextUtils.isEmpty(referrerUrl)) {
            return;
        }
        Map<String, String> paramMap = splitReferrer(referrerUrl);
        String utmSource = paramMap.get(AppConstant.REFERRER_UTM_SOURCE);
        if (TextUtils.isEmpty(utmSource) && referrerUrl.startsWith(AppConstant.REFERRER_GCLID)) {
            utmSource = AppConstant.REFERRER_GCLID;
        }
        if (TextUtils.isEmpty(utmSource)) {
            String unicode = ascii2native(referrerUrl);
            if (!TextUtils.isEmpty(unicode)) {
                Map<String, String> parseMap = splitReferrer(unicode);
                utmSource = parseMap.get(AppConstant.REFERRER_UTM_SOURCE);
            }
        }
        if (TextUtils.isEmpty(utmSource)) {
            utmSource = AppConstant.REFERRER_OTHER;
        }

        CacheUtils.updateReferrerSource(utmSource);
      //  ReportUtils.reportReferrer(referrerUrl);
    }

    private static Map<String, String> splitReferrer(String referrerUrl) {
        final Map<String, String> paramMap = new HashMap<>();
        try {
            String[] paramArr = referrerUrl.split("&");
            if (paramArr.length == 0) {
                return paramMap;
            }
            for (String param : paramArr) {
                String[] itemArr = param.split("=");
                if (itemArr.length < 2) {
                    continue;
                }
                String key = itemArr[0].toLowerCase();
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                String value = itemArr[1].replace("%20", " ");
                paramMap.put(key, value);
            }
        } catch (Throwable ignored) {
        }
        return paramMap;
    }

    /**
     * @noinspection RegExpDuplicateCharacterInClass
     */
    private static String ascii2native(String ascii) {
        String[] newAscii = {ascii};
        try {
            List<String> ascii_s = new ArrayList<>();
            String regex = "\\\\u[0-9,a-f,A-F]{4}";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(newAscii[0]);
            while (m.find()) {
                ascii_s.add(m.group());
            }
            for (int i = 0, j = 2; i < ascii_s.size(); i++) {
                String code = ascii_s.get(i).substring(j, j + 4);
                char ch = (char) Integer.parseInt(code, 16);
                newAscii[0] = newAscii[0].replace(ascii_s.get(i), String.valueOf(ch));
            }
        } catch (Throwable ignored) {
        }
        return newAscii[0];
    }

    /**
     * @noinspection CharsetObjectCanBeUsed
     */
    @Nullable
    private static String urlDecode(final String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        try {
            String safeInput = input.replaceAll("%(?![0-9a-fA-F]{2})", "%25").replaceAll("\\+", "%2B");
            return URLDecoder.decode(safeInput, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        return null;
    }
}