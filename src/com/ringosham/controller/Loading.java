/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.controller;

import com.ringosham.threads.LoadTask;
import javafx.stage.Stage;

public class Loading {

    private final Stage stage;

    public Loading(Stage stage) {
        this.stage = stage;
    }

    public void initialize() {
        LoadTask task = new LoadTask(stage);
        stage.titleProperty().bind(task.titleProperty());
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
