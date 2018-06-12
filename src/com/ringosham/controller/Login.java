package com.ringosham.controller;

import javafx.stage.Stage;

public class Login {
    private MainScreen mainScreen;
    private Stage currentStage;

    public Login(MainScreen mainScreen, Stage loginStage) {
        this.mainScreen = mainScreen;
        currentStage = loginStage;
    }

    public void onLogin() {
        currentStage.close();
    }
}
