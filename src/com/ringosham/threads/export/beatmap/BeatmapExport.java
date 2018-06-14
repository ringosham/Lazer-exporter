package com.ringosham.threads.export.beatmap;

import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import com.ringosham.objects.Global;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BeatmapExport extends Task<Void> {
    private MainScreen mainScreen;
    private File exportDir;
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
            String beatmapName = beatmap.getBeatmapId() + " " + beatmap.getMetadata().getArtist() + " - " + beatmap.getMetadata().getTitle();
            updateMessage(Localizer.getLocalizedText("exportingMap").replace("%BEATMAP%", beatmapName));
            File outputFile = new File(exportDir, fixIllegalFilename(beatmapName + ".osz"));
            try {
                ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(outputFile));
                for (String filename : beatmap.getFileMap().keySet()) {
                    byte[] buffer = new byte[1024];
                    FileInputStream in = new FileInputStream(getFileFromHash(beatmap.getFileMap().get(filename)));
                    stream.putNextEntry(new ZipEntry(filename));
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        stream.write(buffer, 0, length);
                }
                stream.close();
            } catch (IOException e) {
                failCount++;
                Platform.runLater(() -> {
                    mainScreen.consoleArea.appendText(Localizer.getLocalizedText("failZipping").replace("%BEATMAP%", beatmapName) + "\n");
                    mainScreen.consoleArea.appendText(e.getClass().getName() + " - " + e.getMessage() + "\n");
                });
                e.printStackTrace();
            }
            progress++;
            updateProgress(progress, Global.INSTANCE.beatmapList.size());
        }
        if (failCount == 0)
            updateMessage(Localizer.getLocalizedText("taskSuccess"));
        else
            updateMessage(Localizer.getLocalizedText("taskFinishWithFailure").replace("%FAILCOUNT%", Integer.toString(failCount)));
        updateProgress(0, 0);
        Platform.runLater(() -> mainScreen.enableButtons());
        Global.INSTANCE.inProgress = false;
        return null;
    }

    private File getFileFromHash(String hash) {
        return new File(Global.INSTANCE.getLazerDirectory(), "files/" + hash.substring(0, 1) + "/" + hash.substring(0, 2) + "/" + hash);
    }

    private String fixIllegalFilename(String filename) {
        return filename.replaceAll("\\*", "").replaceAll("<", "").replaceAll(">", "")
                .replaceAll("\\|", "").replaceAll("\\?", "").replaceAll(":", "")
                .replaceAll("\"", "").replaceAll("\\\\", ",").replaceAll("/", ",");
    }
}
