/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham;

import com.ringosham.objects.Beatmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class Global {
    public static Global INSTANCE = new Global();
    private final File configFile = Paths.get("./config.cfg").toFile();
    public List<Beatmap> beatmapList = new ArrayList<>();
    public boolean inProgress;
    private Properties config = new Properties();

    private File lazerDirectory;
    private Locale locale;
    private String email;
    private String password;
    private final File convertDir = new File(System.getProperty("java.io.tmpdir") + "/convertOgg");

    private Global() {
    }

    public void loadConfig() throws IOException {
        if (!configFile.exists()) {
            if (!configFile.createNewFile()) {
                throw new IOException("Failed to load config file");
            }
            initConfig();
        }
        FileInputStream in = new FileInputStream(configFile);
        config.load(in);
        //There are no ports of osu!lazer for macOS and Linux.
        //This is planned, but there are currently no support.
        String defaultLazerDir = Defaults.getDefaultDirectory();
        lazerDirectory = new File(config.getProperty("lazerDirectory", defaultLazerDir));
        //Wow, the Locale class is this dumb.
        //It cannot process both en_US and en-US
        locale = Locale.forLanguageTag(config.getProperty("locale", Defaults.locale.toString()).replace("_", "-"));
        in.close();
    }

    private void initConfig() throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        config.setProperty("lazerDirectory", Defaults.getDefaultDirectory());
        config.setProperty("locale", Defaults.locale.toString());
        config.store(out, "Lazer exporter config");
        out.close();
    }

    public void configFailsafe() {
        String defaultLazerDir;
        defaultLazerDir = Defaults.getDefaultDirectory();
        lazerDirectory = new File(defaultLazerDir);
    }

    public void saveConfig() throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        config.setProperty("lazerDirectory", lazerDirectory.getAbsolutePath());
        config.setProperty("locale", locale.toString());
        config.store(out, "Lazer exporter config");
        out.close();
    }

    public String getDatabaseAbsolutePath() {
        return lazerDirectory.getAbsolutePath() + "/client.db";
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public File getLazerDirectory() {
        return lazerDirectory;
    }

    public String getEmail() {
        return email;
    }

    public void setLazerDirectory(File lazerDirectory) {
        this.lazerDirectory = lazerDirectory;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void clearLoginDetails() {
        this.email = null;
        this.password = null;
    }

    public File getConvertDir() {
        return convertDir;
    }

    private static class Defaults {
        private static final String lastExport = "";
        private static final Locale locale = Locale.US;

        private static String getDefaultDirectory() {
            String os = System.getProperty("os.name").toLowerCase();
            String defaultLazerDir;
            if (os.contains("win"))
                defaultLazerDir = System.getenv("AppData").replaceAll("\\\\", "/") + "/osu";
            else if (os.contains("mac"))
                defaultLazerDir = "";
            else
                defaultLazerDir = "";
            return defaultLazerDir;
        }
    }
}
