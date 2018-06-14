package com.ringosham.controller;

import com.ringosham.locale.Localizer;
import com.ringosham.objects.ExportSettings;
import com.ringosham.objects.Global;
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

    private MainScreen mainScreen;
    private Stage currentStage;

    SongExportScreen(MainScreen mainScreen, Stage exportStage) {
        this.mainScreen = mainScreen;
        currentStage = exportStage;
    }

    @FXML
    public void initialize() {
        //UI initialization
        String convertTooltip = Localizer.getLocalizedText("convertTooltip");
        String addTagTooltip = Localizer.getLocalizedText("applyTagsTooltip");
        String overrideTooltip = Localizer.getLocalizedText("overwriteTagTooltip");
        String useIDTooltip = Localizer.getLocalizedText("renameIDTooltip");
        String renameTooltip = Localizer.getLocalizedText("renameTooltip");
        String practiseTooltip = Localizer.getLocalizedText("practiceTooltip");
        String filterTooltip = Localizer.getLocalizedText("filterTooltip");
        String overwriteTooltip = Localizer.getLocalizedText("overwriteFileTooltip");
        String romajiTooltip = Localizer.getLocalizedText("romajiTooltip");
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
        filterDuplicates.setSelected(true);
        filterSeconds.setText("10");
        filterSeconds.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 2 || !newValue.matches("\\d*") || newValue.equals("0"))
                filterSeconds.setText(oldValue);
        });
    }

    @FXML
    public void onExportClick() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(Localizer.getLocalizedText("chooseExportDir"));
        File exportDirectory = chooser.showDialog(null);
        if (exportDirectory == null)
            return;
        ExportSettings settings = new ExportSettings(convertCheckbox.isSelected(), filterPractice.isSelected(), overwriteCheckbox.isSelected(),
                addTags.isSelected(), overrideTags.isSelected(),
                ((RadioButton) renameOptions.getSelectedToggle()).getText().equals(Localizer.getLocalizedText("renameBeatmap")),
                filterDuplicates.isSelected(), romajiNaming.isSelected(), Integer.parseInt(filterSeconds.getText()), exportDirectory);
        SongExport export = new SongExport(mainScreen, settings);
        mainScreen.statusText.textProperty().bind(export.messageProperty());
        Thread thread = new Thread(export);
        thread.setDaemon(true);
        Global.INSTANCE.inProgress = true;
        thread.start();
        currentStage.close();
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
        } else
            filterSeconds.setDisable(false);
    }
}
