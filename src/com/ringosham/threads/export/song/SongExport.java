package com.ringosham.threads.export.song;

import com.ringosham.controller.MainScreen;
import com.ringosham.objects.ExportSettings;
import com.ringosham.objects.Song;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

public class SongExport extends Task<Void> {
    private MainScreen mainScreen;
    private ExportSettings settings;

    int failCount = 0;
    int exportCount = 0;

    public SongExport(MainScreen mainScreen, ExportSettings settings) {
        this.mainScreen = mainScreen;
        this.settings = settings;
    }

    @Override
    protected Void call() {
        List<Song> songList = new ArrayList<>();

        return null;
    }
}
