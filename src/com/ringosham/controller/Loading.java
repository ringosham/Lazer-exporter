package com.ringosham.controller;

import com.ringosham.threads.LoadTask;
import javafx.stage.Stage;

public class Loading {

    private Stage stage;

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
