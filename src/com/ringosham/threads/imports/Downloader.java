package com.ringosham.threads.imports;

import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Downloader extends Task<Void> {
    //Since the old website is being replaced, the new url will be used to authenticate.
    private static final String homepage = "https://osu.ppy.sh/home";
    private static final String loginUrl = "https://osu.ppy.sh/session";
    private static final String downloadUrl = "https://osu.ppy.sh/d/";
    private final MainScreen mainScreen;
    private final Stage loginStage;
    private static CookieManager cookieManager = new CookieManager();
    private final String email;
    private final String password;
    private Button loginButton;

    public Downloader(MainScreen mainScreen, Stage loginStage, Button loginButton, String email, String password) {
        this.mainScreen = mainScreen;
        this.loginStage = loginStage;
        this.loginButton = loginButton;
        this.email = email;
        this.password = password;
    }

    @Override
    protected Void call() {
        if (!loginProcess())
            return null;

        return null;
    }

    private boolean loginProcess() {
        /*
            Logging in requires 3 things in form data
           -CSRF token (obtained through cookies. XSRF-token = X-CSRF-token)
           -Email
           -Password

           The following in request header
            User-Agent - Spoof as any web browser and operating system
            Origin, Referer, accept-encoding, accept-language, accept, DNT, etc. - Standard parameters for web browsers
            cookie - Obviously the cookie
            content-type - The type of data being posted. In this case, plaintext with URL UTF-8 encoding.
            x-csrf-token - Token required for authentication
         */
        //Cookies are needed before authenticating
        if (cookieManager.getCookieStore().getCookies().isEmpty()) {
            try {
                getCookies();
            } catch (IOException e) {
                Platform.runLater(() -> {
                    mainScreen.consoleArea.appendText(Localizer.getLocalizedText("serverConnectionFail") + "\n");
                    mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(Localizer.getLocalizedText("serverConnectionFail"));
                    alert.setContentText(Localizer.getLocalizedText("serverConnectionFailDesc"));
                    alert.show();
                    loginButton.setDisable(false);
                });
                e.printStackTrace();
                return false;
            }
        }
        try {
            URL login = new URL(loginUrl);
            HttpsURLConnection loginConnection = (HttpsURLConnection) login.openConnection();
            loginConnection.setRequestMethod("POST");

            //The post data
            String encodeEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.displayName());
            String encodePassword = URLEncoder.encode(password, StandardCharsets.UTF_8.displayName());
            String postData = "_token=" + getToken() + "&username=" + encodeEmail + "&password=" + encodePassword;
            byte[] postBytes = postData.getBytes(StandardCharsets.UTF_8);
            //The request header.
            //A copy of Chrome on Windows 10 visiting the osu website
            loginConnection.setRequestProperty("Content-Length", String.valueOf(postData.length()));
            loginConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
            loginConnection.setRequestProperty("Origin", "https://osu.ppy.sh");
            loginConnection.setRequestProperty("Referer", "https://osu.ppy.sh/home");
            loginConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            loginConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            loginConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.9,zh-TW;q=0.8,zh;q=0.7");
            loginConnection.setRequestProperty("Accept", "*/*;q=0.5, text/javascript, application/javascript, application/ecmascript, application/x-ecmascript");
            loginConnection.setRequestProperty("DNT", "1");
            loginConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            loginConnection.setRequestProperty("Cookie", getCookieString());
            loginConnection.setRequestProperty("X-CSRF-Token", getToken());
            //Connection configurations
            loginConnection.setInstanceFollowRedirects(true);
            loginConnection.setDoOutput(true);
            loginConnection.setUseCaches(false);

            OutputStream stream = loginConnection.getOutputStream();
            //Write the post data to stream
            stream.write(postBytes);
            stream.close();

            int responseCode = loginConnection.getResponseCode();
            //Normally. A login failure would be 424.
            if (responseCode >= HttpsURLConnection.HTTP_BAD_REQUEST) {
                BufferedReader in = new BufferedReader(new InputStreamReader(loginConnection.getErrorStream()));
                StringBuilder errorString = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    errorString.append(line);
                }
                Pattern pattern = Pattern.compile("\\{\"error\":\"(.*)\"}");
                Matcher matcher = pattern.matcher(errorString.toString());
                String errorMessage;
                if (matcher.matches())
                    errorMessage = matcher.group(1);
                else
                    errorMessage = errorString.toString();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(Localizer.getLocalizedText("loginFail"));
                    alert.setHeaderText(Localizer.getLocalizedText("loginFailHead"));
                    alert.setContentText(errorMessage);
                    alert.show();
                    loginButton.setDisable(false);
                });
                return false;
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(loginConnection.getInputStream()));
                String responseString;
                while ((responseString = in.readLine()) != null) {
                    System.out.println(responseString);
                }
                Platform.runLater(loginStage::close);
            }
        } catch (IOException e) {
            Platform.runLater(() -> {
                mainScreen.consoleArea.appendText(Localizer.getLocalizedText("loginException") + "\n");
                mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(Localizer.getLocalizedText("loginException"));
                alert.setContentText(Localizer.getLocalizedText("loginExceptionDesc"));
                alert.show();
                loginButton.setDisable(false);
            });
            e.printStackTrace();
        }
        return true;
    }

    private void getCookies() throws IOException {
        URL url = new URL(homepage);
        URLConnection connection = url.openConnection();
        connection.connect();
        List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
        for (String cookie : cookies)
            cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
    }

    private String getCookieString() {
        cookieManager.getCookieStore();
        StringBuilder cookieString = new StringBuilder();
        for (HttpCookie httpCookie : cookieManager.getCookieStore().getCookies()) {
            cookieString.append(httpCookie.toString());
            cookieString.append("; ");
        }
        return cookieString.substring(0, cookieString.toString().length() - 2);
    }

    private String getToken() {
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (cookie.getName().toLowerCase().equals("xsrf-token"))
                return cookie.getValue();
        }
        return null;
    }
}
