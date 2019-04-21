/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.export.song;

import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.objects.Song;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

class CleanUp {

    private MainScreen screen;

    CleanUp(MainScreen screen) {
        this.screen = screen;
    }

    void run() throws IOException {
        if (!Global.INSTANCE.getConvertDir().exists())
            return;
        Files.walk(Global.INSTANCE.getConvertDir().toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        String os = System.getProperty("os.name").toLowerCase();
        File ffmpeg = new File(System.getProperty("java.io.tmpdir"), "ffmpeg" + (os.contains("win") ? ".exe" : ""));
        if (ffmpeg.exists())
            Files.delete(ffmpeg.toPath());
    }

    void run(List<Song> songList, File exportDirectory) throws IOException {
        Platform.runLater(() -> {
            screen.mainProgress.setProgress(-1);
            screen.subProgress.setProgress(0);
        });
        run();
        for (File file : Objects.requireNonNull(exportDirectory.listFiles())) {
            boolean exist = false;
            for (Song song : songList) {
                if (song.getOutputLocation().equals(file)) {
                    exist = true;
                    break;
                }
            }
            if (!exist)
                Files.delete(file.toPath());
        }
    }
}
