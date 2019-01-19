/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.objects;

import java.util.HashMap;

public class Beatmap {
    private final int beatmapId;
    private final Metadata metadata;
    private final HashMap<String, String> fileMap;

    public Beatmap(int beatmapId, Metadata metadata, HashMap<String, String> fileMap) {
        this.beatmapId = beatmapId;
        this.metadata = metadata;
        this.fileMap = fileMap;
    }

    public int getBeatmapId() {
        return beatmapId;
    }

    public HashMap<String, String> getFileMap() {
        return fileMap;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String getBeatmapFullname() {
        return beatmapId + " " + metadata.getArtist() + " - " + metadata.getTitle();
    }
}
