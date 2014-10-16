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

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.ListSelectionView;

public class HelloListSelectionView extends ControlsFXSample {

    @Override
    public String getSampleName() {
        return "List Selection View";
    }

    @Override
    public Node getPanel(Stage stage) {
        ListSelectionView<String> view = new ListSelectionView<>();
        view.getSourceItems()
                .addAll("Katja", "Dirk", "Philip", "Jule", "Armin");

        GridPane pane = new GridPane();
        pane.add(view, 0, 0);
        pane.setAlignment(Pos.CENTER);

        return pane;
    }

    @Override
    public String getSampleDescription() {
        return "A control used to let the user select multiple values from a "
                + "list of available values. Selected values are moved into a "
                + "second list that is showing the current selection. Items can "
                + "be moved by double clicking on them or by first selecting "
                + "them and then pressing one of the buttons in the center.";
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE
                + "org/controlsfx/control/ListSelectionView.html";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
