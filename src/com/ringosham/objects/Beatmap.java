package com.ringosham.objects;

import java.util.HashMap;

public class Beatmap {
    private int beatmapId;
    private String artist;
    private String title;
    private String unicodeArtist;
    private String unicodeTitle;
    private HashMap<String, String> fileMap;

    public Beatmap(int beatmapId, String artist, String title, String unicodeArtist, String unicodeTitle, HashMap<String, String> fileMap) {
        this.beatmapId = beatmapId;
        this.artist = artist;
        this.title = title;
        this.unicodeArtist = unicodeArtist;
        this.unicodeTitle = unicodeTitle;
        this.fileMap = fileMap;
    }

    public int getBeatmapId() {
        return beatmapId;
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

    public HashMap<String, String> getFileMap() {
        return fileMap;
    }
}
