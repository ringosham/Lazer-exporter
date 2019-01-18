/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.export.song;

import com.mpatric.mp3agic.*;
import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import com.ringosham.objects.Metadata;
import com.ringosham.objects.Song;
import javafx.application.Platform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

class Tagger {
    private final MainScreen mainScreen;
    private final List<Song> songList;
    private final boolean overrideTags;

    Tagger(MainScreen mainScreen, List<Song> songList, boolean overrideTags) {
        this.mainScreen = mainScreen;
        this.songList = songList;
        this.overrideTags = overrideTags;
    }

    void run() {
        int workDone = 0;
        for (Song song : songList) {
            if (song.isOgg())
                continue;
            //ID3v2 supports album arts
            ID3v2 v2Tag;
            try {
                boolean changesMade = false;
                Mp3File mp3 = new Mp3File(song.getOutputLocation());
                if (overrideTags) {
                    applyTags(mp3, song);
                    changesMade = true;
                } else {
                    if (mp3.hasId3v2Tag()) {
                        String artist = null;
                        String title = null;
                        //This will preserve some data from old ID3v1 tags, but it will be overwritten with ID3v2 to support album arts.
                        if (mp3.hasId3v1Tag()) {
                            ID3v1 v1Tag = mp3.getId3v1Tag();
                            artist = v1Tag.getArtist();
                            title = v1Tag.getTitle();
                            mp3.removeId3v1Tag();
                        }
                        v2Tag = mp3.getId3v2Tag();
                        if (artist == null)
                            artist = v2Tag.getArtist();
                        if (title == null)
                            title = v2Tag.getTitle();
                        if ((title == null || artist == null)) {
                            applyTags(mp3, song);
                            changesMade = true;
                        } else if ((title.trim().isEmpty() || artist.trim().isEmpty())) {
                            applyTags(mp3, song);
                            changesMade = true;
                        }
                    }
                }
                if (changesMade) {
                    String tempFilename = UUID.randomUUID().toString();
                    String parent = song.getOutputLocation().getParent();
                    String filename = song.getOutputLocation().getName();
                    mp3.save(parent + "/" + tempFilename);
                    if (!song.getOutputLocation().delete())
                        song.getOutputLocation().deleteOnExit();
                    Files.move(new File(parent, tempFilename).toPath(), new File(parent, filename).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException | UnsupportedTagException | InvalidDataException | NotSupportedException e) {
                Metadata metadata = getMetadataFromSong(song.getBeatmapID());
                assert metadata != null;
                String error = Localizer.getLocalizedText("export.error.applyTag").replace("%SONG%", metadata.getTitle() + " - " + metadata.getArtist());
                mainScreen.consoleArea.appendText(error + "\n");
                mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
                e.printStackTrace();
            }
            workDone++;
            double progressDouble = ((double) workDone) / songList.size();
            Platform.runLater(() -> mainScreen.mainProgress.setProgress(progressDouble));
        }
    }

    private void applyTags(Mp3File mp3, Song song) {
        Metadata metadata = getMetadataFromSong(song.getBeatmapID());
        assert metadata != null;
        if (metadata.getUnicodeTitle() == null || metadata.getUnicodeArtist() == null)
            Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("export.process.applying")
                    .replace("%SONG%", metadata.getTitle() + " - " + metadata.getArtist())));
        else
            Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("export.process.applying")
                    .replace("%SONG%", metadata.getUnicodeTitle() + " - " + metadata.getUnicodeArtist())));
        mp3.setId3v2Tag(generateTag(song, metadata));
    }

    private ID3v24Tag generateTag(Song song, Metadata metadata) {
        ID3v24Tag tag = new ID3v24Tag();
        if (metadata.getUnicodeTitle() == null || metadata.getUnicodeArtist() == null) {
            tag.setTitle(metadata.getTitle());
            tag.setArtist(metadata.getArtist());
        } else {
            tag.setTitle(metadata.getUnicodeTitle());
            tag.setArtist(metadata.getUnicodeArtist());
        }
        File albumArt = getBackgroundFromFilename(song.getBeatmapID(), metadata.getBackgroundFilename());
        if (albumArt != null) {
            String mimeType;
            //Album arts can only be in these formats
            if (metadata.getBackgroundFilename().endsWith(".png"))
                mimeType = "image/png";
            else if (metadata.getBackgroundFilename().endsWith(".bmp"))
                mimeType = "image/bmp";
            else
                mimeType = "image/jpeg";
            try {
                BufferedImage image = ImageIO.read(albumArt);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                String type = mimeType.replaceFirst("image/", "");
                if (type.equals("jpeg"))
                    type = "jpg";
                ImageIO.write(image, type, stream);
                stream.flush();
                byte[] artBytes = stream.toByteArray();
                stream.close();
                tag.setAlbumImage(artBytes, mimeType);
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                String error = Localizer.getLocalizedText("export.error.applyImage").replace("%SONG%", metadata.getTitle() + " - " + metadata.getArtist());
                mainScreen.consoleArea.appendText(error + "\n");
                mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
            }
        }
        return tag;
    }

    private File getBackgroundFromFilename(int beatmapID, String filename) {
        if (filename == null)
            return null;
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            if (beatmap.getBeatmapId() == beatmapID) {
                String hash = beatmap.getFileMap().get(filename);
                return new File(Global.INSTANCE.getLazerDirectory(), "files/" + hash.substring(0, 1) + "/" + hash.substring(0, 2) + "/" + hash);
            }
        }
        return null;
    }

    private Metadata getMetadataFromSong(int beatmapID) {
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            if (beatmap.getBeatmapId() == beatmapID)
                return beatmap.getMetadata();
        }
        return null;
    }
}
