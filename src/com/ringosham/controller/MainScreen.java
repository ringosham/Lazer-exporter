package com.ringosham.controller;

import com.ringosham.locale.Localizer;
import com.ringosham.objects.Global;
import com.ringosham.threads.export.beatmap.BeatmapExport;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainScreen {

    @FXML
    public Label statusText;
    @FXML
    public ProgressBar mainProgress;
    @FXML
    public ProgressBar subProgress;
    @FXML
    private Button importList;
    @FXML
    private Button exportMap;
    @FXML
    private Button exportList;
    @FXML
    private Button exportSongs;
    @FXML
    public TableView beatmapList;
    @FXML
    public TextArea consoleArea;
    @FXML
    private Button downloadMaps;

    //Stages
    private Stage settingsStage = new Stage();
    private Stage aboutStage = new Stage();
    private Stage exportStage = new Stage();
    private Stage loginStage = new Stage();

    private boolean shownDisclaimer = false;

    public void initialize() {
        statusText.setText(Localizer.getLocalizedText("readyStatus").replace("%BEATMAPCOUNT%", Integer.toString(Global.INSTANCE.beatmapList.size())));
    }

    public void exit() {
        if (Global.INSTANCE.inProgress) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, Localizer.getLocalizedText("confirmExitDesc"), ButtonType.YES, ButtonType.NO);
            alert.setTitle(Localizer.getLocalizedText("confirmExit"));
            alert.setHeaderText(Localizer.getLocalizedText("confirmExit"));
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
        unbindNodes();
        File importDir = getChooserDirectory(true);
        disableButtons();
    }

    public void downloadMaps() throws IOException {
        unbindNodes();
        loadStage(loginStage, Localizer.getLocalizedText("loginTitle"), "../fxml/login.fxml", new Login(this, loginStage));
    }

    public void exportSongs() throws IOException {
        unbindNodes();
        if (!shownDisclaimer)
            showDisclaimer();
        loadStage(exportStage, Localizer.getLocalizedText("exportSongs"), "../fxml/songExport.fxml", new SongExportScreen(this, exportStage));
    }

    private void showDisclaimer() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Localizer.getLocalizedText("disclaimer"));
        alert.setHeaderText(Localizer.getLocalizedText("disclaimerHead"));
        alert.setContentText(Localizer.getLocalizedText("disclaimerDesc"));
        alert.showAndWait();
        shownDisclaimer = true;
    }

    public void exportList() {
        unbindNodes();
        File exportDir = getChooserDirectory(false);
        disableButtons();
    }

    public void exportMaps() {
        unbindNodes();
        File exportDir = getChooserDirectory(false);
        if (exportDir == null)
            return;
        BeatmapExport export = new BeatmapExport(this, exportDir);
        statusText.textProperty().bind(export.messageProperty());
        mainProgress.progressProperty().bind(export.progressProperty());
        Thread thread = new Thread(export);
        thread.setDaemon(true);
        Global.INSTANCE.inProgress = true;
        thread.start();
        disableButtons();
    }

    void disableButtons() {
        importList.setDisable(true);
        downloadMaps.setDisable(true);
        exportList.setDisable(true);
        exportMap.setDisable(true);
        exportSongs.setDisable(true);
    }

    public void enableButtons() {
        importList.setDisable(false);
        downloadMaps.setDisable(false);
        exportList.setDisable(false);
        exportMap.setDisable(false);
        exportSongs.setDisable(false);
    }

    private void unbindNodes() {
        statusText.textProperty().unbind();
        mainProgress.progressProperty().unbind();
        subProgress.progressProperty().unbind();
    }

    private File getChooserDirectory(boolean isImport) {
        DirectoryChooser chooser = new DirectoryChooser();
        if (isImport)
            chooser.setTitle(Localizer.getLocalizedText("chooseImportDir"));
        else
            chooser.setTitle(Localizer.getLocalizedText("chooseExportDir"));
        return chooser.showDialog(null);
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
