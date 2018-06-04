package com.ringosham.controller;

import com.ringosham.threads.LoadTask;
import javafx.fxml.FXML;

public class Loading {
    @FXML
    public void initialize() {
        LoadTask task = new LoadTask();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
