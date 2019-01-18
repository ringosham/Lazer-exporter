/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.controller;

import com.ringosham.Global;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.ExportSettings;
import com.ringosham.threads.export.song.SongExport;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class SongExportScreen {

    @FXML
    private CheckBox convertCheckbox;
    @FXML
    private CheckBox filterPractice;
    @FXML
    private CheckBox overwriteCheckbox;
    @FXML
    private CheckBox addTags;
    @FXML
    private CheckBox overrideTags;
    @FXML
    private ToggleGroup renameOptions;
    @FXML
    private RadioButton renameBeatmap;
    @FXML
    private RadioButton useBeatmapID;
    @FXML
    private CheckBox filterDuplicates;
    @FXML
    private TextField filterSeconds;
    @FXML
    private CheckBox romajiNaming;

    private final MainScreen mainScreen;
    private final Stage currentStage;

    SongExportScreen(MainScreen mainScreen, Stage exportStage) {
        this.mainScreen = mainScreen;
        currentStage = exportStage;
    }

    @FXML
    public void initialize() {
        //UI initialization
        String convertTooltip = Localizer.getLocalizedText("export.tooltip.convert");
        String addTagTooltip = Localizer.getLocalizedText("export.tooltip.applyTags");
        String overrideTooltip = Localizer.getLocalizedText("export.tooltip.overwriteTag");
        String useIDTooltip = Localizer.getLocalizedText("export.tooltip.renameID");
        String renameTooltip = Localizer.getLocalizedText("export.tooltip.rename");
        String practiseTooltip = Localizer.getLocalizedText("export.tooltip.practice");
        String filterTooltip = Localizer.getLocalizedText("export.tooltip.filter");
        String overwriteTooltip = Localizer.getLocalizedText("export.tooltip.overwriteFile");
        String romajiTooltip = Localizer.getLocalizedText("export.tooltip.romaji");
        convertCheckbox.setTooltip(new Tooltip(convertTooltip));
        overrideTags.setTooltip(new Tooltip(overrideTooltip));
        useBeatmapID.setTooltip(new Tooltip(useIDTooltip));
        filterPractice.setTooltip(new Tooltip(practiseTooltip));
        filterDuplicates.setTooltip(new Tooltip(filterTooltip));
        renameBeatmap.setTooltip(new Tooltip(renameTooltip));
        addTags.setTooltip(new Tooltip(addTagTooltip));
        overwriteCheckbox.setTooltip(new Tooltip(overwriteTooltip));
        romajiNaming.setTooltip(new Tooltip(romajiTooltip));
        overrideTags.setDisable(true);
        filterPractice.setSelected(true);
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            //macOS cannot run the FFmpeg included. These options needed to be disabled.
            convertCheckbox.setDisable(true);
            filterDuplicates.setDisable(true);
            filterSeconds.setDisable(true);
            useBeatmapID.setSelected(true);
            useBeatmapID.setDisable(true);
            renameBeatmap.setDisable(true);
        } else {
            filterDuplicates.setSelected(true);
            filterSeconds.setText("10");
            filterSeconds.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 2 || !newValue.matches("\\d*") || newValue.equals("0"))
                    filterSeconds.setText(oldValue);
            });
        }
    }

    @FXML
    public void onExportClick() {
        if (filterDuplicates.isSelected() && filterSeconds.getText().trim().isEmpty()) {
            filterSeconds.requestFocus();
            filterSeconds.setStyle("-fx-text-box-boarder: red; -fx-focus-color: red");
            return;
        }
        int seconds = 0;
        if (filterDuplicates.isSelected())
            seconds = Integer.parseInt(filterSeconds.getText());
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(Localizer.getLocalizedText("dialog.export.chooseExportDir"));
        File exportDirectory = chooser.showDialog(null);
        if (exportDirectory == null)
            return;
        ExportSettings settings = new ExportSettings(convertCheckbox.isSelected(), filterPractice.isSelected(), overwriteCheckbox.isSelected(),
                addTags.isSelected(), overrideTags.isSelected(),
                ((RadioButton) renameOptions.getSelectedToggle()).getText().equals(Localizer.getLocalizedText("export.option.renameBeatmap")),
                filterDuplicates.isSelected(), romajiNaming.isSelected(), seconds, exportDirectory);
        SongExport export = new SongExport(mainScreen, settings);
        Thread thread = new Thread(export);
        thread.setDaemon(true);
        Global.INSTANCE.inProgress = true;
        thread.start();
        currentStage.close();
        mainScreen.disableButtons();
    }

    @FXML
    public void onAddTagChecked() {
        if (!addTags.isSelected()) {
            overrideTags.setDisable(true);
            overrideTags.setSelected(false);
        } else
            overrideTags.setDisable(false);
    }

    @FXML
    public void onFilterDuplicatesChecked() {
        if (!filterDuplicates.isSelected()) {
            filterSeconds.setDisable(true);
            filterSeconds.setText("");
            useBeatmapID.setSelected(true);
            useBeatmapID.setDisable(true);
            renameBeatmap.setDisable(true);
        } else {
            filterSeconds.setDisable(false);
            useBeatmapID.setDisable(false);
            renameBeatmap.setDisable(false);
        }
    }

    @FXML
    public void onRenameID() {
        if (useBeatmapID.isSelected()) {
            romajiNaming.setSelected(true);
            romajiNaming.setDisable(true);
        } else {
            romajiNaming.setDisable(false);
        }
    }
}
