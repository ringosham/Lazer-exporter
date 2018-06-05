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
        lastExport = new File(config.getProperty("lastExport", ""));
        //There are no ports of osu!lazer for macOS and Linux.
        //This is planned, but there are currently no support.
        String defaultLazerDir = getDefaultDirectory();
        lazerDirectory = new File(config.getProperty("lazerDirectory", defaultLazerDir));
        locale = new Locale(config.getProperty("locale", "en_US"));
        in.close();
    }

    private void initConfig() throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        config.setProperty("lastExport", "");
        config.setProperty("lazerDirectory", getDefaultDirectory());
        config.setProperty("locale", "en_US");
        config.store(out, "Lazer exporter config");
        out.close();
    }

    public void saveConfig() throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        config.setProperty("lastExport", lastExport.getAbsolutePath());
        config.setProperty("lazerDirectory", lazerDirectory.getAbsolutePath());
        config.setProperty("locale", locale.toLanguageTag());
        config.store(out, "Lazer exporter config");
        out.close();
    }

    public void configFailsafe() {
        lastExport = new File("");
        String defaultLazerDir;
        defaultLazerDir = getDefaultDirectory();
        lazerDirectory = new File(defaultLazerDir);
    }

    private String getDefaultDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String defaultLazerDir;
        if (os.contains("win"))
            defaultLazerDir = System.getenv("AppData") + "/osu";
        else if (os.contains("mac"))
            defaultLazerDir = "";
        else
            defaultLazerDir = "";
        return defaultLazerDir;
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

    public void setLazerDirectory(File lazerDirectory) {
        this.lazerDirectory = lazerDirectory;
    }
}
