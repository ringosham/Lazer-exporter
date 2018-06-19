/*
 * Copyright (c) 2018. Ringosham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.export.song;

import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import com.ringosham.objects.Metadata;
import com.ringosham.objects.Song;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

class Copier {
    private final boolean overwrite;
    private final boolean romajiNaming;
    private final boolean renameAsBeatmap;
    private MainScreen mainScreen;
    private List<Song> songList;
    private File exportDirectory;

    Copier(MainScreen mainScreen, List<Song> songList, boolean overwrite, boolean romajiNaming, boolean renameAsBeatmap, File exportDirectory) {
        this.mainScreen = mainScreen;
        this.songList = songList;
        this.overwrite = overwrite;
        this.romajiNaming = romajiNaming;
        this.renameAsBeatmap = renameAsBeatmap;
        this.exportDirectory = exportDirectory;
    }

    void run() {
        int workDone = 0;
        for (Song song : songList) {
            String filename;
            Metadata metadata = getMetadataFromSong(song.getBeatmapID());
            assert metadata != null;
            if (renameAsBeatmap) {
                if (metadata.getUnicodeTitle() != null && metadata.getUnicodeArtist() != null)
                    if (metadata.getUnicodeTitle().isEmpty() && metadata.getUnicodeArtist().isEmpty() && !romajiNaming)
                        filename = metadata.getTitle() + " - " + metadata.getArtist();
                    else
                        filename = metadata.getUnicodeTitle() + " - " + metadata.getUnicodeArtist();
                else
                    filename = metadata.getTitle() + " - " + metadata.getArtist();
                if (song.isFullVersion())
                    filename = filename + " (Full version)";
                filename = getValidFileName(filename);
            } else
                filename = getBeatmapFullname(song.getBeatmapID());
            if (metadata.getUnicodeTitle() == null || metadata.getUnicodeArtist() == null)
                Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("copying")
                        .replace("%SONG%", metadata.getTitle() + " - " + metadata.getArtist())));
            else
                Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("copying")
                        .replace("%SONG%", metadata.getUnicodeTitle() + " - " + metadata.getUnicodeArtist())));
            if (!song.isOgg())
                filename = filename + ".mp3";
            else
                filename = filename + ".ogg";
            File outputFile = new File(exportDirectory.getAbsolutePath(), filename);
            try {
                if (overwrite)
                    Files.copy(song.getFileLocation().toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                else if (outputFile.length() != song.getFileLocation().length())
                    Files.copy(song.getFileLocation().toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                song.setOutputLocation(outputFile);
            } catch (IOException e) {
                Platform.runLater(() -> {
                    String error = Localizer.getLocalizedText("errorCopy").replace("%SONG%", metadata.getTitle() + " - " + metadata.getArtist());
                    mainScreen.consoleArea.appendText(error + "\n");
                    mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
                });
                SongExport.failCount++;
                e.printStackTrace();
            }
            workDone++;
            double progressDouble = ((double) workDone) / songList.size();
            Platform.runLater(() -> mainScreen.mainProgress.setProgress(progressDouble));
        }
    }

    private Metadata getMetadataFromSong(int beatmapID) {
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            if (beatmap.getBeatmapId() == beatmapID)
                return beatmap.getMetadata();
        }
        return null;
    }

    private String getBeatmapFullname(int beatmapID) {
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            if (beatmap.getBeatmapId() == beatmapID)
                return beatmap.getBeatmapFullname();
        }
        return null;
    }

    //Remove any illegal characters in the file name
    //Many export programs I have seen forgot to eliminate illegal characters from the file name
    private String getValidFileName(String name) {
        return name.replaceAll("\\*", "").replaceAll("<", "").replaceAll(">", "")
                .replaceAll("\\|", "").replaceAll("\\?", "").replaceAll(":", "")
                .replaceAll("\"", "").replaceAll("\\\\", ",").replaceAll("/", ",");
    }
}
