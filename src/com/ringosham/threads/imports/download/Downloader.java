/*
 * Copyright (c) 2020. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.imports.download;

import com.ringosham.Global;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.xml.BeatmapXML;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class Downloader {
    private static final String downloadUrlPrefix = "https://osu.ppy.sh/beatmapsets/";
    private static final String downloadUrlSuffix = "/download";
    private static final String downloadNoVidUrlSuffix = "/download?noVideo=1";
    private final MainScreen mainScreen;
    private final List<BeatmapXML> beatmaps;

    private int failCount = 0;

    Downloader(MainScreen mainScreen, List<BeatmapXML> beatmaps) {
        this.mainScreen = mainScreen;
        this.beatmaps = beatmaps;
    }

    void downloadTask() {
        int progress = 0;
        Platform.runLater(() -> mainScreen.mainProgress.setProgress(0));
        for (BeatmapXML beatmap : beatmaps) {
            String filename = beatmap.getBeatmapID() + " " + beatmap.getArtist() + " - " + beatmap.getTitle() + ".osz";
            File osz = new File(Global.INSTANCE.getLazerDirectory(), "/files/" + Global.INSTANCE.getValidFileName(filename));
            if (!osz.exists()) {
                try {
                    URL url;
                    if (Global.INSTANCE.isVideoDownload())
                        url = new URL(downloadUrlPrefix + beatmap.getBeatmapID() + downloadUrlSuffix);
                    else
                        url = new URL(downloadUrlPrefix + beatmap.getBeatmapID() + downloadNoVidUrlSuffix);
                    boolean downloadSuccess = false;
                    while (!downloadSuccess)
                        downloadSuccess = downloadBeatmap(osz, url, beatmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            progress++;
            int finalProgress = progress;
            Platform.runLater(() -> mainScreen.mainProgress.setProgress((double) finalProgress / beatmaps.size()));
        }
        Platform.runLater(() -> {
            if (failCount == 0)
                mainScreen.statusText.setText(Localizer.getLocalizedText("status.success"));
            else
                mainScreen.statusText.setText(Localizer.getLocalizedText("status.finishWithFailure")
                        .replace("%FAILCOUNT%", Integer.toString(failCount)));
            mainScreen.mainProgress.setProgress(0);
            mainScreen.subProgress.setProgress(0);
            ButtonType ok = new ButtonType(Localizer.getLocalizedText("dialog.common.yes"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel = new ButtonType(Localizer.getLocalizedText("dialog.common.no"), ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, Localizer.getLocalizedText("dialog.import.beatmapText"), ok, cancel);
            alert.setTitle(Localizer.getLocalizedText("dialog.import.beatmapTitle"));
            alert.setHeaderText(Localizer.getLocalizedText("dialog.import.beatmapHeader"));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.orElse(cancel) == ok) {
                File[] beatmaps = new File(Global.INSTANCE.getLazerDirectory(), "files").listFiles((dir, name) -> name.endsWith(".osz"));
                List<String> command = new ArrayList<>();
                command.add(Global.INSTANCE.getGameExecutable().getAbsolutePath());
                List<String> paths = beatmaps == null ? new ArrayList<>() : Arrays.stream(beatmaps).map(File::getAbsolutePath).collect(Collectors.toList());
                command.addAll(paths);
                ProcessBuilder builder = new ProcessBuilder(command);
                try {
                    builder.start();
                } catch (IOException ignored) {
                }
            } else {
                Alert saveAlert = new Alert(Alert.AlertType.INFORMATION);
                saveAlert.setTitle(Localizer.getLocalizedText("dialog.import.saveTitle"));
                saveAlert.setHeaderText(Localizer.getLocalizedText("dialog.import.saveText"));
                saveAlert.setContentText(new File(Global.INSTANCE.getLazerDirectory(), "files").getAbsolutePath());
                ((Button) (saveAlert.getDialogPane().lookupButton(ButtonType.OK))).setText(Localizer.getLocalizedText("dialog.common.ok"));
                saveAlert.showAndWait();
            }
        });
    }

    private boolean downloadBeatmap(File osz, URL url, BeatmapXML beatmap) {
        String beatmapDisplay = beatmap.getBeatmapID() + " " + beatmap.getArtist() + " - " + beatmap.getTitle();
        Platform.runLater(() -> mainScreen.statusText.setText(Localizer.getLocalizedText("status.download.downloading")
                .replace("%BEATMAP%", beatmapDisplay)));
        try {
            if (osz.exists())
                Files.delete(osz.toPath());
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
            int responseCode = connection.getResponseCode();
            OutputStream out = new FileOutputStream(osz);
            InputStream in = connection.getInputStream();
            if (responseCode >= HttpsURLConnection.HTTP_BAD_REQUEST && responseCode != HttpsURLConnection.HTTP_NOT_FOUND) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder errorString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    errorString.append(line);
                if (errorString.toString().contains("slow down")) {
                    timeout();
                    in.close();
                    out.close();
                    return false;
                } else {
                    in.close();
                    out.close();
                    throw new IOException(errorString.toString());
                }
            } else if (responseCode == HttpsURLConnection.HTTP_NOT_FOUND) {
                in.close();
                out.close();
                throw new IOException(Localizer.getLocalizedText("download.notFound"));
            } else {
                final byte[] data = new byte[1024];
                long downloadedSize = 0;
                int count;
                //Check if osu! sent 0 bytes (Prevent user from using too much bandwidth)
                long timeoutStart = -1;
                long timeoutCurrent;
                while ((count = in.read(data, 0, 1024)) != -1) {
                    if (count == 0) {
                        if (timeoutStart == -1)
                            timeoutStart = System.currentTimeMillis();
                        timeoutCurrent = System.currentTimeMillis();
                        //If osu! has stop sending any data for 5 seconds
                        if (timeoutCurrent - timeoutStart >= 5000) {
                            timeout();
                            in.close();
                            out.close();
                            return false;
                        }
                    } else {
                        timeoutStart = -1;
                    }
                    downloadedSize += count;
                    long finalDownloadedSize = downloadedSize;
                    //Unknown file size would turn the progress bar indeterminate
                    Platform.runLater(() -> mainScreen.subProgress.setProgress((double) finalDownloadedSize / fileSize));
                    out.write(data, 0, count);
                }
                in.close();
                out.close();
            }
        } catch (IOException e) {
            failCount++;
            String error = Localizer.getLocalizedText("download.failDownload")
                    .replace("%BEATMAP%", beatmap.getBeatmapID() + " " + beatmap.getArtist() + " - " + beatmap.getTitle());
            mainScreen.consoleArea.appendText(error + "\n");
            mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        return true;
    }

    private void timeout() {
        int timeout = 30;
        while (timeout >= 0) {
            int finalTimeout = timeout;
            Platform.runLater(() -> {
                mainScreen.statusText.setText(Localizer.getLocalizedText("status.download.timeout")
                        .replace("%TIMEOUT%", String.valueOf(finalTimeout)));
                mainScreen.subProgress.setProgress(-1);
            });
            try {
                Thread.sleep(30000);
            } catch (InterruptedException ignored) {
            }
            timeout--;
        }
    }
}