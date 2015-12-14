package com.activels.als.diyappmanager.utils;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Created by arvin.li on 2015/11/18.
 */
public class SystemUtil {

    /**
     * 设置应用语言
     *
     * @param context
     * @param language
     */
    public static void updateLanguage(Context context, String language) {

        Configuration config = context.getResources().getConfiguration();

        if (language != null) {
            if ("zh_TW".equals(language)) {
                config.locale = Locale.TAIWAN;
            } else if ("zh_CN".equals(language)) {
                config.locale = Locale.SIMPLIFIED_CHINESE;
            } else if ("en_US".equals(language)) {
                config.locale = Locale.TRADITIONAL_CHINESE;
            }
        } else {
            config.locale = Locale.getDefault();
        }

        context.getResources().updateConfiguration(config, null);
    }
}
