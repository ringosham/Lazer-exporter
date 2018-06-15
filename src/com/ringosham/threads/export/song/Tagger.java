package com.ringosham.threads.export.song;

import com.ringosham.controller.MainScreen;
import com.ringosham.objects.Song;

import java.util.List;

class Tagger {
    private final MainScreen mainScreen;
    private final List<Song> songList;
    private final boolean overrideTags;

    Tagger(MainScreen mainScreen, List<Song> songList, boolean overrideTags) {
        this.mainScreen = mainScreen;
        this.songList = songList;
        this.overrideTags = overrideTags;
    }

    void run() {

    }
}
