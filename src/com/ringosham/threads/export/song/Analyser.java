package com.ringosham.threads.export.song;

import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import com.ringosham.objects.Global;
import com.ringosham.objects.Song;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.gagravarr.vorbis.VorbisFile;

import java.io.File;
import java.io.IOException;
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
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            File songFile = getFileFromHash(beatmap.getFileMap().get(beatmap.getMetadata().getAudioFilename()));
            //Determine song length
            if (beatmap.getFileMap().get(beatmap.getMetadata().getAudioFilename()).toLowerCase().endsWith(".mp3")) {
                Media mediaFile = new Media(songFile.toURI().toString());
                MediaPlayer player = new MediaPlayer(mediaFile);
                player.setOnError(() -> {
                    SongExport.failCount++;
                    mainScreen.consoleArea.appendText(Localizer.getLocalizedText("errorMp3").replace("%BEATMAP%", beatmap.getBeatmapFullname()));
                    mainScreen.consoleArea.appendText("\n");
                    mainScreen.consoleArea.appendText(player.getError().getClass().getName() + " : " + player.getError().getMessage());
                    mainScreen.consoleArea.appendText("\n");
                    player.getError().printStackTrace();
                    player.dispose();
                });
                long duration;
                while (player.getStatus() == MediaPlayer.Status.UNKNOWN) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                }
                if (player.getStatus() != MediaPlayer.Status.DISPOSED)
                    duration = (long) mediaFile.getDuration().toSeconds();
                else {
                    continue;
                }
                songMap.put(beatmap.getFileMap().get(beatmap.getMetadata().getAudioFilename()),
                        new Song(songFile, beatmap.getBeatmapId(), false, duration));
            } else {
                try {
                    VorbisFile vorbisFile = new VorbisFile(songFile);
                    //Nominal bitrate is not accurate enough though. About 10 seconds in error
                    long duration = (songFile.length() * 8) / vorbisFile.getInfo().getBitrateNominal();
                    vorbisFile.close();
                    songMap.put(beatmap.getFileMap().get(beatmap.getMetadata().getAudioFilename()),
                            new Song(songFile, beatmap.getBeatmapId(), true, duration));
                } catch (IOException e) {
                    SongExport.failCount++;
                    mainScreen.consoleArea.appendText(Localizer.getLocalizedText("errorOgg").replace("%BEATMAP%", beatmap.getBeatmapFullname()));
                    mainScreen.consoleArea.appendText("\n");
                    mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage());
                    mainScreen.consoleArea.appendText("\n");
                    e.printStackTrace();
                }
            }
        }
        return new ArrayList<>(songMap.values());
    }

    private File getFileFromHash(String hash) {
        return new File(Global.INSTANCE.getLazerDirectory(), "files/" + hash.substring(0, 1) + "/" + hash.substring(0, 2) + "/" + hash);
    }
}
