package com.ringosham.threads.export.song;

import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.ExportSettings;
import com.ringosham.objects.Song;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.List;

public class SongExport extends Task<Void> {
    private MainScreen mainScreen;
    private ExportSettings settings;

    static int failCount = 0;

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
        if (settings.isFilterPractice() || settings.isFilterDuplicates()) {
            updateMessage(Localizer.getLocalizedText("filtering"));
            Platform.runLater(() -> {
                mainScreen.mainProgress.setProgress(-1);
                mainScreen.subProgress.setProgress(0);
            });
            songList = new Filter(songList, settings.isFilterPractice(), settings.isFilterDuplicates(), settings.getFilterSeconds()).run();
        }
        if (settings.isConvertOgg()) {
            int progress = 0;
            int size = songList.size();
            for (Song song : songList) {
                if (song.isOgg())
                    new Converter(mainScreen, song).run();
                progress++;
                int finalProgress = progress;
                Platform.runLater(() -> mainScreen.mainProgress.setProgress((double) finalProgress / size));
            }
        }
        Platform.runLater(() -> mainScreen.subProgress.setProgress(0));
        new Copier(mainScreen, songList, settings.isOverwrite(), settings.isRomajiNaming(), settings.isRenameAsBeatmap()
                , settings.getExportDirectory()).run();
        if (settings.isApplyTags())
            new Tagger(mainScreen, songList, settings.isOverrideTags()).run();
        return null;
    }
}
