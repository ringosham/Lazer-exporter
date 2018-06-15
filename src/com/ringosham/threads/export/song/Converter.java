package com.ringosham.threads.export.song;

import com.ringosham.controller.MainScreen;
import com.ringosham.objects.Song;

class Converter {
    private final MainScreen mainScreen;
    private final Song song;

    Converter(MainScreen mainScreen, Song song) {
        this.mainScreen = mainScreen;
        this.song = song;
    }

    void run() {
    }
}
