package com.ringosham.objects;

import java.io.File;

public class Song {
    private String hash;
    private boolean isOgg;
    private boolean isFullVersion;
    private File outputLocation;
    private File fileLocation;
    private long lengthInSeconds;

    public Song(String hash, File fileLocation) {
        this.hash = hash;
        this.fileLocation = fileLocation;
    }

    public String getHash() {
        return hash;
    }

    public boolean isOgg() {
        return isOgg;
    }

    public void setOgg(boolean ogg) {
        isOgg = ogg;
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

    public void setLengthInSeconds(long lengthInSeconds) {
        this.lengthInSeconds = lengthInSeconds;
    }
}
