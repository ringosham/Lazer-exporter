/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham;

import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public final class Global {
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
    private File gameExecutable;
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
        //There are no ports of osu!lazer for Linux.
        //This is planned, but there are currently no support.
        String defaultLazerDir = Defaults.getDefaultDirectory();
        lazerDirectory = new File(config.getProperty("lazerDirectory", defaultLazerDir));
        String localeString = config.getProperty("locale", Defaults.locale.toString()).toLowerCase().replace("_", "-");
        locale = Locale.forLanguageTag(localeString);
        videoDownload = Boolean.parseBoolean(config.getProperty("settings.videoDownload", String.valueOf(Defaults.videoDownload)));
        gameExecutable = new File(config.getProperty("settings.gameExecutable", Defaults.getDefaultGameExecutable()));
        in.close();
    }

    private void initConfig() throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        config.setProperty("lazerDirectory", Defaults.getDefaultDirectory());
        config.setProperty("locale", Defaults.locale.toString());
        config.setProperty("settings.videoDownload", String.valueOf(Defaults.videoDownload));
        config.setProperty("settings.gameExecutable", Defaults.getDefaultGameExecutable());
        config.store(out, "Lazer exporter config");
        out.close();
    }

    public void configFailsafe() {
        String defaultLazerDir;
        defaultLazerDir = Defaults.getDefaultDirectory();
        lazerDirectory = new File(defaultLazerDir);
        locale = Defaults.locale;
        gameExecutable = new File(Defaults.getDefaultGameExecutable());
    }

    public void saveConfig() throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        config.setProperty("lazerDirectory", lazerDirectory.getAbsolutePath());
        config.setProperty("locale", locale.toString());
        config.setProperty("settings.videoDownload", String.valueOf(videoDownload));
        config.setProperty("gameExectable", gameExecutable.getAbsolutePath());
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

    public File getGameExecutable() {
        return gameExecutable;
    }

    public void setGameExecutable(File gameExecutable) {
        this.gameExecutable = gameExecutable;
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
                defaultLazerDir = System.getProperty("user.home") + "/osu";
            else
                defaultLazerDir = System.getProperty("user.home") + "/.local/share/osu";
            return defaultLazerDir;
        }

        private static String getDefaultGameExecutable() {
            String os = System.getProperty("os.name").toLowerCase();
            String defaultExecutable;
            if (os.contains("win"))
                defaultExecutable = System.getenv("localappdata").replaceAll("\\\\", "/") + "/osulazer/osu!.exe";
            else if (os.contains("mac"))
                //Lazer will replace the unofficial stable anyway.
                defaultExecutable = "/Applications/osu!.app";
            else
                //Based on Arch Linux AUR
                defaultExecutable = "/usr/bin/osu-lazer";
            return defaultExecutable;
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
        ((Button) (alert.getDialogPane().lookupButton(ButtonType.OK))).setText(Localizer.getLocalizedText("dialog.common.ok"));
        alert.showAndWait();
    }

    //Remove any illegal characters in the file name
    //Many export programs I have seen forgot to eliminate illegal characters from the file name
    public String getValidFileName(String name) {
        return name.replaceAll("\\*", "").replaceAll("<", "").replaceAll(">", "")
                .replaceAll("\\|", "").replaceAll("\\?", "").replaceAll(":", "")
                .replaceAll("\"", "").replaceAll("\\\\", ",").replaceAll("/", ",");
    }

    public void openLink(String url) {
        //AWT only works on Windows and macOS, but not Linux.
        //HostService on JavaFX is bugged in OpenJDK 8, and it's not fixed until Java 9.
        if (System.getProperty("os.name").toLowerCase().equals("linux")) {
            //Why is something as simple as opening an URL so difficult!?
            //If you are using wayland, sorry. There is nothing I can do.
            try {
                Runtime.getRuntime().exec("xdg-open " + url);
            } catch (IOException ignored) {
            }
            return;
        }
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException ignored) {
            }
        }
    }
}
