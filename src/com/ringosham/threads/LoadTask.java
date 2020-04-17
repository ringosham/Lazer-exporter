/*
 * Copyright (c) 2020. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads;

import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import com.ringosham.objects.Metadata;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoadTask extends Task<Void> {
    private final Stage stage;

    public LoadTask(Stage stage) {
        this.stage = stage;
    }

    @Override
    protected Void call() {
        Global.INSTANCE.beatmapList.clear();
        updateTitle("Loading configs...");
        try {
            Global.INSTANCE.loadConfig();
        } catch (IOException e) {
            Global.INSTANCE.showAlert(Alert.AlertType.ERROR, "Failed loading config file",
                    "Failed to load configuration file. Defaults will be used instead");
            e.printStackTrace();
            Global.INSTANCE.configFailsafe();
        }
        updateTitle(Localizer.getLocalizedText("init.checkInstall"));
        if (!Global.INSTANCE.getLazerDirectory().exists() || !Global.INSTANCE.getGameExecutable().exists()) {
            selectInstallDirectory();
        } else if (!new File(Global.INSTANCE.getDatabaseAbsolutePath()).exists()) {
            selectInstallDirectory();
        }
        updateTitle(Localizer.getLocalizedText("init.parsingDb"));
        final String jdbcUrl = "jdbc:sqlite:";
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl + Global.INSTANCE.getDatabaseAbsolutePath());
            String beatmapsetInfoSql = "SELECT ID, OnlineBeatmapSetID, MetadataID, Status, DeletePending FROM BeatmapSetInfo ORDER BY ID";
            String fileInfoSql = "SELECT BeatmapSetInfoID, Filename, FileInfo.Hash" +
                    " FROM BeatmapSetFileInfo JOIN FileInfo ON BeatmapSetFileInfo.FileInfoID = FileInfo.ID";
            String metadataSql = "SELECT ID, Artist, ArtistUnicode, Title, TitleUnicode, AudioFile, BackgroundFile FROM BeatmapMetadata";
            Statement beatmapsetInfoStatement = connection.createStatement();
            Statement fileInfoStatement = connection.createStatement();
            Statement metadataStatement = connection.createStatement();
            ResultSet beatmapSetInfoSet = beatmapsetInfoStatement.executeQuery(beatmapsetInfoSql);
            ResultSet fileInfoSet = fileInfoStatement.executeQuery(fileInfoSql);
            ResultSet beatmapMetadataSet = metadataStatement.executeQuery(metadataSql);
            //Extract ResultSets to Java objects as SQLite only supports forward only cursors.
            List<List<String>> beatmapSetInfo = new ArrayList<List<String>>() {{
                for (int i = 0; i < 5; i++)
                    add(new ArrayList<>());
            }};
            List<List<String>> fileInfo = new ArrayList<List<String>>() {{
                for (int i = 0; i < 3; i++)
                    add(new ArrayList<>());
            }};
            List<List<String>> beatmapMetadata = new ArrayList<List<String>>() {{
                for (int i = 0; i < 7; i++)
                    add(new ArrayList<>());
            }};
            /*
                The Filename and AudioFile name may have different capitalization
                While this has no effect as the files are supposed to be in Windows, whereas file names are case-insensitive
                in NTFS, this may affect users in macOS and Linux.
                Filename refers to the actual file. While AudioFile is an attribute from the beatmap file.
                Both needed to be lower cased as a result.
                The game itself looks up and compare file names while ignoring case, so it has no problems.
             */
            while (beatmapSetInfoSet.next()) {
                beatmapSetInfo.get(0).add(beatmapSetInfoSet.getString(1));
                beatmapSetInfo.get(1).add(beatmapSetInfoSet.getString(2));
                beatmapSetInfo.get(2).add(beatmapSetInfoSet.getString(3));
                beatmapSetInfo.get(3).add(beatmapSetInfoSet.getString(4));
                beatmapSetInfo.get(4).add(beatmapSetInfoSet.getString(5));
            }
            while (fileInfoSet.next()) {
                fileInfo.get(0).add(fileInfoSet.getString(1));
                fileInfo.get(1).add(fileInfoSet.getString(2).toLowerCase());
                fileInfo.get(2).add(fileInfoSet.getString(3));
            }
            while (beatmapMetadataSet.next()) {
                beatmapMetadata.get(0).add(beatmapMetadataSet.getString(1));
                beatmapMetadata.get(1).add(beatmapMetadataSet.getString(2));
                beatmapMetadata.get(2).add(beatmapMetadataSet.getString(3));
                beatmapMetadata.get(3).add(beatmapMetadataSet.getString(4));
                beatmapMetadata.get(4).add(beatmapMetadataSet.getString(5));
                beatmapMetadata.get(5).add(beatmapMetadataSet.getString(6).toLowerCase());
                String coverArt = beatmapMetadataSet.getString(7);
                if (coverArt != null)
                    coverArt = coverArt.toLowerCase();
                beatmapMetadata.get(6).add(coverArt);
            }
            connection.close();
            //Process the data and stored them as objects.
            //There is always at least one beatmap in the database (Circles by nekodex). So using index 0 is safe.
            boolean nullIDFound = false;
            for (int i = 0; i < beatmapSetInfo.get(0).size(); i++) {
                String beatmapID = beatmapSetInfo.get(0).get(i);
                String onlineString = beatmapSetInfo.get(1).get(i);
                int beatmapStatus = Integer.parseInt(beatmapSetInfo.get(3).get(i));
                String deletePending = beatmapSetInfo.get(4).get(i);
                //Check if there are any beatmaps that doesn't have a online ID
                //4 means loved
                //3 means qualified
                //2 probably means approved (Haven't tested)
                //1 means ranked
                //0 means pending
                //-2 means graveyard
                //-3 means built-in (Like Triangles and Circles)
                if (onlineString == null && beatmapStatus != -3) {
                    if (!nullIDFound) {
                        Global.INSTANCE.showAlert(Alert.AlertType.WARNING, Localizer.getLocalizedText("dialog.warn.nullID"),
                                Localizer.getLocalizedText("dialog.warn.nullIDHead"), Localizer.getLocalizedText("dialog.warn.nullIDDesc"));
                        nullIDFound = true;
                    }
                    continue;
                }
                //Skip any beatmaps that are built-in (Status -3) and deleted
                //osu! deletes the beatmaps after you close the game, but it does not delete the entry in the database
                if (beatmapStatus == -3)
                    continue;
                if (deletePending.equals("1"))
                    continue;
                //Get online beatmap ID
                int onlineID = Integer.parseInt(onlineString);
                String metadataID = beatmapSetInfo.get(2).get(i);
                HashMap<String, String> fileMap = new HashMap<>();
                for (int j = 0; j < fileInfo.get(0).size(); j++)
                    if (beatmapID.equals(fileInfo.get(0).get(j)))
                        fileMap.put(fileInfo.get(1).get(j), fileInfo.get(2).get(j));
                String title = null;
                String unicodeTitle = null;
                String artist = null;
                String unicodeArtist = null;
                String audioFilename = null;
                String backgroundFilename = null;
                for (int j = 0; j < beatmapMetadata.get(0).size(); j++) {
                    if (metadataID.equals(beatmapMetadata.get(0).get(j))) {
                        title = beatmapMetadata.get(3).get(j);
                        unicodeTitle = beatmapMetadata.get(4).get(j);
                        artist = beatmapMetadata.get(1).get(j);
                        unicodeArtist = beatmapMetadata.get(2).get(j);
                        audioFilename = beatmapMetadata.get(5).get(j);
                        backgroundFilename = beatmapMetadata.get(6).get(j);
                        break;
                    }
                }
                Metadata metadata = new Metadata(artist, title, unicodeArtist, unicodeTitle, audioFilename, backgroundFilename);
                Global.INSTANCE.beatmapList.add(new Beatmap(onlineID, metadata, fileMap));
            }
        } catch (SQLException e) {
            Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("dialog.error.databaseFail"),
                    Localizer.getLocalizedText("dialog.error.databaseFailDesc"));
            e.printStackTrace();
            return null;
        }
        updateTitle("Lazer exporter");
        Platform.runLater(() -> {
            stage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ringosham/fxml/mainScreen.fxml"), Localizer.getResourceBundle());
            loader.setController(new MainScreen());
            try {
                Parent root = loader.load();
                stage.resizableProperty().setValue(true);
                stage.setMinWidth(550);
                stage.setMinHeight(500);
                stage.setWidth(800);
                stage.setHeight(700);
                MainScreen controller = loader.getController();
                stage.setOnCloseRequest(e -> {
                    e.consume();
                    controller.exit();
                });
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return null;
    }

    private void selectInstallDirectory() {
        Global.INSTANCE.setLazerDirectory(null);
        AtomicBoolean setup = new AtomicBoolean(false);
        Platform.runLater(() -> {
            Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("dialog.init.notFoundTitle"),
                    Localizer.getLocalizedText("dialog.init.notFoundDesc"));
            while (Global.INSTANCE.getLazerDirectory() == null) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle(Localizer.getLocalizedText("dialog.init.selectGameDir"));
                File dir = chooser.showDialog(null);
                if (dir == null)
                    System.exit(0);
                if (new File(dir, "client.db").exists()) {
                    Global.INSTANCE.setLazerDirectory(dir);
                    try {
                        Global.INSTANCE.saveConfig();
                    } catch (IOException e) {
                        Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("dialog.error.failedSaveConfig"),
                                Localizer.getLocalizedText("dialog.error.failedSaveConfigDesc"));
                        e.printStackTrace();
                    }
                } else
                    Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("dialog.init.dirInvalid"),
                            Localizer.getLocalizedText("dialog.init.dirInvalidDesc"));
            }
            String os = System.getProperty("os.name").toLowerCase();
            File game = null;
            if (os.contains("win") || os.equals("linux")) {
                FileChooser chooser = new FileChooser();
                chooser.setTitle(Localizer.getLocalizedText("dialog.init.selectGameExec"));
                if (os.contains("win"))
                    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("osu! Executable", "osu!.exe"));
                else
                    chooser.setInitialDirectory(new File("/usr/bin"));
                game = chooser.showOpenDialog(null);
                if (game == null)
                    System.exit(0);
            } else {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle(Localizer.getLocalizedText("dialog.init.selectGameExec"));
                while (game == null || !game.getName().equals("osu!.app")) {
                    game = chooser.showDialog(null);
                    if (game == null)
                        System.exit(0);
                    Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("dialog.error.invalidExec"), Localizer.getLocalizedText("dialog.error.invalidExecDesc"));
                }
            }
            Global.INSTANCE.setGameExecutable(game);
            try {
                Global.INSTANCE.saveConfig();
            } catch (IOException e) {
                Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("dialog.error.failedSaveConfig"),
                        Localizer.getLocalizedText("dialog.error.failedSaveConfig"), Localizer.getLocalizedText("dialog.error.failedSaveConfigDesc"));
                e.printStackTrace();
            }
            setup.set(true);
        });
        while (!setup.get()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
