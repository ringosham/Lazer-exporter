package com.ringosham.threads.imports;

import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.Beatmap;
import com.ringosham.objects.Global;
import com.ringosham.objects.xml.BeatmapListXML;
import com.ringosham.objects.xml.BeatmapXML;
import javafx.application.Platform;
import javafx.concurrent.Task;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
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
        updateMessage(Localizer.getLocalizedText("parsingXML"));
        BeatmapListXML xml;
        try {
            JAXBContext context = JAXBContext.newInstance(BeatmapListXML.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            xml = (BeatmapListXML) unmarshaller.unmarshal(importFile);
        } catch (JAXBException e) {
            updateMessage(Localizer.getLocalizedText("taskFinishWithFailure").replace("%FAILCOUNT%",
                    Integer.toString(1)));
            Platform.runLater(() -> {
                mainScreen.consoleArea.appendText(Localizer.getLocalizedText("failXML") + "\n");
                mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
            });
            finish();
            e.printStackTrace();
            return null;
        }
        updateMessage(Localizer.getLocalizedText("processingXML"));
        List<BeatmapXML> list = xml.getBeatmaps();
        list.sort((o1, o2) -> {
            if (isBeatmapInstalled(o1.getBeatmapID()) && !isBeatmapInstalled(o2.getBeatmapID()))
                return -1;
            else if (!isBeatmapInstalled(o1.getBeatmapID()) && isBeatmapInstalled(o2.getBeatmapID()))
                return 1;
            else {
                return Integer.compare(o1.getBeatmapID(), o2.getBeatmapID());
            }
        });

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
        Platform.runLater(mainScreen::enableButtons);
        Global.INSTANCE.inProgress = false;
    }
}
