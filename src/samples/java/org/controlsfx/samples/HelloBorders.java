/**
 * Copyright (c) 2013, ControlsFX
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

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.tools.Borders;

import fxsampler.SampleBase;

public class HelloBorders extends SampleBase {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "Borders";
    }
    
    @Override public String getJavaDocURL() {
        return null; //Utils.JAVADOC_BASE + "org/controlsfx/tools/Borders.html";
    }
    
    @Override public boolean isVisible() {
        return true;
    }
    
    @Override public Node getPanel(Stage stage) {
        Pane root = new Pane();
        
        Button button = new Button("Hello World!");
        Node wrappedButton = Borders.wrap(button).emptyBorder().padding(100).build()
                                                 .lineBorder().color(Color.RED).thickness(1, 1, 1, 5).build()
                                                 .emptyBorder().padding(50).build()
                                                 .build();
        root.getChildren().add(wrappedButton);
        
        return root;
    }
    
    @Override public String getSampleDescription() {
        return null;
    }
    
    @Override public void start(Stage stage) {
        stage.setTitle("Borders Demo");

        Scene scene = new Scene((Parent)getPanel(stage), 520, 360);

        stage.setScene(scene);
        stage.show();
    }
}
