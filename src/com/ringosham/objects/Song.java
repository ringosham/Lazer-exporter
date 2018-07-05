/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.objects;

import java.io.File;

public class Song {
    private int beatmapID;
    private boolean isOgg;
    private boolean isFullVersion;
    private File outputLocation;
    private File fileLocation;
    private long lengthInSeconds;
    private long bitrate;

    public Song(File fileLocation, int beatmapID, boolean isOgg, long lengthInSeconds, long bitrate) {
        this.fileLocation = fileLocation;
        this.beatmapID = beatmapID;
        this.isOgg = isOgg;
        this.lengthInSeconds = lengthInSeconds;
        this.bitrate = bitrate;
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

    public void setFileLocation(File fileLocation) {
        this.fileLocation = fileLocation;
    }

    public long getLengthInSeconds() {
        return lengthInSeconds;
    }

    public int getBeatmapID() {
        return beatmapID;
    }

    public long getBitrate() {
        return bitrate;
    }

    public void setOgg(boolean ogg) {
        isOgg = ogg;
    }
}
