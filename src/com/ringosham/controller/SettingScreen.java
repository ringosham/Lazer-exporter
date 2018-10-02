/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.controller;

import com.ringosham.Global;
import com.ringosham.locale.Localizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class SettingScreen {
    private final ObservableList<Language> options = FXCollections.observableArrayList(
            new Language("English", Locale.US),
            new Language("中文(繁體)", Locale.TRADITIONAL_CHINESE)
    );
    @FXML
    private TextField gameDirectory;
    @FXML
    private ComboBox<Language> languageOption;
    @FXML
    private CheckBox videoDownload;
    @FXML
    private TextField gameExecutable;
    private boolean isLanguageChanged = false;
    private final MainScreen mainScreen;

    SettingScreen(MainScreen mainScreen) {
        this.mainScreen = mainScreen;
    }

    public void initialize() {
        gameDirectory.setText(Global.INSTANCE.getLazerDirectory().getAbsolutePath());
        languageOption.setItems(options);
        languageOption.setCellFactory(new Callback<ListView<Language>, ListCell<Language>>() {
            @Override
            public ListCell<Language> call(ListView<Language> param) {
                return new ListCell<Language>() {
                    @Override
                    protected void updateItem(Language lang, boolean empty) {
                        super.updateItem(lang, empty);
                        if (lang != null)
                            setText(lang.getName());
                        else
                            setText("");
                    }
                };
            }
        });
        languageOption.setConverter(new StringConverter<Language>() {
            @Override
            public String toString(Language lang) {
                return lang.getName();
            }

            @Override
            public Language fromString(String string) {
                for (Language lang : options)
                    if (lang.getName().equals(string))
                        return lang;
                return null;
            }
        });
        int selectedIndex = getIndexFromLocale(Global.INSTANCE.getLocale());
        languageOption.getSelectionModel().select(selectedIndex);
        languageOption.valueProperty().addListener((ObsValue, oldValue, newValue) -> isLanguageChanged = true);
        videoDownload.setSelected(Global.INSTANCE.isVideoDownload());
        gameExecutable.setText(Global.INSTANCE.getGameExecutable().getAbsolutePath());
    }

    private int getIndexFromLocale(Locale locale) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getLocale().toLanguageTag().equals(locale.toLanguageTag()))
                return i;
        }
        return -1;
    }

    public void close() {
        ((Stage) gameDirectory.getScene().getWindow()).close();
    }

    public void apply() {
        File gameDir = new File(gameDirectory.getText());
        if (!checkGameDir(gameDir))
            return;
        Global.INSTANCE.setLazerDirectory(new File(gameDirectory.getText()));
        Global.INSTANCE.setLocale(languageOption.getSelectionModel().getSelectedItem().getLocale());
        Global.INSTANCE.setVideoDownload(videoDownload.isSelected());
        Global.INSTANCE.setGameExecutable(new File(gameExecutable.getText()));
        try {
            Global.INSTANCE.saveConfig();
        } catch (IOException e) {
            Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("failedSaveConfig"),
                    Localizer.getLocalizedText("failedSaveConfigDesc"));
            e.printStackTrace();
        }
        if (isLanguageChanged)
            mainScreen.resync();
        else
            close();
    }

    public void changeGameDir() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(Localizer.getLocalizedText("selectGameDir"));
        File dir = chooser.showDialog(null);
        if (dir != null)
            checkGameDir(dir);
    }

    private boolean checkGameDir(File dir) {
        if (new File(dir, "client.db").exists()) {
            gameDirectory.setText(dir.getAbsolutePath());
            return true;
        } else {
            Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("dirInvalid"),
                    Localizer.getLocalizedText("dirInvalidDesc"));
            return false;
        }
    }

    public void changeGameExec() {
        String os = System.getProperty("os.name").toLowerCase();
        File game = null;
        if (os.contains("win") || os.contains("nix")) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(Localizer.getLocalizedText("selectGameExec"));
            if (os.contains("win"))
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("osu! Executable", "osu!.exe"));
            else
                //Assumes the executable in Arch linux AUR
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("osu! Executable", "osu-lazer"));
            game = chooser.showOpenDialog(null);
        } else {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(Localizer.getLocalizedText("selectGameExec"));
            while (game == null || !game.getName().equals("osu!.app")) {
                game = chooser.showDialog(null);
                if (game == null)
                    break;
                Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("invalidExec"), Localizer.getLocalizedText("invalidExecDesc"));
            }
        }
        if (game != null)
            gameExecutable.setText(game.getAbsolutePath());
    }

    private class Language {
        private final String name;
        private final Locale locale;

        Language(String name, Locale locale) {
            this.name = name;
            this.locale = locale;
        }

        String getName() {
            return name;
        }

        Locale getLocale() {
            return locale;
        }
    }
}
