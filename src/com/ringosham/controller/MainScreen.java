package com.ringosham.controller;

import com.ringosham.locale.Localizer;
import com.ringosham.objects.Global;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

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
    public TableView beatmapList;
    @FXML
    public TextArea consoleArea;
    @FXML
    public Button downloadMaps;
    @FXML
    public MenuItem launchGame;

    //Stages
    private Stage settingsStage = new Stage();
    private Stage aboutStage = new Stage();
    private Stage exportStage = new Stage();
    private Stage loginStage = new Stage();

    public void initialize() {
        statusText.setText(Localizer.getLocalizedText("readyStatus").replace("%BEATMAPCOUNT%", Integer.toString(Global.INSTANCE.beatmapList.size())));
    }

    public void exit() {
        if (Global.INSTANCE.inProgress) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "A process is still running. Confirm exit?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES)
                Platform.exit();
        } else
            Platform.exit();
    }

    public void settingsWindow() throws IOException {
        loadStage(settingsStage, Localizer.getLocalizedText("settings"), "../fxml/settings.fxml");
    }

    private void loadStage(Stage stage, String title, String resourcePath) throws IOException {
        loadStage(stage, title, resourcePath, null);
    }

    private void loadStage(Stage stage, String title, String resourcePath, Object controller) throws IOException {
        if (stage.isShowing()) {
            stage.toFront();
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath), Localizer.getResourceBundle());
        if (controller != null)
            loader.setController(controller);
        Parent root = loader.load();
        stage.setTitle(title);
        stage.resizableProperty().setValue(false);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void aboutWindow() throws IOException {
        loadStage(aboutStage, Localizer.getLocalizedText("menu.help.about"), "../fxml/about.fxml");
    }

    public void importList() {

    }

    public void downloadMaps() throws IOException {
        loadStage(loginStage, Localizer.getLocalizedText("loginTitle"), "../fxml/login.fxml", new Login(this, loginStage));
    }

    public void exportSongs() throws IOException {
        loadStage(exportStage, Localizer.getLocalizedText("exportSongs"), "../fxml/songExport.fxml", new SongExportScreen(this, exportStage));
    }

    public void exportList() {

    }

    public void exportMaps() {

    }

    public void launchGame() {
        String os = System.getProperty("os.name").toLowerCase();
        String gameExecutable;
        if (os.contains("win"))
            gameExecutable = System.getenv("localappdata").replaceAll("\\\\", "/") + "/osulazer/osu!.exe";
        else if (os.contains("mac"))
            gameExecutable = "";
        else
            gameExecutable = "";
        try {
            Runtime.getRuntime().exec(gameExecutable);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cannot launch process");
            alert.setContentText("Cannot launch game. This normally should not happen. If you believe this is a bug, please report it to GitHub.");
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}
