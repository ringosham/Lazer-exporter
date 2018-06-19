/*
 * Copyright (c) 2018. Ringosham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.imports.download;

import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.xml.BeatmapXML;
import javafx.application.Platform;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.List;

class Downloader {
    private static final String downloadUrlPrefix = "https://osu.ppy.sh/beatmapsets/";
    private static final String downloadUrlSuffix = "/download";
    private final MainScreen mainScreen;
    private final List<BeatmapXML> beatmaps;

    Downloader(MainScreen mainScreen, List<BeatmapXML> beatmaps) {
        this.mainScreen = mainScreen;
        this.beatmaps = beatmaps;
    }

    void downloadBeatmap() {
        int progress = 0;
        int failCount = 0;
        Platform.runLater(() -> mainScreen.mainProgress.setProgress(0));
        for (BeatmapXML beatmap : beatmaps) {
            String beatmapDisplay = beatmap.getBeatmapID() + " " + beatmap.getArtist() + " - " + beatmap.getTitle();
            Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("downloading")
                    .replace("%BEATMAP%", beatmapDisplay)));
            String filename = beatmap.getBeatmapID() + " " + beatmap.getArtist() + " - " + beatmap.getTitle() + ".osz";
            File osz = new File(Global.INSTANCE.getLazerDirectory(), "/files/" + getValidFileName(filename));
            if (!osz.exists()) {
                try {
                    URL url = new URL(downloadUrlPrefix + beatmap.getBeatmapID() + downloadUrlSuffix);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    //Header
                    connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                    connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
                    connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9,zh-TW;q=0.8,zh;q=0.7");
                    connection.setRequestProperty("Cookie", BeatmapImport.getCookieString());
                    connection.setRequestProperty("DNT", "1");
                    connection.setRequestProperty("Referer", downloadUrlPrefix + beatmap.getBeatmapID());
                    connection.setRequestProperty("Update-Insecure-Requests", "1");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");

                    connection.setDoOutput(true);
                    connection.setInstanceFollowRedirects(true);
                    connection.setUseCaches(false);

                    long fileSize = connection.getContentLengthLong();
                    OutputStream out = new FileOutputStream(osz);
                    InputStream in = connection.getInputStream();

                    final byte[] data = new byte[1024];
                    long downloadedSize = 0;
                    int count;
                    while ((count = in.read(data, 0, 1024)) != -1) {
                        downloadedSize += count;
                        long finalDownloadedSize = downloadedSize;
                        //Unknown file size would turn the progress bar indeterminate
                        Platform.runLater(() -> mainScreen.subProgress.setProgress((double) finalDownloadedSize / fileSize));
                        out.write(data, 0, count);
                    }
                    out.close();
                } catch (IOException e) {
                    failCount++;
                    String error = Localizer.getLocalizedText("failDownload")
                            .replace("%BEATMAP%", beatmap.getBeatmapID() + " " + beatmap.getArtist() + " - " + beatmap.getTitle());
                    mainScreen.consoleArea.appendText(error + "\n");
                    mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
                    e.printStackTrace();
                }
            }
            progress++;
            int finalProgress = progress;
            Platform.runLater(() -> mainScreen.mainProgress.setProgress((double) finalProgress / beatmaps.size()));
        }
        int finalFailCount = failCount;
        Platform.runLater(() -> {
            if (finalFailCount == 0)
                mainScreen.statusText.setText(Localizer.getLocalizedText("taskSuccess"));
            else
                mainScreen.statusText.setText(Localizer.getLocalizedText("taskFinishWithFailure")
                        .replace("%FAILCOUNT%", Integer.toString(finalFailCount)));
            mainScreen.mainProgress.setProgress(0);
            mainScreen.subProgress.setProgress(0);
        });
    }

    private String getValidFileName(String name) {
        return name.replaceAll("\\*", "").replaceAll("<", "").replaceAll(">", "")
                .replaceAll("\\|", "").replaceAll("\\?", "").replaceAll(":", "")
                .replaceAll("\"", "").replaceAll("\\\\", ",").replaceAll("/", ",");
    }
}
