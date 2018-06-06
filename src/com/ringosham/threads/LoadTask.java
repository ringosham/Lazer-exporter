package com.ringosham.threads;

import com.ringosham.locale.Localizer;
import com.ringosham.objects.Global;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class LoadTask extends Task<Void> {
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
            String beatmapinfoSql = "SELECT BeatmapSetInfoID, MetadataID, OnlineBeatmapID FROM BeatmapInfo";
            String beatmapsetfileinfoSql = "SELECT BeatmapSetInfoID, FileInfoID, Filename FROM BeatmapSetFileInfo";
            String fileinfoSql = "SELECT ID, Hash FROM FileInfo";
            String beatmapmetadataSql = "SELECT ID, Artist, ArtistUnicode, Title, TitleUnicode FROM BeatmapMetadata";
            Statement beatmapinfoStatement = connection.createStatement();
            Statement beatmapsetfileinfoStatement = connection.createStatement();
            Statement fileinfoStatement = connection.createStatement();
            Statement beatmapmetadataStatement = connection.createStatement();
            ResultSet beatmapInfo = beatmapinfoStatement.executeQuery(beatmapinfoSql);
            ResultSet beatmapSetFileInfo = beatmapsetfileinfoStatement.executeQuery(beatmapsetfileinfoSql);
            ResultSet fileInfo = fileinfoStatement.executeQuery(fileinfoSql);
            ResultSet beatmapMetadata = beatmapmetadataStatement.executeQuery(beatmapmetadataSql);

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
