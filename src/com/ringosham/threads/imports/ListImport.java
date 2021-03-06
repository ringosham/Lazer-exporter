/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.imports;

import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import com.ringosham.objects.view.BeatmapView;
import com.ringosham.objects.xml.BeatmapListXML;
import com.ringosham.objects.xml.BeatmapXML;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListImport extends Task<Void> {
    private final MainScreen mainScreen;
    private final File importFile;

    public ListImport(MainScreen mainScreen, File importFile) {
        this.mainScreen = mainScreen;
        this.importFile = importFile;
    }

    @Override
    protected Void call() {
        updateMessage(Localizer.getLocalizedText("status.list.parsingXML"));
        BeatmapListXML xml;
        try {
            JAXBContext context = JAXBContext.newInstance(BeatmapListXML.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            xml = (BeatmapListXML) unmarshaller.unmarshal(importFile);
        } catch (JAXBException e) {
            updateMessage(Localizer.getLocalizedText("status.finishWithFailure").replace("%FAILCOUNT%",
                    Integer.toString(1)));
            Platform.runLater(() -> {
                mainScreen.consoleArea.appendText(Localizer.getLocalizedText("status.list.failXML") + "\n");
                mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
            });
            finish();
            e.printStackTrace();
            return null;
        }
        updateMessage(Localizer.getLocalizedText("status.list.processingXML"));
        List<BeatmapXML> list = xml.getBeatmaps();
        //Sanity checks. Marshaller converts all non-digits and decimals to 0.
        list.removeIf(beatmapXML -> beatmapXML.getBeatmapID() <= 0);
        //Preparing view.
        list.sort((o1, o2) -> {
            if (isBeatmapInstalled(o1.getBeatmapID()) && !isBeatmapInstalled(o2.getBeatmapID()))
                return 1;
            else if (!isBeatmapInstalled(o1.getBeatmapID()) && isBeatmapInstalled(o2.getBeatmapID()))
                return -1;
            else
                return Integer.compare(o1.getBeatmapID(), o2.getBeatmapID());
        });
        //Convert data to viewable objects for TableView
        List<BeatmapView> view = new ArrayList<>();
        for (BeatmapXML beatmap : list)
            view.add(new BeatmapView(isBeatmapInstalled(beatmap.getBeatmapID()), beatmap));
        Platform.runLater(() -> mainScreen.beatmapList.setItems(FXCollections.observableArrayList(view)));
        updateMessage(Localizer.getLocalizedText("status.success"));
        finish();
        return null;
    }

    private boolean isBeatmapInstalled(int beatmapID) {
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            if (beatmap.getBeatmapId() == beatmapID)
                return true;
        }
        return false;
    }

    private void finish() {
        Platform.runLater(mainScreen::enableAllButtons);
        Global.INSTANCE.inProgress = false;
    }
}
