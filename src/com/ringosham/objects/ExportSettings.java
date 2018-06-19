/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.objects;

import java.io.File;

public class ExportSettings {
    private boolean convertOgg;
    private boolean filterPractice;
    private boolean overwrite;
    private boolean applyTags;
    private boolean overrideTags;
    private boolean renameAsBeatmap;
    private boolean filterDuplicates;
    private boolean romajiNaming;
    private int filterSeconds;
    private File exportDirectory;


    public ExportSettings(boolean convertOgg, boolean filterPractice, boolean overwrite, boolean applyTags, boolean overrideTags, boolean renameAsBeatmap, boolean filterDuplicates, boolean romajiNaming, int filterSeconds, File exportDirectory) {
        this.convertOgg = convertOgg;
        this.filterPractice = filterPractice;
        this.overwrite = overwrite;
        this.applyTags = applyTags;
        this.overrideTags = overrideTags;
        this.renameAsBeatmap = renameAsBeatmap;
        this.filterDuplicates = filterDuplicates;
        this.romajiNaming = romajiNaming;
        this.filterSeconds = filterSeconds;
        this.exportDirectory = exportDirectory;
    }

    public boolean isConvertOgg() {
        return convertOgg;
    }

    public boolean isFilterPractice() {
        return filterPractice;
    }

    public boolean isApplyTags() {
        return applyTags;
    }

    public boolean isOverrideTags() {
        return overrideTags;
    }

    public boolean isRenameAsBeatmap() {
        return renameAsBeatmap;
    }

    public boolean isFilterDuplicates() {
        return filterDuplicates;
    }

    public int getFilterSeconds() {
        return filterSeconds;
    }

    public File getExportDirectory() {
        return exportDirectory;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public boolean isRomajiNaming() {
        return romajiNaming;
    }
}
