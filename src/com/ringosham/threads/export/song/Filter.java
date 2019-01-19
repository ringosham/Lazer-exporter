/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.export.song;

import com.ringosham.Global;
import com.ringosham.objects.Beatmap;
import com.ringosham.objects.Song;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

class Filter {
    private final List<Song> songList;
    private final boolean filterPractice;
    private final boolean filterDuplicates;
    private final int filterSeconds;

    Filter(List<Song> songList, boolean filterPractice, boolean filterDuplicates, int filterSeconds) {
        this.songList = songList;
        this.filterPractice = filterPractice;
        this.filterDuplicates = filterDuplicates;
        this.filterSeconds = filterSeconds;
    }

    List<Song> run() {
        List<Song> temp = new LinkedList<>(songList);
        if (filterPractice)
            temp.removeIf(song -> {
                String[] filters = {"stream practice", "stream practise", "jump practice", "jump practise"};
                for (String filter : filters)
                    if (Objects.requireNonNull(getTitleFromSong(song.getBeatmapID())).contains(filter))
                        return true;
                return false;
            });
        if (filterDuplicates) {
            int j = songList.size();
            for (int i = 0; i < j; i++) {
                Iterator<Song> iterator = songList.iterator();
                for (int k = 0; k < i + 1; k++)
                    iterator.next();
                while (iterator.hasNext()) {
                    Song songA = songList.get(i);
                    Song songB = iterator.next();
                    String songATitle = Objects.requireNonNull(getTitleFromSong(songA.getBeatmapID())).toLowerCase().trim();
                    String songBTitle = Objects.requireNonNull(getTitleFromSong(songB.getBeatmapID())).toLowerCase().trim();
                    if (songATitle.equals(songBTitle)) {
                        if (Math.abs(songA.getLengthInSeconds() - songB.getLengthInSeconds()) < filterSeconds) {
                            iterator.remove();
                        } else {
                            if (songA.getLengthInSeconds() > songB.getLengthInSeconds())
                                songA.setFullVersion(true);
                            else
                                songB.setFullVersion(true);
                        }
                    }
                }
                j = songList.size();
            }
        }
        return temp;
    }

    //It is actually impossible to get null in this situation as it is a 1 to 1 relationship.
    private String getTitleFromSong(int beatmapID) {
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            if (beatmap.getBeatmapId() == beatmapID)
                return beatmap.getMetadata().getTitle();
        }
        return null;
    }
}
