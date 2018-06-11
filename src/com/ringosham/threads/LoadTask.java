package com.ringosham.threads;

import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import com.ringosham.objects.Global;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoadTask extends Task<Void> {
    private Stage stage;

    public LoadTask(Stage stage) {
        this.stage = stage;
    }

    @Override
    protected Void call() {
        updateTitle("Loading configs...");
        try {
            Global.INSTANCE.loadConfig();
        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Failed loading config file");
                alert.setContentText("Failed to load configuration file. Defaults will be used instead");
                alert.show();
            });
            e.printStackTrace();
            Global.INSTANCE.configFailsafe();
        }
        updateTitle(Localizer.getLocalizedText("checkInstall"));
        if (!Global.INSTANCE.getLazerDirectory().exists()) {
            selectInstallDirectory();
        } else if (!new File(Global.INSTANCE.getDatabaseAbsolutePath()).exists()) {
            selectInstallDirectory();
        }
        updateTitle(Localizer.getLocalizedText("parsingDb"));
        final String jdbcUrl = "jdbc:sqlite:";
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl + Global.INSTANCE.getDatabaseAbsolutePath());
            String beatmapsetInfoSql = "SELECT ID, OnlineBeatmapSetID, MetadataID FROM BeatmapSetInfo ORDER BY ID";
            String fileInfoSql = "SELECT BeatmapSetInfoID, Filename, FileInfo.Hash" +
                    " FROM BeatmapSetFileInfo JOIN FileInfo ON BeatmapSetFileInfo.FileInfoID = FileInfo.ID";
            String metadataSql = "SELECT ID, Artist, ArtistUnicode, Title, TitleUnicode FROM BeatmapMetadata";
            Statement beatmapsetInfoStatement = connection.createStatement();
            Statement fileInfoStatement = connection.createStatement();
            Statement metadataStatement = connection.createStatement();
            ResultSet beatmapSetInfoSet = beatmapsetInfoStatement.executeQuery(beatmapsetInfoSql);
            ResultSet fileInfoSet = fileInfoStatement.executeQuery(fileInfoSql);
            ResultSet beatmapMetadataSet = metadataStatement.executeQuery(metadataSql);
            //Extract ResultSets to Java objects as SQLite only supports forward only cursors.
            List<List<String>> beatmapSetInfo = new ArrayList<List<String>>() {{
                for (int i = 0; i < 3; i++)
                    add(new ArrayList<>());
            }};
            List<List<String>> fileInfo = new ArrayList<List<String>>() {{
                for (int i = 0; i < 3; i++)
                    add(new ArrayList<>());
            }};
            List<List<String>> beatmapMetadata = new ArrayList<List<String>>() {{
                for (int i = 0; i < 5; i++)
                    add(new ArrayList<>());
            }};
            while (beatmapSetInfoSet.next()) {
                beatmapSetInfo.get(0).add(beatmapSetInfoSet.getString(1));
                beatmapSetInfo.get(1).add(beatmapSetInfoSet.getString(2));
                beatmapSetInfo.get(2).add(beatmapSetInfoSet.getString(3));
            }
            while (fileInfoSet.next()) {
                fileInfo.get(0).add(fileInfoSet.getString(1));
                fileInfo.get(1).add(fileInfoSet.getString(2));
                fileInfo.get(2).add(fileInfoSet.getString(3));
            }
            while (beatmapMetadataSet.next()) {
                beatmapMetadata.get(0).add(beatmapMetadataSet.getString(1));
                beatmapMetadata.get(1).add(beatmapMetadataSet.getString(2));
                beatmapMetadata.get(2).add(beatmapMetadataSet.getString(3));
                beatmapMetadata.get(3).add(beatmapMetadataSet.getString(4));
                beatmapMetadata.get(4).add(beatmapMetadataSet.getString(5));
            }
            connection.close();
            //Process the data and stored them as objects.
            //There is always one beatmap in the database (Circles by nekodex). So using index 0 is safe.
            boolean nullIDFound = false;
            for (int i = 0; i < beatmapSetInfo.get(0).size(); i++) {
                String beatmapID = beatmapSetInfo.get(0).get(i);
                String onlineString = beatmapSetInfo.get(1).get(i);
                if (onlineString == null) {
                    if (!nullIDFound) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle(Localizer.getLocalizedText("nullID"));
                            alert.setHeaderText(Localizer.getLocalizedText("nullIDHead"));
                            alert.setContentText(Localizer.getLocalizedText("nullIDDesc"));
                            alert.show();
                        });
                        nullIDFound = true;
                    }
                    continue;
                }
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
                for (int j = 0; j < beatmapMetadata.get(0).size(); j++) {
                    if (metadataID.equals(beatmapMetadata.get(0).get(j))) {
                        title = beatmapMetadata.get(3).get(j);
                        unicodeTitle = beatmapMetadata.get(4).get(j);
                        artist = beatmapMetadata.get(1).get(j);
                        unicodeArtist = beatmapMetadata.get(2).get(j);
                        break;
                    }
                }
                Global.INSTANCE.beatmapList.add(new Beatmap(onlineID, artist, title, unicodeArtist, unicodeTitle, fileMap));
            }
        } catch (SQLException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(Localizer.getLocalizedText("databaseFail"));
                alert.setContentText(Localizer.getLocalizedText("databaseFailDesc"));
                alert.showAndWait();
                Platform.exit();
            });
            e.printStackTrace();
            return null;
        }
        updateTitle(Localizer.getLocalizedText("appTitle"));
        Platform.runLater(() -> {
            stage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/mainScreen.fxml"), Localizer.getResourceBundle());
            try {
                Parent root = loader.load();
                stage.resizableProperty().setValue(true);
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
        while (Global.INSTANCE.getLazerDirectory() == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(Localizer.getLocalizedText("notFoundTitle"));
                alert.setContentText(Localizer.getLocalizedText("notFoundDesc"));
                alert.showAndWait();
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle(Localizer.getLocalizedText("selectGameDir"));
                File dir = chooser.showDialog(null);
                if (new File(dir, "client.db").exists()) {
                    Global.INSTANCE.setLazerDirectory(dir);
                    try {
                        Global.INSTANCE.saveConfig();
                    } catch (IOException e) {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle(Localizer.getLocalizedText("failedSaveConfig"));
                        error.setContentText(Localizer.getLocalizedText("failedSaveConfigDesc"));
                        error.showAndWait();
                        e.printStackTrace();
                    }
                }
                Alert alert1 = new Alert(Alert.AlertType.ERROR);
                alert1.setTitle(Localizer.getLocalizedText("dirInvalid"));
                alert1.setTitle(Localizer.getLocalizedText("dirInvalidDesc"));
                alert1.showAndWait();
            });
        }
    }
}
