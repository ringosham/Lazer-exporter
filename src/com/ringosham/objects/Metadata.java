package com.ringosham.objects;

public class Metadata {
    private String artist;
    private String title;
    private String unicodeArtist;
    private String unicodeTitle;

    public Metadata(String artist, String title, String unicodeArtist, String unicodeTitle) {
        this.artist = artist;
        this.title = title;
        this.unicodeArtist = unicodeArtist;
        this.unicodeTitle = unicodeTitle;
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
}
