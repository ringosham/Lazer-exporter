package com.ringosham.threads.imports.download;

import com.ringosham.controller.Login;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import com.ringosham.objects.Global;
import com.ringosham.objects.view.BeatmapView;
import com.ringosham.objects.xml.BeatmapXML;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

public class BeatmapImport extends Task<Void> {
    //Since the old website is being replaced, the new url will be used to authenticate.
    //This means no more /d/, /forum/ucp.php?mode=login
    private static final String homepage = "https://osu.ppy.sh/home";
    private final MainScreen mainScreen;
    private final Stage loginStage;
    private static CookieManager cookieManager = new CookieManager();
    private final String email;
    private final String password;
    private Login loginScreen;

    public BeatmapImport(MainScreen mainScreen, Stage loginStage, Login loginScreen, String email, String password) {
        this.mainScreen = mainScreen;
        this.loginStage = loginStage;
        this.loginScreen = loginScreen;
        this.email = email;
        this.password = password;
    }

    static void getUnauthCookies() throws IOException {
        URL url = new URL(homepage);
        URLConnection connection = url.openConnection();
        connection.connect();
        List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
        for (String cookie : cookies)
            cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
    }

    static String getCookieString() {
        cookieManager.getCookieStore();
        StringBuilder cookieString = new StringBuilder();
        for (HttpCookie httpCookie : cookieManager.getCookieStore().getCookies()) {
            cookieString.append(httpCookie.toString());
            cookieString.append("; ");
        }
        return cookieString.substring(0, cookieString.toString().length() - 2);
    }

    static String getToken() {
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies())
            if (cookie.getName().toLowerCase().equals("xsrf-token"))
                return cookie.getValue();
        return null;
    }

    static CookieManager getCookieManager() {
        return cookieManager;
    }

    @Override
    protected Void call() {
        List<BeatmapXML> downloadList = getTableItems();
        if (downloadList.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(Localizer.getLocalizedText("noMapSelect"));
                alert.setContentText(Localizer.getLocalizedText("noMapSelectDesc"));
                alert.show();
                loginScreen.enableElements();
            });
            return null;
        }
        Authenticator authenticator = new Authenticator(mainScreen, loginScreen, email, password);
        if (!authenticator.loginProcess())
            return null;
        Global.INSTANCE.setEmail(email);
        Global.INSTANCE.setPassword(password);
        Platform.runLater(() -> {
            loginStage.close();
            mainScreen.disableButtons();
        });
        Global.INSTANCE.inProgress = true;
        Downloader downloader = new Downloader(mainScreen, downloadList);
        downloader.downloadBeatmap();
        Global.INSTANCE.inProgress = false;
        Platform.runLater(mainScreen::enableAllButtons);
        return null;
    }

    private List<BeatmapXML> getTableItems() {
        List<BeatmapXML> list = new LinkedList<>();
        for (BeatmapView beatmap : mainScreen.beatmapList.getItems()) {
            if (beatmap.getQueueProperty().get() && !beatmap.getInstalledProperty().get()) {
                BeatmapXML item = new BeatmapXML();
                item.setBeatmapID(beatmap.getBeatmapIdIntProperty().get());
                item.setTitle(beatmap.getTitle().get());
                item.setArtist(beatmap.getArtist().get());
                list.add(item);
            }
        }
        return list;
    }
}
