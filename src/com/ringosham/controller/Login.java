/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.controller;

import com.ringosham.Global;
import com.ringosham.threads.imports.download.BeatmapImport;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Login {
    private final MainScreen mainScreen;
    private final Stage currentStage;

    //Standard RFC 3522 email regex.
    private static final String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";
    @FXML
    private Button loginButton;
    @FXML
    private TextField email;
    @FXML
    private PasswordField password;

    public Login(MainScreen mainScreen, Stage loginStage) {
        this.mainScreen = mainScreen;
        this.currentStage = loginStage;
    }

    public void initialize() {
        if (Global.INSTANCE.getEmail() != null && Global.INSTANCE.getPassword() != null) {
            email.setText(Global.INSTANCE.getEmail());
            password.setText(Global.INSTANCE.getPassword());
            startLoginTask();
        }
        password.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                onLogin();
        });
    }

    public void onLogin() {
        if (!email.getText().matches(emailRegex)) {
            email.setStyle("-fx-text-box-boarder: red; -fx-focus-color: red");
            email.requestFocus();
            return;
        }
        //Passwords must be at least 8 characters long. Stated in the website
        if (password.getText().trim().length() < 8) {
            password.setStyle("-fx-text-box-boarder: red; -fx-focus-color: red");
            password.requestFocus();
            return;
        }
        startLoginTask();
    }

    private void disableElements() {
        loginButton.setDisable(true);
        email.setDisable(true);
        password.setDisable(true);
    }

    public void enableElements() {
        loginButton.setDisable(false);
        email.setDisable(false);
        password.setDisable(false);
    }

    private void startLoginTask() {
        BeatmapImport beatmapImport = new BeatmapImport(mainScreen, currentStage, this, email.getText(), password.getText());
        disableElements();
        Thread thread = new Thread(beatmapImport);
        thread.setDaemon(true);
        thread.start();
    }
}
