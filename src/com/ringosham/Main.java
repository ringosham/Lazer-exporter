/*
 * Copyright (c) 2018. Ringosham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

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
        TODO UI Design (Remember to restrict maximize and minimize buttons in sub windows)
     */

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/loading.fxml"));
        //Need to pass HostServices around all threads just to make hyperlinks. What the hell JavaFX.
        loader.setController(new Loading(primaryStage, getHostServices()));
        Parent root = loader.load();
        primaryStage.resizableProperty().setValue(false);
        primaryStage.setScene(new Scene(root, 400, 15));
        primaryStage.show();
    }
}
