package com.ringosham;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    /*
        TODOs
        TODO Load schemas
        TODO Properties (Config files)
        TODO Translation
        TODO Export as beatmap list (XML encode in base64)
        TODO Import beatmap list
        TODO Download beatmaps to lazer (The game hasn't supported yet. This will be placeholder)
        TODO Export as beatmaps
        TODO Export songs (Same functionality as osz song exporter)
        TODO UI Design (Remember to restrict maximize and minimize buttons in sub windows)
     */

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml/mainScreen.fxml"));
        primaryStage.setTitle("Parsing database. Please wait...");
        //primaryStage.resizableProperty().setValue(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
