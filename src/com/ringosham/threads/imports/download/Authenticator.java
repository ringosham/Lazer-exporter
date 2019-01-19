/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.threads.imports.download;

import com.ringosham.Global;
import com.ringosham.controller.Login;
import com.ringosham.controller.MainScreen;
import com.ringosham.locale.Localizer;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Authenticator {
    private static final String loginUrl = "https://osu.ppy.sh/session";

    private final MainScreen mainScreen;
    private final String email;
    private final String password;
    private final Login loginScreen;

    Authenticator(MainScreen mainScreen, Login loginScreen, String email, String password) {
        this.mainScreen = mainScreen;
        this.loginScreen = loginScreen;
        this.email = email;
        this.password = password;
    }

    boolean loginProcess() {
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
        if (BeatmapImport.getCookieManager().getCookieStore().getCookies().isEmpty()) {
            try {
                BeatmapImport.getUnauthCookies();
            } catch (IOException e) {
                Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("login.error.serverConnectionFail"),
                        Localizer.getLocalizedText("login.error.serverConnectionFailDesc"));
                Platform.runLater(() -> {
                    mainScreen.consoleArea.appendText(Localizer.getLocalizedText("login.error.serverConnectionFail") + "\n");
                    mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
                    loginScreen.enableElements();
                });
                e.printStackTrace();
                return false;
            }
        }
        try {
            URL login = new URL(loginUrl);
            HttpsURLConnection connection = (HttpsURLConnection) login.openConnection();
            connection.setRequestMethod("POST");

            //The post data
            String encodeEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.displayName());
            String encodePassword = URLEncoder.encode(password, StandardCharsets.UTF_8.displayName());
            String postData = "_token=" + BeatmapImport.getToken() + "&username=" + encodeEmail + "&password=" + encodePassword;
            byte[] postBytes = postData.getBytes(StandardCharsets.UTF_8);
            //The request header.
            //A copy of Chrome on Windows 10 visiting the osu website
            connection.setRequestProperty("Content-Length", String.valueOf(postData.length()));
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
            connection.setRequestProperty("Origin", "https://osu.ppy.sh");
            connection.setRequestProperty("Referer", "https://osu.ppy.sh/home");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9,zh-TW;q=0.8,zh;q=0.7");
            connection.setRequestProperty("Accept", "*/*;q=0.5, text/javascript, application/javascript, application/ecmascript, application/x-ecmascript");
            connection.setRequestProperty("DNT", "1");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            connection.setRequestProperty("Cookie", BeatmapImport.getCookieString());
            connection.setRequestProperty("X-CSRF-Token", BeatmapImport.getToken());
            //Connection configurations
            connection.setInstanceFollowRedirects(false);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            OutputStream stream = connection.getOutputStream();
            //Write the post data to stream
            stream.write(postBytes);
            stream.close();

            int responseCode = connection.getResponseCode();
            //Normally. A login failure would be 424.
            if (responseCode >= HttpsURLConnection.HTTP_BAD_REQUEST) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder errorString = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    errorString.append(line);
                }
                //"WHAT ARE YOU DOING HERE PARSING JSON WITH REGEX!?"
                //Yeah I know. This looks horrifying, but importing a JSON library just for the authentication message is just not worth it.
                Pattern pattern = Pattern.compile("\\{\"error\":\"(.*)\"}");
                Matcher matcher = pattern.matcher(errorString.toString());
                String errorMessage;
                if (matcher.matches())
                    errorMessage = matcher.group(1);
                else
                    errorMessage = errorString.toString();
                Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("login.error.title"),
                        Localizer.getLocalizedText("dialog.login.loginFailHead"), errorMessage);
                Platform.runLater(loginScreen::enableElements);
                Global.INSTANCE.clearLoginDetails();
                return false;
            } else {
                //Login success. Get the new token and get out of here.
                List<String> newCookies = connection.getHeaderFields().get("Set-Cookie");
                BeatmapImport.getCookieManager().getCookieStore().removeAll();
                for (String cookie : newCookies)
                    BeatmapImport.getCookieManager().getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
            }
        } catch (IOException e) {
            Global.INSTANCE.showAlert(Alert.AlertType.ERROR, Localizer.getLocalizedText("dialog.login.loginException"),
                    Localizer.getLocalizedText("dialog.login.loginExceptionDesc"));
            Platform.runLater(() -> {
                mainScreen.consoleArea.appendText(Localizer.getLocalizedText("dialog.login.loginException") + "\n");
                mainScreen.consoleArea.appendText(e.getClass().getName() + " : " + e.getMessage() + "\n");
                loginScreen.enableElements();
            });
            e.printStackTrace();
        }
        return true;
    }
}
