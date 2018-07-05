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
import com.ringosham.objects.Song;
import javafx.application.Platform;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Analyser {
    private MainScreen mainScreen;

    Analyser(MainScreen mainScreen) {
        this.mainScreen = mainScreen;
    }

    List<Song> run() {
        //Using a HashMap filters files that are exactly the same (Hash matches)
        /*
            Since the game hashes every file that is imported. We can just use the hashes stored in the database instead of
            hashing them ourselves.
         */
        Map<String, Song> songMap = new HashMap<>();
        Platform.runLater(() -> {
            mainScreen.mainProgress.setProgress(0);
            mainScreen.subProgress.setProgress(-1);
        });
        String os = System.getProperty("os.name").toLowerCase();
        File ffprobeExecutable = new File(System.getProperty("java.io.tmpdir"), "ffprobe" + (os.contains("win") ? ".exe" : ""));
        int progress = 0;
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            String audioFilename = beatmap.getFileMap().get(beatmap.getMetadata().getAudioFilename());
            File songFile = getFileFromHash(audioFilename);
            try {
                FFprobe ffprobe = new FFprobe(ffprobeExecutable.getAbsolutePath());
                FFmpegProbeResult result = ffprobe.probe(songFile.getAbsolutePath());
                long bitrate = result.getFormat().bit_rate;
                int duration = (int) result.getFormat().duration;
                //Beatmap songs can only be MP3s or Vorbis ogg.
                songMap.put(beatmap.getFileMap().get(beatmap.getMetadata().getAudioFilename()),
                        new Song(songFile, beatmap.getBeatmapId(),
                                beatmap.getMetadata().getAudioFilename().toLowerCase().endsWith(".ogg"), duration, bitrate));
            } catch (Exception e) {
                SongExport.failCount++;
                Platform.runLater(() -> {
                    mainScreen.consoleArea.appendText(Localizer.getLocalizedText("errorSong").replace("%BEATMAP%",
                            beatmap.getBeatmapFullname()));
                    mainScreen.consoleArea.appendText("\n");
                    mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage());
                    mainScreen.consoleArea.appendText("\n");
                });
                e.printStackTrace();
            }
            progress++;
            int finalProgress = progress;
            Platform.runLater(() -> mainScreen.mainProgress.setProgress((double) finalProgress / Global.INSTANCE.beatmapList.size()));
        }
        return new ArrayList<>(songMap.values());
    }

    private File getFileFromHash(String hash) {
        return new File(Global.INSTANCE.getLazerDirectory(), "files/" + hash.substring(0, 1) + "/" + hash.substring(0, 2) + "/" + hash);
    }
}
