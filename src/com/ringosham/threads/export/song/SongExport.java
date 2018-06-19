/*
 * Copyright (c) 2018. Ringosham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.export.song;

import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.ExportSettings;
import com.ringosham.objects.Song;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class SongExport extends Task<Void> {
    static int failCount = 0;
    private MainScreen mainScreen;
    private ExportSettings settings;

    public SongExport(MainScreen mainScreen, ExportSettings settings) {
        this.mainScreen = mainScreen;
        this.settings = settings;
    }

    @Override
    protected Void call() {
        List<Song> songList;
        Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("analysing")));
        Analyser analyser = new Analyser(mainScreen);
        songList = analyser.run();
        if (settings.isFilterPractice() || settings.isFilterDuplicates()) {
            Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("filtering")));
            Platform.runLater(() -> {
                mainScreen.mainProgress.setProgress(-1);
                mainScreen.subProgress.setProgress(0);
            });
            songList = new Filter(songList, settings.isFilterPractice(), settings.isFilterDuplicates(), settings.getFilterSeconds()).run();
        }
        Platform.runLater(() -> mainScreen.mainProgress.setProgress(0));
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
        Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("cleanUp")));
        try {
            cleanUp();
        } catch (IOException e) {
            String error = Localizer.getLocalizedText("failCleanUp");
            mainScreen.consoleArea.appendText(error + "\n");
            mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            mainScreen.mainProgress.setProgress(0);
            mainScreen.subProgress.setProgress(0);
            if (failCount == 0)
                mainScreen.statusText.setText(Localizer.getLocalizedText("taskSuccess"));
            else
                mainScreen.statusText.setText(Localizer.getLocalizedText("taskFinishWithFailure")
                        .replace("%FAILCOUNT%", Integer.toString(failCount)));
            mainScreen.enableButtons();
        });
        Global.INSTANCE.inProgress = false;
        return null;
    }

    //Clean up
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void cleanUp() throws IOException {
        if (!Global.INSTANCE.getConvertDir().exists())
            return;
        Files.walk(Global.INSTANCE.getConvertDir().toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
