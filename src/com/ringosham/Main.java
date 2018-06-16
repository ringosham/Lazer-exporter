package com.ringosham;

import com.ringosham.controller.Loading;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    /*
        TODOs
        TODO Properties (Config files)
        TODO Download beatmaps to lazer (The game hasn't supported yet. This will be placeholder)
        TODO Osu account login (Required for downloading beatmaps)
        TODO UI Design (Remember to restrict maximize and minimize buttons in sub windows)
     */

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/loading.fxml"));
        loader.setController(new Loading(primaryStage));
        Parent root = loader.load();
        primaryStage.resizableProperty().setValue(false);
        primaryStage.setScene(new Scene(root, 400, 15));
        primaryStage.show();
    }
}
