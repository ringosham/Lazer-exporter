/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.export.list;

import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import com.ringosham.objects.xml.BeatmapListXML;
import com.ringosham.objects.xml.BeatmapXML;
import javafx.application.Platform;
import javafx.concurrent.Task;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;

public class ListExport extends Task<Void> {
    private MainScreen mainScreen;
    private File exportFile;

    public ListExport(MainScreen mainScreen, File exportFile) {
        this.mainScreen = mainScreen;
        this.exportFile = exportFile;
    }

    @Override
    protected Void call() {
        BeatmapListXML xml = new BeatmapListXML();
        xml.setBeatmaps(new ArrayList<>());
        updateMessage(Localizer.getLocalizedText("parsingDb"));
        int progress = 0;
        for (Beatmap beatmap : Global.INSTANCE.beatmapList) {
            //The ID -1 refers to the default beatmap (Circles by nekodex).
            //-1 is obviously invalid, so this is skipped.
            if (beatmap.getBeatmapId() == -1)
                continue;
            BeatmapXML element = new BeatmapXML();
            element.setBeatmapID(beatmap.getBeatmapId());
            element.setTitle(beatmap.getMetadata().getTitle());
            element.setArtist(beatmap.getMetadata().getArtist());
            xml.getBeatmaps().add(element);
            progress++;
            updateProgress(progress, Global.INSTANCE.beatmapList.size());
        }
        updateMessage(Localizer.getLocalizedText("writingFile"));
        try {
            JAXBContext context = JAXBContext.newInstance(BeatmapListXML.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xml, exportFile);
        } catch (JAXBException e) {
            updateMessage(Localizer.getLocalizedText("taskFinishWithFailure").replace("%FAILCOUNT%",
                    Integer.toString(1)));
            Platform.runLater(() -> {
                mainScreen.consoleArea.appendText(Localizer.getLocalizedText("failWriting") + "\n");
                mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
                mainScreen.enableButtons();
            });
            e.printStackTrace();
            Global.INSTANCE.inProgress = false;
            return null;
        }
        updateProgress(0, 0);
        updateMessage(Localizer.getLocalizedText("taskSuccess"));
        Platform.runLater(() -> mainScreen.enableButtons());
        Global.INSTANCE.inProgress = false;
        return null;
    }
}
