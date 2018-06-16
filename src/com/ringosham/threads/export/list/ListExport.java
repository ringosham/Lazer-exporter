package com.ringosham.threads.export.list;

import javafx.concurrent.Task;

import java.io.File;

public class ListExport extends Task<Void> {
    private File exportDir;

    public ListExport(File exportDir) {
        this.exportDir = exportDir;
    }

    @Override
    protected Void call() {
        return null;
    }
}
