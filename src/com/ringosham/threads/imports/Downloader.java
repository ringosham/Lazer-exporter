package com.ringosham.threads.imports;

import com.ringosham.controller.MainScreen;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;

public class Downloader extends Task<Void> {
    //Since the old website is being replaced, the new url will be used to authenticate.
    private static final String loginUrl = "https://osu.ppy.sh/session";
    private static final String downloadUrl = "https://osu.ppy.sh/d/";
    private final MainScreen mainScreen;
    private final Stage loginStage;
    private final String email;
    private final String password;

    public Downloader(MainScreen mainScreen, Stage loginStage, String email, String password) {
        this.mainScreen = mainScreen;
        this.loginStage = loginStage;
        this.email = email;
        this.password = password;
    }

    @Override
    protected Void call() {
        try {
            URL login = new URL(loginUrl);
            HttpsURLConnection loginConnection = (HttpsURLConnection) login.openConnection();
            loginConnection.setRequestMethod("POST");
            loginConnection.setInstanceFollowRedirects(false);

            //The request header.
            //A copy of Chrome on Windows 10 visiting the osu website
            loginConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
            loginConnection.setRequestProperty("Origin", "https://osu.ppy.sh");
            loginConnection.setRequestProperty("Referer", "https://osu.ppy.sh/home");
            loginConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            loginConnection.setRequestProperty("accept-encoding", "gzip, deflate, br");
            loginConnection.setRequestProperty("accept-language", "en-US,en;q=0.9,zh-TW;q=0.8,zh;q=0.7");
            loginConnection.setRequestProperty("accept", "*/*;q=0.5, text/javascript, application/javascript, application/ecmascript, application/x-ecmascript");
            loginConnection.setRequestProperty("DNT", "1");
            loginConnection.setRequestProperty("x-requested-with", "XMLHttpRequest");
            //TODO Set cookie
            CookieManager cookieManager = new CookieManager();
            cookieManager.getCookieStore();
            loginConnection.setRequestProperty("cookie", "");
        } catch (MalformedURLException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
