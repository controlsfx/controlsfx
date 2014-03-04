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

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.HiddenSidesPane;

public class HelloHiddenSidesPane extends ControlsFXSample {

    @Override
    public String getSampleName() {
        return "Hidden Sides Pane";
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE
                + "org/controlsfx/control/HiddenSidesPane.html";
    }

    @Override
    public String getSampleDescription() {
        return "Hidden nodes will appear when moving the mouse cursor close to the edge of the content node. "
                + "They disappear again when the mouse cursor exits them. In this example a hidden node "
                + "can be pinned (and unpinned) by clicking on it so that it stays visible all the time.";
    }

    @Override
    public Node getPanel(Stage stage) {
        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-padding: 30");

        HiddenSidesPane pane = new HiddenSidesPane();

        Label content = new Label("Content Node");
        content.setStyle("-fx-background-color: white; -fx-border-color: black;");
        content.setAlignment(Pos.CENTER);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        pane.setContent(content);

        SideNode top = new SideNode("Top", Side.TOP, pane);
        top.setStyle("-fx-background-color: rgba(0,255,0,.25);");
        pane.setTop(top);

        SideNode right = new SideNode("Right", Side.RIGHT, pane);
        right.setStyle("-fx-background-color: rgba(0,0, 255,.25);");
        pane.setRight(right);

        SideNode bottom = new SideNode("Bottom", Side.BOTTOM, pane);
        bottom.setStyle("-fx-background-color: rgba(255,255,0,.25);");
        pane.setBottom(bottom);

        SideNode left = new SideNode("Left", Side.LEFT, pane);
        left.setStyle("-fx-background-color: rgba(255,0,0,.25);");
        pane.setLeft(left);

        stackPane.getChildren().add(pane);

        return stackPane;
    }

    class SideNode extends Label {

        public SideNode(final String text, final Side side,
                final HiddenSidesPane pane) {

            super(text + " (Click to pin / unpin)");

            setAlignment(Pos.CENTER);
            setPrefSize(200, 200);

            setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (pane.getPinnedSide() != null) {
                        setText(text + " (unpinned)");
                        pane.setPinnedSide(null);
                    } else {
                        setText(text + " (pinned)");
                        pane.setPinnedSide(side);
                    }
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
