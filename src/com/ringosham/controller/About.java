/*
 * Copyright (c) 2018. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.controller;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class About {
    private HostServices hostServices;

    @FXML
    private Hyperlink license;
    @FXML
    private Hyperlink github;

    public About(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void initialize() {
        license.setOnAction(e -> hostServices.showDocument("https://www.apache.org/licenses/LICENSE-2.0"));
        github.setOnAction(e -> hostServices.showDocument("https://github.com/ringosham/Lazer-exporter"));
    }
}
