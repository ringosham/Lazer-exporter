/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.objects;

public class Metadata {
    private final String artist;
    private final String title;
    private final String unicodeArtist;
    private final String unicodeTitle;
    private final String audioFilename;
    private final String backgroundFilename;

    public Metadata(String artist, String title, String unicodeArtist, String unicodeTitle, String audioFilename, String backgroundFilename) {
        this.artist = artist;
        this.title = title;
        this.unicodeArtist = unicodeArtist;
        this.unicodeTitle = unicodeTitle;
        this.audioFilename = audioFilename;
        this.backgroundFilename = backgroundFilename;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getUnicodeArtist() {
        return unicodeArtist;
    }

    public String getUnicodeTitle() {
        return unicodeTitle;
    }

    public String getAudioFilename() {
        return audioFilename;
    }

    public String getBackgroundFilename() {
        return backgroundFilename;
    }
}
