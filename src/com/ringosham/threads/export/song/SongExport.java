/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.export.song;

import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.ExportSettings;
import com.ringosham.objects.Song;
import it.sauronsoftware.jave.FFMPEGLocator;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.List;

public class SongExport extends Task<Void> {
    static int failCount = 0;
    private MainScreen mainScreen;
    private ExportSettings settings;
    static FFMPEGLocator locator;

    public SongExport(MainScreen mainScreen, ExportSettings settings) {
        this.mainScreen = mainScreen;
        this.settings = settings;
    }

    @Override
    protected Void call() {
        List<Song> songList;
        Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("initializing")));
        //Extract executables from jar, based on arch and os
        String os = System.getProperty("os.name").toLowerCase();
        //String arch = System.getProperty("sun.arch.data.model");
        String amd64Path = "/com/ringosham/bin/amd64/";
        //String ia32Path = "/com/ringosham/bin/ia32/";
        File ffmpeg = new File(System.getProperty("java.io.tmpdir"), "ffmpeg" + (os.contains("win") ? ".exe" : ""));
        InputStream ffmpegStream;
        //These os checks are just there in case I want to include newer binaries.
        /*boolean isUnixFs;
        if (os.contains("win")) {
            ffmpegStream = getClass().
                    getResourceAsStream(arch.equals("64") ? amd64Path : ia32Path + "windows/ffmpeg.exe");
            isUnixFs = false;
        } else if (os.contains("mac")) {
            //macOS and Mac OS X have been 64 bit for a long time as far as I know. So I don't think a 32 bit version is needed.
            ffmpegStream = getClass().
                    getResourceAsStream(amd64Path + "macos/ffmpeg");
            isUnixFs = true;
        } else {
            ffmpegStream = getClass().
                    getResourceAsStream(arch.equals("64") ? amd64Path : ia32Path + "linux/ffmpeg");
            isUnixFs = true;
        }*/
        boolean customExecutable = false;
        if (os.contains("mac")) {
            ffmpegStream = getClass().getResourceAsStream(amd64Path + "macos/ffmpeg");
            try {
                copyExecutables(ffmpeg, ffmpegStream, true);
            } catch (IOException e) {
                failCount++;
                Platform.runLater(() -> {
                    mainScreen.mainProgress.setProgress(0);
                    mainScreen.subProgress.setProgress(0);
                    mainScreen.statusText.setText(Localizer.getLocalizedText("taskFinishWithFailure")
                            .replace("%FAILCOUNT%", Integer.toString(failCount)));
                    mainScreen.consoleArea.appendText(Localizer.getLocalizedText("failInitialize") + "\n");
                    mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
                    mainScreen.enableButtons();
                });
                Global.INSTANCE.inProgress = false;
                e.printStackTrace();
                return null;
            }
            customExecutable = true;
            locator = new FFMPEGLocator() {
                @Override
                protected String getFFMPEGExecutablePath() {
                    String os = System.getProperty("os.name");
                    return new File(System.getProperty("java.io.tmpdir"), "ffmpeg" + (os.contains("win") ? ".exe" : "")).getAbsolutePath();
                }
            };
        }
        Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("analysing")));
        Analyser analyser = new Analyser(mainScreen, customExecutable);
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
                    new Converter(mainScreen, song, customExecutable).run();
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

    private void copyExecutables(File ffmpeg, InputStream ffmpegStream, boolean isUnixFs) throws IOException {
        Files.copy(ffmpegStream, ffmpeg.toPath());
        if (isUnixFs)
            Files.setPosixFilePermissions(ffmpeg.toPath(), PosixFilePermissions.fromString("rwxrwxr-x"));
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
        String os = System.getProperty("os.name").toLowerCase();
        File ffmpeg = new File(System.getProperty("java.io.tmpdir"), "ffmpeg" + (os.contains("win") ? ".exe" : ""));
        File ffprobe = new File(System.getProperty("java.io.tmpdir"), "ffprobe" + (os.contains("win") ? ".exe" : ""));
        Files.delete(ffmpeg.toPath());
        Files.delete(ffprobe.toPath());
    }
}
