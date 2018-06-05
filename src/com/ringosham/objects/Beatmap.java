package com.ringosham.objects;

import java.io.File;

public class Beatmap {
    private int localId;
    private int beatmapId;
    private String artist;
    private String title;
    private String unicodeArtist;
    private String unicodeTitle;
    private File coverArt;
    private Song songFile;

    public Beatmap(int localId, int beatmapId, String artist, String title, String unicodeArtist, String unicodeTitle, File coverArt, Song songFile) {
        this.localId = localId;
        this.beatmapId = beatmapId;
        this.artist = artist;
        this.title = title;
        this.unicodeArtist = unicodeArtist;
        this.unicodeTitle = unicodeTitle;
        this.coverArt = coverArt;
        this.songFile = songFile;
    }

    public int getLocalId() {
        return localId;
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

    public File getCoverArt() {
        return coverArt;
    }

    public Song getSongFile() {
        return songFile;
    }
}
