package com.ringosham.objects;

import java.util.HashMap;

public class Beatmap {
    private int beatmapId;
    private Metadata metadata;
    private HashMap<String, String> fileMap;

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
}
