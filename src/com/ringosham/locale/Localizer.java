package com.ringosham.locale;

import com.ringosham.objects.Global;

import java.util.ResourceBundle;

public class Localizer {
    private static final String RESOURCE_BUNDLE = "com.ringosham.lang.lang";

    public static String getLocalizedText(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, Global.INSTANCE.getLocale(), ClassLoader.getSystemClassLoader());
        try {
            if (bundle.keySet().contains(key))
                return bundle.getString(key);
            else
                return "[No message - " + Global.INSTANCE.getLocale().toLanguageTag() + " ]";
        } catch (Exception e) {
            e.printStackTrace();
            return "[Error]";
        }
    }
}
