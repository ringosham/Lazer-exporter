package com.ringosham.threads.export.song;

import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.ExportSettings;
import com.ringosham.objects.Song;
import javafx.concurrent.Task;

import java.util.List;

public class SongExport extends Task<Void> {
    private MainScreen mainScreen;
    private ExportSettings settings;

    static int failCount = 0;
    int exportCount = 0;

    public SongExport(MainScreen mainScreen, ExportSettings settings) {
        this.mainScreen = mainScreen;
        this.settings = settings;
    }

    @Override
    protected Void call() {
        List<Song> songList;
        updateMessage(Localizer.getLocalizedText("analysing"));
        Analyser analyser = new Analyser(mainScreen);
        songList = analyser.run();
        if (settings.isFilterPractice() || settings.isFilterDuplicates())
            songList = new Filter(mainScreen, songList).run();
        if (settings.isConvertOgg())
            for (Song song : songList)
                if (song.isOgg())
                    new Converter(mainScreen, song).run();

        return null;
    }
}
