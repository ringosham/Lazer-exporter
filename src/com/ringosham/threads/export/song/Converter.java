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
import it.sauronsoftware.jave.*;
import javafx.application.Platform;

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
        boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        Encoder encoder = new Encoder();
        AudioAttributes audioInfo = new AudioAttributes();
        audioInfo.setBitRate(song.getBitrate());
        EncodingAttributes attributes = new EncodingAttributes();
        attributes.setAudioAttributes(audioInfo);
        attributes.setFormat("mp3");
        try {
            encoder.encode(song.getFileLocation(), output, attributes, new EncoderProgressListener() {
                @Override
                public void sourceInfo(MultimediaInfo multimediaInfo) {
                }

                @Override
                public void progress(int i) {
                    if (isMac)
                        Platform.runLater(() -> mainScreen.subProgress.setProgress(-1));
                    else
                        Platform.runLater(() -> mainScreen.subProgress.setProgress((double) i / 1000));
                }

                @Override
                public void message(String s) {
                }
            });
            song.setFileLocation(output);
            song.setOgg(false);
        } catch (EncoderException e) {
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
