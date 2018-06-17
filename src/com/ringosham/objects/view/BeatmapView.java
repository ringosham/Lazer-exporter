package com.ringosham.objects.view;

import com.ringosham.objects.xml.BeatmapXML;
import javafx.application.HostServices;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Hyperlink;

public class BeatmapView {
    private final BooleanProperty booleanProperty = new SimpleBooleanProperty();
    private final boolean isInstalled;
    private final String title;
    private final String artist;
    private final Hyperlink beatmapId;

    public BeatmapView(boolean isInstalled, BeatmapXML xml, HostServices hostServices) {
        this.title = xml.getTitle();
        this.artist = xml.getArtist();
        this.beatmapId = new Hyperlink(Integer.toString(xml.getBeatmapID()));
        beatmapId.setOnAction(e -> hostServices.showDocument("https://osu.ppy.sh/beatmapsets/" + xml.getBeatmapID()));
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

    public Hyperlink getBeatmapId() {
        return beatmapId;
    }
}
