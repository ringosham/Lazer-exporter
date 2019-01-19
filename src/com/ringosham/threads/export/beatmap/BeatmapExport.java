/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.export.beatmap;

import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BeatmapExport extends Task<Void> {
    private final MainScreen mainScreen;
    private final File exportDir;
    private int failCount = 0;

    public BeatmapExport(MainScreen mainScreen, File exportDir) {
        this.mainScreen = mainScreen;
        this.exportDir = exportDir;
    }

    @Override
    protected Void call() {
        updateProgress(0, Global.INSTANCE.beatmapList.size());
        int progress = 0;
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            updateMessage(Localizer.getLocalizedText("status.beatmap.exportingMap").replace("%BEATMAP%", beatmap.getBeatmapFullname()));
            File outputFile = new File(exportDir, Global.INSTANCE.getValidFileName(beatmap.getBeatmapFullname() + ".osz"));
            try {
                ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(outputFile));
                Platform.runLater(() -> mainScreen.subProgress.setProgress(0));
                int fileCount = 0;
                for (String filename : beatmap.getFileMap().keySet()) {
                    byte[] buffer = new byte[1024];
                    FileInputStream in = new FileInputStream(getFileFromHash(beatmap.getFileMap().get(filename)));
                    stream.putNextEntry(new ZipEntry(filename));
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        stream.write(buffer, 0, length);
                    fileCount++;
                    int finalFileCount = fileCount;
                    Platform.runLater(() -> mainScreen.subProgress.setProgress((double) finalFileCount / beatmap.getFileMap().keySet().size()));
                }
                stream.close();
            } catch (IOException e) {
                failCount++;
                Platform.runLater(() -> {
                    mainScreen.consoleArea.appendText(Localizer.getLocalizedText("status.beatmap.failZipping").replace("%BEATMAP%", beatmap.getBeatmapFullname()) + "\n");
                    mainScreen.consoleArea.appendText(e.getClass().getName() + " - " + e.getMessage() + "\n");
                });
                e.printStackTrace();
            }
            progress++;
            updateProgress(progress, Global.INSTANCE.beatmapList.size());
        }
        if (failCount == 0)
            updateMessage(Localizer.getLocalizedText("status.success"));
        else
            updateMessage(Localizer.getLocalizedText("status.finishWithFailure").replace("%FAILCOUNT%", Integer.toString(failCount)));
        updateProgress(0, 0);
        Platform.runLater(() -> {
            mainScreen.enableButtons();
            mainScreen.subProgress.setProgress(0);
        });
        Global.INSTANCE.inProgress = false;
        return null;
    }

    private File getFileFromHash(String hash) {
        return new File(Global.INSTANCE.getLazerDirectory(), "files/" + hash.substring(0, 1) + "/" + hash.substring(0, 2) + "/" + hash);
    }
}
