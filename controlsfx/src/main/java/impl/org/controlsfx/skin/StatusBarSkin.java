/**
 * Copyright (c) 2014, 2017 ControlsFX
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
package impl.org.controlsfx.skin;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import org.controlsfx.control.StatusBar;

public class StatusBarSkin extends SkinBase<StatusBar> {
    
    private HBox leftBox;
    private HBox rightBox;
    private Label label;
    private ProgressBar progressBar;

    public StatusBarSkin(StatusBar statusBar) {
        super(statusBar);
        final BooleanBinding notZeroProgressProperty = Bindings.notEqual(0, statusBar.progressProperty());

        GridPane gridPane = new GridPane();
        
        leftBox = new HBox();
        leftBox.getStyleClass().add("left-items"); //$NON-NLS-1$

        rightBox = new HBox();
        rightBox.getStyleClass().add("right-items"); //$NON-NLS-1$

        progressBar = new ProgressBar();
        progressBar.progressProperty().bind(statusBar.progressProperty());
        progressBar.visibleProperty().bind(notZeroProgressProperty);
        progressBar.managedProperty().bind(notZeroProgressProperty);

        label = new Label();
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.textProperty().bind(statusBar.textProperty());
        label.graphicProperty().bind(statusBar.graphicProperty());
        label.styleProperty().bind(getSkinnable().styleProperty());
        label.getStyleClass().add("status-label"); //$NON-NLS-1$

        leftBox.getChildren().setAll(getSkinnable().getLeftItems());
        rightBox.getChildren().setAll(getSkinnable().getRightItems());

        statusBar.getLeftItems().addListener(
                (Observable evt) -> leftBox.getChildren().setAll(
                        getSkinnable().getLeftItems()));

        statusBar.getRightItems().addListener(
                (Observable evt) -> rightBox.getChildren().setAll(
                        getSkinnable().getRightItems()));
        
        GridPane.setFillHeight(leftBox, true);
        GridPane.setFillHeight(rightBox, true);
        GridPane.setFillHeight(label, true);
        GridPane.setFillHeight(progressBar, true);

        GridPane.setVgrow(leftBox, Priority.ALWAYS);
        GridPane.setVgrow(rightBox, Priority.ALWAYS);
        GridPane.setVgrow(label, Priority.ALWAYS);
        GridPane.setVgrow(progressBar, Priority.ALWAYS);

        GridPane.setHgrow(label, Priority.ALWAYS);

        gridPane.add(leftBox, 0, 0);
        gridPane.add(label, 1, 0);
        gridPane.add(progressBar, 2, 0);
        gridPane.add(rightBox, 3, 0);

        getChildren().add(gridPane);
    }
}
