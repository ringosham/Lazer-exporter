package com.ringosham.controller;

import com.ringosham.objects.Global;
import com.ringosham.threads.imports.Downloader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Login {
    private MainScreen mainScreen;
    private Stage currentStage;

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
        currentStage = loginStage;
    }

    public void initialize() {
        if (Global.INSTANCE.getUsername() != null && Global.INSTANCE.getPassword() != null) {
            email.setText(Global.INSTANCE.getUsername());
            password.setText(Global.INSTANCE.getPassword());
            startLoginTask();
        }
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

    private void startLoginTask() {
        Downloader downloader = new Downloader(mainScreen, currentStage, loginButton, email.getText(), password.getText());
        loginButton.setDisable(true);
        Thread thread = new Thread(downloader);
        thread.setDaemon(true);
        thread.start();
    }
}
