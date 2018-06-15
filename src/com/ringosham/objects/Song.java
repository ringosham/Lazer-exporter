package com.ringosham.objects;

import java.io.File;

public class Song {
    private int beatmapID;
    private boolean isOgg;
    private boolean isFullVersion;
    private File outputLocation;
    private File fileLocation;
    private long lengthInSeconds;

    public Song(File fileLocation, int beatmapID, boolean isOgg, long lengthInSeconds) {
        this.fileLocation = fileLocation;
        this.beatmapID = beatmapID;
        this.isOgg = isOgg;
        this.lengthInSeconds = lengthInSeconds;
    }

    public boolean isOgg() {
        return isOgg;
    }

    public boolean isFullVersion() {
        return isFullVersion;
    }

    public void setFullVersion(boolean fullVersion) {
        isFullVersion = fullVersion;
    }

    public File getOutputLocation() {
        return outputLocation;
    }

    public void setOutputLocation(File outputLocation) {
        this.outputLocation = outputLocation;
    }

    public File getFileLocation() {
        return fileLocation;
    }

    public long getLengthInSeconds() {
        return lengthInSeconds;
    }

    public int getBeatmapID() {
        return beatmapID;
    }
}
