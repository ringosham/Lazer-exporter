package com.ringosham.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MainScreen {

    @FXML
    public MenuItem menuSettings;
    @FXML
    public MenuItem menuClose;
    @FXML
    public MenuItem menuAbout;
    @FXML
    public Label statusText;
    @FXML
    public ProgressBar mainProgress;
    @FXML
    public ProgressBar subProgress;
    @FXML
    public Button importList;
    @FXML
    public Button exportMap;
    @FXML
    public Button exportList;
    @FXML
    public Button exportSongs;
    @FXML
    public ListView beatmapList;
    @FXML
    public TextArea consoleArea;
    @FXML
    public Button downloadMaps;
    @FXML
    public MenuItem launchGame;

    public void initialize() {

    }
}
