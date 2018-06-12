package com.ringosham.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

public class SongExportScreen {
    @FXML
    public Label progressText;
    @FXML
    public ProgressBar progress;
    @FXML
    public Button exportButton;
    @FXML
    public TextArea consoleArea;
    @FXML
    public Button oszExport;
    @FXML
    private FlowPane pane;
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

    SongExportScreen(MainScreen mainScreen) {
        this.mainScreen = mainScreen;
    }

    @FXML
    public void initialize() {
        //UI initialization
        String convertTooltip = "Some old beatmaps may use ogg files instead of mp3. Disabling this will ensure audio quality, " +
                "but your music player will likely not able to read ogg tags (Album arts and song info)";
        String addTagTooltip = "Automatically add mp3 tags based on beatmap info to the exported songs";
        String overrideTooltip = "Overrides the existing mp3 tag, in case you don't like it.";
        String useIDTooltip = "Includes the beatmap ID in the file name. Ensures there are no file conflicts, " +
                "but it will be harder to find full versions of the song when it is mixed with TV sizes";
        String renameTooltip = "The renamed file will have this following format: \"(Song name) - (Song author)\"\n" +
                "Full versions of the song will have \"(Full version)\" in the file name";
        String practiseTooltip = "Skips through any maps that are labelled as \"Stream practice\" and \"Jump practice\"";
        String filterTooltip = "The program will try to differentiate full length songs and TV size songs through the length of the song. " +
                "Highly recommended if you have a lot of beatmaps.";
        String overwriteTooltip = "Overwrite the file even if it already exists. Otherwise it will overwrite if the file sizes are different";
        String romajiTooltip = "Rename the song after romaji instead of its Japanese/other languages' name. This has no effect if \"Using beatmap ID\" is selected.";
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
