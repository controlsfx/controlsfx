/**
 * Copyright (c) 2013, 2020, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx;

import fxsampler.FXSamplerProject;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import fxsampler.model.WelcomePage;

public class ControlsFXSampler implements FXSamplerProject {

    /** {@inheritDoc} */
    @Override public String getProjectName() {
        return "ControlsFX";
    }

    /** {@inheritDoc} */
    @Override public String getSampleBasePackage() {
        return "org.controlsfx.samples";
    }

    /** {@inheritDoc} */
    @Override
    public String getModuleName() {
        return "org.controlsfx.controls";
    }

    /** {@inheritDoc} */
    @Override public WelcomePage getWelcomePage() {
        VBox vBox = new VBox();
        ImageView imgView = new ImageView();
        imgView.setStyle("-fx-image: url('org/controlsfx/samples/ControlsFX.png');");
        StackPane pane = new StackPane();
        pane.setPrefHeight(207);
        pane.setStyle("-fx-background-image: url('org/controlsfx/samples/bar.png');"
                + "-fx-background-repeat: repeat-x;");
        pane.getChildren().add(imgView);
        Label label = new Label();
        label.setWrapText(true);
        StringBuilder desc = new StringBuilder();
        desc.append("ControlsFX is an open source project for JavaFX that aims ");
        desc.append("to provide really high quality UI controls and other tools to ");
        desc.append("complement the core JavaFX distribution.");
        desc.append("\n\n");
        desc.append("Explore the available UI controls by clicking on the options to the left.");
        label.setText(desc.toString());
        label.setStyle("-fx-font-size: 1.5em; -fx-padding: 20 0 0 5;");
        vBox.getChildren().addAll(pane, label);
        return new WelcomePage("Welcome to Controls FX!", vBox);
    }
}
