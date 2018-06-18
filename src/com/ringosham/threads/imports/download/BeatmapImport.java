package com.ringosham.threads.imports.download;

import com.ringosham.controller.MainScreen;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class BeatmapImport extends Task<Void> {
    //Since the old website is being replaced, the new url will be used to authenticate.
    //This means no more /d/, /forum/ucp.php?mode=login
    private static final String homepage = "https://osu.ppy.sh/home";
    private static final String downloadUrlPrefix = "https://osu.ppy.sh/beatmapsets/";
    private static final String downloadUrlSuffix = "/download";
    private final MainScreen mainScreen;
    private final Stage loginStage;
    private static CookieManager cookieManager = new CookieManager();
    private final String email;
    private final String password;
    private Button loginButton;

    public BeatmapImport(MainScreen mainScreen, Stage loginStage, Button loginButton, String email, String password) {
        this.mainScreen = mainScreen;
        this.loginStage = loginStage;
        this.loginButton = loginButton;
        this.email = email;
        this.password = password;
    }

    static void getCookies() throws IOException {
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
        Authenticator authenticator = new Authenticator(mainScreen, loginStage, loginButton, email, password);
        if (!authenticator.loginProcess())
            return null;

        return null;
    }
}
