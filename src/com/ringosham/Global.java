/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham;

import com.ringosham.objects.Beatmap;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class Global {
    public static final Global INSTANCE = new Global();
    private File configFile;
    public final List<Beatmap> beatmapList = new ArrayList<>();
    public boolean inProgress;
    private final Properties config = new Properties();

    private File lazerDirectory;
    private Locale locale;
    private boolean videoDownload;
    private String email;
    private String password;
    private final File convertDir = new File(System.getProperty("java.io.tmpdir") + "/convertOgg");
    private static final List<Image> appIcon = new ArrayList<Image>() {{
        add(new Image(Global.class.getResourceAsStream("assets/logo16x16.png")));
        add(new Image(Global.class.getResourceAsStream("assets/logo32x32.png")));
        add(new Image(Global.class.getResourceAsStream("assets/logo64x64.png")));
    }};

    private Global() {
        try {
            File jar = new File(Global.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            configFile = new File(jar.getParentFile(), "config.cfg");
        } catch (URISyntaxException ignored) {
        }
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
        String localeString = config.getProperty("locale", Defaults.locale.toString()).toLowerCase().replace("_", "-");
        locale = Locale.forLanguageTag(localeString);
        videoDownload = Boolean.parseBoolean(config.getProperty("videoDownload", String.valueOf(Defaults.videoDownload)));
        in.close();
    }

    private void initConfig() throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        config.setProperty("lazerDirectory", Defaults.getDefaultDirectory());
        config.setProperty("locale", Defaults.locale.toString());
        config.setProperty("videoDownload", String.valueOf(Defaults.videoDownload));
        config.store(out, "Lazer exporter config");
        out.close();
    }

    public void configFailsafe() {
        String defaultLazerDir;
        defaultLazerDir = Defaults.getDefaultDirectory();
        lazerDirectory = new File(defaultLazerDir);
        locale = Defaults.locale;
    }

    public void saveConfig() throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        config.setProperty("lazerDirectory", lazerDirectory.getAbsolutePath());
        config.setProperty("locale", locale.toString());
        config.setProperty("videoDownload", String.valueOf(videoDownload));
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

    public List<Image> getAppIcon() {
        return appIcon;
    }

    public boolean isVideoDownload() {
        return videoDownload;
    }

    public void setVideoDownload(boolean videoDownload) {
        this.videoDownload = videoDownload;
    }

    private static class Defaults {
        private static final Locale locale = Locale.US;
        private static final boolean videoDownload = true;

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

    public void showAlert(Alert.AlertType alertType, String title, String content) {
        showAlert(alertType, title, title, content);
    }

    public void showAlert(Alert.AlertType alertType, String title, String headerText, String content) {
        if (!Platform.isFxApplicationThread())
            Platform.runLater(() -> invokeAlertBox(alertType, title, headerText, content));
        else
            invokeAlertBox(alertType, title, headerText, content);
    }

    private void invokeAlertBox(Alert.AlertType alertType, String title, String headerText, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
