package com.ringosham.objects;

import com.ringosham.objects.xml.BeatmapXML;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BeatmapView {
    private final BooleanProperty booleanProperty = new SimpleBooleanProperty();
    private final boolean isInstalled;
    private final String title;
    private final String artist;
    private final int beatmapId;

    public BeatmapView(boolean isInstalled, BeatmapXML xml) {
        this.title = xml.getTitle();
        this.artist = xml.getArtist();
        this.beatmapId = xml.getBeatmapID();
        this.isInstalled = isInstalled;
        booleanProperty.set(isInstalled);
    }

    public BooleanProperty getInstalledProperty() {
        return booleanProperty;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public int getBeatmapId() {
        return beatmapId;
    }
}
