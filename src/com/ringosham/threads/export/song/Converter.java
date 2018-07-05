/*
 * Copyright (c) 2018. Ringo Sham.
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
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

class Converter {
    private final MainScreen mainScreen;
    private final Song song;

    private boolean taskFinish = false;

    Converter(MainScreen mainScreen, Song song) {
        this.mainScreen = mainScreen;
        this.song = song;
    }

    void run() {
        Metadata metadata = getMetadataFromSong(song.getBeatmapID());
        assert metadata != null;
        if (metadata.getUnicodeTitle() == null || metadata.getUnicodeArtist() == null)
            Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("converting")
                    .replace("%SONG%", metadata.getTitle() + " - " + metadata.getArtist())));
        else
            Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("converting")
                    .replace("%SONG%", metadata.getUnicodeTitle() + " - " + metadata.getUnicodeArtist())));
        if (!Global.INSTANCE.getConvertDir().exists()) {
            try {
                Files.createDirectory(Global.INSTANCE.getConvertDir().toPath());
            } catch (IOException e) {
                String error = Localizer.getLocalizedText("errorCreateDir").replace("%SONG%", metadata.getTitle() + " - " + metadata.getArtist());
                mainScreen.consoleArea.appendText(error + "\n");
                mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }
        File output = new File(Global.INSTANCE.getConvertDir().getAbsolutePath(), UUID.randomUUID().toString() + ".mp3");
        String os = System.getProperty("os.name").toLowerCase();
        File ffmpegExecutable = new File(System.getProperty("java.io.tmpdir"), "ffmpeg" + (os.contains("win") ? ".exe" : ""));
        try {
            FFmpeg ffmpeg = new FFmpeg(ffmpegExecutable.getAbsolutePath());
            FFmpegBuilder builder = new FFmpegBuilder();
            builder.setInput(song.getFileLocation().getAbsolutePath())
                    .overrideOutputFiles(true)
                    .addOutput(output.getAbsolutePath())
                    .done();
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
            FFmpegJob job = executor.createJob(builder, progress -> {
                double progressValue = (double) progress.out_time_ns / song.getLengthInSeconds();
                Platform.runLater(() -> mainScreen.subProgress.setProgress(progressValue));
                if (progressValue == 1)
                    taskFinish = true;
            });
            job.run();
            while (!taskFinish)
                Thread.sleep(1);
            song.setFileLocation(output);
            song.setOgg(false);
        } catch (Exception e) {
            Platform.runLater(() -> {
                String error = Localizer.getLocalizedText("errorConvert").replace("%SONG%", metadata.getTitle() + " - " + metadata.getArtist());
                mainScreen.consoleArea.appendText(error + "\n");
                mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
            });
            e.printStackTrace();
        }
    }

    //Again, it is impossible to return null as it is a 1 to 1 relationship
    private Metadata getMetadataFromSong(int beatmapID) {
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            if (beatmap.getBeatmapId() == beatmapID)
                return beatmap.getMetadata();
        }
        return null;
    }
}
