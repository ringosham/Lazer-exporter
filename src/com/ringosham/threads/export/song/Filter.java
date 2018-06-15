package com.ringosham.threads.export.song;

import com.ringosham.controller.MainScreen;
import com.ringosham.objects.Song;

import java.util.List;

public class Filter {
    private final MainScreen mainScreen;
    private final List<Song> songList;

    public Filter(MainScreen mainScreen, List<Song> songList) {
        this.mainScreen = mainScreen;
        this.songList = songList;
    }

    List<Song> run() {
        return null;
    }
}
