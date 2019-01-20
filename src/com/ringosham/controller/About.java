/*
 * Copyright (c) 2019. Ringo Sham.
 * Licensed under the Apache license. Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ringosham.controller;

import com.ringosham.Global;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class About {

    @FXML
    private Hyperlink license;
    @FXML
    private Hyperlink github;

    public void initialize() {
        license.setOnAction(e -> Global.INSTANCE.openLink("https://www.apache.org/licenses/LICENSE-2.0"));
        github.setOnAction(e -> Global.INSTANCE.openLink("https://github.com/ringosham/Lazer-exporter"));
    }
}
