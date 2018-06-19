/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.locale;

import com.ringosham.Global;

import java.util.ResourceBundle;

public class Localizer {
    private static final String RESOURCE_BUNDLE = "com.ringosham.lang.lang";

    public static String getLocalizedText(String key) {
        ResourceBundle bundle = getResourceBundle();
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

    public static ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle(RESOURCE_BUNDLE, Global.INSTANCE.getLocale(), ClassLoader.getSystemClassLoader());
    }
}
