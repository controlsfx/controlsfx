/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.samples;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.PlusMinusSlider;
import org.controlsfx.control.PlusMinusSlider.PlusMinusEvent;

public class HelloPlusMinusSlider extends ControlsFXSample {

    private PlusMinusSlider plusMinusSlider = new PlusMinusSlider();

    @Override
    public Node getPanel(Stage stage) {
        Group group = new Group();

        VBox vBox = new VBox();
        vBox.setMinWidth(500);
        vBox.setSpacing(20);
        vBox.setStyle("-fx-padding: 40;");

        group.getChildren().add(vBox);

        vBox.getChildren().add(plusMinusSlider);

        final Label counterLabel = new Label();
        vBox.getChildren().add(counterLabel);

        final Label valueLabel = new Label();
        vBox.getChildren().add(valueLabel);

        plusMinusSlider.setOnValueChanged(new EventHandler<PlusMinusEvent>() {
            long counter = 1;

            @Override
            public void handle(PlusMinusEvent event) {
                counterLabel.setText("Event #" + counter);
                valueLabel.setText("Value = " + event.getValue());
                counter++;
            }
        });

        return group;
    }

    @Override
    public Node getControlPanel() {
        ComboBox<Orientation> box = new ComboBox<>();
        box.getItems().addAll(Orientation.values());
        box.setValue(plusMinusSlider.getOrientation());
        plusMinusSlider.orientationProperty().bind(box.valueProperty());
        return box;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public String getSampleName() {
        return "PlusMinusSlider";
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE
                + "org/controlsfx/control/PlusMinusSlider.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/plusminusslider.css";
    }

    @Override
    public String getSampleDescription() {
        return "A slider-like control used to fire value events "
                + "with values in the range of -1 and +1. "
                + "The slider thumb jumps back to the zero "
                + "position when the user lets go of the mouse. "
                + "Possible use case: scrolling through a lot of data "
                + "at different speeds.";
    }
}
