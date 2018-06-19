/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.controller;

import com.ringosham.Global;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.view.BeatmapView;
import com.ringosham.threads.export.beatmap.BeatmapExport;
import com.ringosham.threads.export.list.ListExport;
import com.ringosham.threads.imports.ListImport;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

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
    public TableView<BeatmapView> beatmapList;
    @FXML
    private TableColumn<BeatmapView, Boolean> columnInstalled;
    @FXML
    private TableColumn<BeatmapView, Hyperlink> columnID;
    @FXML
    private TableColumn<BeatmapView, String> columnTitle;
    @FXML
    private TableColumn<BeatmapView, String> columnArtist;
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
    private HostServices hostServices;

    public MainScreen(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void initialize() {
        downloadMaps.setDisable(true);
        statusText.setText(Localizer.getLocalizedText("readyStatus").replace("%BEATMAPCOUNT%", Integer.toString(Global.INSTANCE.beatmapList.size())));
        //Bind table values to BeatmapView.
        columnInstalled.setCellValueFactory(cell -> cell.getValue().getQueueProperty());
        columnInstalled.setCellFactory(cell -> new CheckBoxTableCell<BeatmapView, Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                TableRow<BeatmapView> currentRow = (TableRow<BeatmapView>) getTableRow();
                this.setDisable(false);
                if (currentRow.getItem() != null && !empty) {
                    if (currentRow.getItem().getInstalledProperty().get()) {
                        this.setDisable(true);
                    }
                }
            }
        });
        columnID.setCellValueFactory(new PropertyValueFactory<>("beatmapId"));
        columnID.setCellFactory(new HyperlinkCell());
        columnArtist.setCellValueFactory(cell -> cell.getValue().getArtist());
        columnTitle.setCellValueFactory(cell -> cell.getValue().getTitle());
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
        loadStage(settingsStage, Localizer.getLocalizedText("settings"), "../fxml/settings.fxml", new SettingScreen(this));
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
        File importFile = getChooserFile(true);
        if (importFile == null)
            return;
        ListImport importer = new ListImport(this, importFile, hostServices);
        statusText.textProperty().bind(importer.messageProperty());
        Thread thread = new Thread(importer);
        thread.setDaemon(true);
        thread.start();
        Global.INSTANCE.inProgress = true;
        disableButtons();
    }

    public void downloadMaps() throws IOException {
        unbindNodes();
        loadStage(loginStage, Localizer.getLocalizedText("loginTitle"), "../fxml/Login.fxml", new Login(this, loginStage));
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
        File exportDir = getChooserFile(false);
        if (exportDir == null)
            return;
        ListExport export = new ListExport(this, exportDir);
        mainProgress.progressProperty().bind(export.progressProperty());
        statusText.textProperty().bind(export.messageProperty());
        Thread thread = new Thread(export);
        thread.setDaemon(true);
        thread.start();
        Global.INSTANCE.inProgress = true;
        disableButtons();
    }

    public void exportMaps() {
        unbindNodes();
        File exportDir = getChooserDirectory();
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

    public void disableButtons() {
        importList.setDisable(true);
        downloadMaps.setDisable(true);
        exportList.setDisable(true);
        exportMap.setDisable(true);
        exportSongs.setDisable(true);
    }

    public void enableButtons() {
        importList.setDisable(false);
        exportList.setDisable(false);
        exportMap.setDisable(false);
        exportSongs.setDisable(false);
    }

    public void enableAllButtons() {
        downloadMaps.setDisable(false);
        enableButtons();
    }

    private void unbindNodes() {
        statusText.textProperty().unbind();
        mainProgress.progressProperty().unbind();
        subProgress.progressProperty().unbind();
    }

    private File getChooserFile(boolean isImport) {
        FileChooser chooser = new FileChooser();
        if (isImport)
            chooser.setTitle(Localizer.getLocalizedText("chooseImportFile"));
        else
            chooser.setTitle(Localizer.getLocalizedText("chooseExportFile"));
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XML file", "*.xml");
        chooser.getExtensionFilters().add(filter);
        if (isImport)
            return chooser.showOpenDialog(null);
        else
            return chooser.showSaveDialog(null);
    }

    private File getChooserDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
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

    void resync() {
        Stage stage = (Stage) statusText.getScene().getWindow();
        stage.close();
        loginStage.close();
        exportStage.close();
        settingsStage.close();
        aboutStage.close();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/loading.fxml"), Localizer.getResourceBundle());
        loader.setController(new Loading(stage, hostServices));
        try {
            Parent root = loader.load();
            stage.resizableProperty().setValue(false);
            stage.setScene(new Scene(root, 400, 15));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectAll() {
        for (BeatmapView beatmap : beatmapList.getItems())
            beatmap.getQueueProperty().set(true);
    }

    public void deselectAll() {
        for (BeatmapView beatmap : beatmapList.getItems())
            if (!beatmap.getInstalledProperty().get())
                beatmap.getQueueProperty().set(false);
    }

    private class HyperlinkCell implements Callback<TableColumn<BeatmapView, Hyperlink>, TableCell<BeatmapView, Hyperlink>> {
        @Override
        public TableCell<BeatmapView, Hyperlink> call(TableColumn<BeatmapView, Hyperlink> args) {
            return new TableCell<BeatmapView, Hyperlink>() {
                @Override
                protected void updateItem(Hyperlink item, boolean empty) {
                    setGraphic(item);
                }
            };
        }
    }
}
