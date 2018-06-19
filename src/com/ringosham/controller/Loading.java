/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.controller;

import com.ringosham.threads.LoadTask;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class Loading {

    private Stage stage;
    private HostServices hostServices;

    public Loading(Stage stage, HostServices hostServices) {
        this.stage = stage;
        this.hostServices = hostServices;
    }

    public void initialize() {
        LoadTask task = new LoadTask(stage, hostServices);
        stage.titleProperty().bind(task.titleProperty());
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
