package com.ringosham.objects;

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
    private Properties config = new Properties();

    private File lastExport;
    private File lazerDirectory;
    private Locale locale;
    private String username;
    private String password;

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
        lastExport = new File(config.getProperty("lastExport", Defaults.lastExport));
        //There are no ports of osu!lazer for macOS and Linux.
        //This is planned, but there are currently no support.
        String defaultLazerDir = Defaults.getDefaultDirectory();
        lazerDirectory = new File(config.getProperty("lazerDirectory", defaultLazerDir));
        locale = new Locale(config.getProperty("locale", Defaults.locale.toString()));
        in.close();
    }

    private void initConfig() throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        config.setProperty("lastExport", Defaults.lastExport);
        config.setProperty("lazerDirectory", Defaults.getDefaultDirectory());
        config.setProperty("locale", Defaults.locale.toString());
        config.store(out, "Lazer exporter config");
        out.close();
    }

    public void configFailsafe() {
        lastExport = new File("");
        String defaultLazerDir;
        defaultLazerDir = Defaults.getDefaultDirectory();
        lazerDirectory = new File(defaultLazerDir);
    }

    public void saveConfig() throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        config.setProperty("lastExport", lastExport.getAbsolutePath());
        config.setProperty("lazerDirectory", lazerDirectory.getAbsolutePath());
        config.setProperty("locale", locale.toLanguageTag());
        config.store(out, "Lazer exporter config");
        out.close();
    }

    public String getDatabaseAbsolutePath() {
        return lazerDirectory.getAbsolutePath().replaceAll("\\\\", "/") + "/client.db";
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public File getLastExport() {
        return lastExport;
    }

    public void setLastExport(File lastExport) {
        this.lastExport = lastExport;
    }

    public File getLazerDirectory() {
        return lazerDirectory;
    }

    public String getUsername() {
        return username;
    }

    public void setLazerDirectory(File lazerDirectory) {
        this.lazerDirectory = lazerDirectory;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
