package com.ringosham.threads;

import com.ringosham.locale.Localizer;
import com.ringosham.objects.Global;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.IOException;

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
        updateTitle(Localizer.getLocalizedText("parsingDb"));
        return null;
    }
}
