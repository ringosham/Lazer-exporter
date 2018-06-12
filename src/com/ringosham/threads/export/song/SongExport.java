package com.ringosham.threads.export.song;

import com.ringosham.controller.MainScreen;
import com.ringosham.objects.ExportSettings;
import javafx.concurrent.Task;

public class SongExport extends Task<Void> {
    private MainScreen mainScreen;
    private ExportSettings settings;

    public SongExport(MainScreen mainScreen, ExportSettings settings) {
        this.mainScreen = mainScreen;
        this.settings = settings;
    }

    @Override
    protected Void call() {
        return null;
    }
}
