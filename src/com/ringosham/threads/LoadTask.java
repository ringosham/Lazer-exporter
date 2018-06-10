package com.ringosham.threads;

import com.ringosham.controller.MainScreen;
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
import java.util.HashMap;

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
            ResultSet beatmapSetInfo = beatmapsetInfoStatement.executeQuery(beatmapsetInfoSql);
            ResultSet fileInfo = fileInfoStatement.executeQuery(fileInfoSql);
            ResultSet beatmapMetadata = metadataStatement.executeQuery(metadataSql);
            while (beatmapSetInfo.next()) {
                String beatmapID = beatmapSetInfo.getString(1);
                //Wait for issue #2758 to be merged and closed. Current column will read null values which crashes the thread
                int onlineID = Integer.parseInt(beatmapSetInfo.getString(2));
                String metadataID = beatmapSetInfo.getString(3);
                HashMap<String, String> fileMap = new HashMap<>();
                while (fileInfo.next())
                    if (beatmapID.equals(fileInfo.getString(1)))
                        fileMap.put(fileInfo.getString(2), fileInfo.getString(3));
                //Reset the pointer.
                fileInfo.beforeFirst();
                String title = null;
                String unicodeTitle = null;
                String artist = null;
                String unicodeArtist = null;
                while (beatmapMetadata.next()) {
                    if (metadataID.equals(beatmapMetadata.getString(1))) {
                        title = beatmapMetadata.getString(4);
                        unicodeTitle = beatmapMetadata.getString(5);
                        artist = beatmapMetadata.getString(2);
                        unicodeArtist = beatmapMetadata.getString(3);
                        break;
                    }
                }
                //Reset the pointer.
                beatmapMetadata.beforeFirst();
                Global.INSTANCE.beatmapList.add(new Beatmap(onlineID, artist, title, unicodeArtist, unicodeTitle, fileMap));
            }
            connection.close();
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
        Platform.runLater(() -> {
            stage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/mainScreen.fxml"));
            loader.setController(new MainScreen());
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
