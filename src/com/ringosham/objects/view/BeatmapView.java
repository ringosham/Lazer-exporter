/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.objects.view;

import com.ringosham.objects.xml.BeatmapXML;
import javafx.application.HostServices;
import javafx.beans.property.*;
import javafx.scene.control.Hyperlink;

public class BeatmapView {
    private final BooleanProperty queueProperty = new SimpleBooleanProperty();
    private final BooleanProperty installedProperty = new SimpleBooleanProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty artist = new SimpleStringProperty();
    private final IntegerProperty beatmapIdInt = new SimpleIntegerProperty();
    private final Hyperlink beatmapId;

    public BeatmapView(boolean isInstalled, BeatmapXML xml, HostServices hostServices) {
        this.title.set(xml.getTitle());
        this.artist.set(xml.getArtist());
        this.beatmapId = new Hyperlink(Integer.toString(xml.getBeatmapID()));
        this.installedProperty.set(isInstalled);
        this.beatmapIdInt.set(xml.getBeatmapID());
        beatmapId.setOnAction(e -> hostServices.showDocument("https://osu.ppy.sh/beatmapsets/" + xml.getBeatmapID()));
        queueProperty.set(isInstalled);
    }

    public BooleanProperty getQueueProperty() {
        return queueProperty;
    }

    public StringProperty getTitle() {
        return title;
    }

    public StringProperty getArtist() {
        return artist;
    }

    public Hyperlink getBeatmapId() {
        return beatmapId;
    }

    public BooleanProperty getInstalledProperty() {
        return installedProperty;
    }

    public IntegerProperty getBeatmapIdIntProperty() {
        return beatmapIdInt;
    }
}
