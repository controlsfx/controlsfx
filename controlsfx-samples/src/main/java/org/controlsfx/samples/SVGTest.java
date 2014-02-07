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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;

public class SVGTest extends ControlsFXSample {
    
    private static final int IMAGE_HEIGHT = 175;
    private static final int IMAGE_SPACING = 0;
    
    private final String[] images = new String[] {
          "emblem-important.svg",
          "error.svg",
          "help-browser.svg"
  };

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "SVG Rendering";
    }
    
    @Override public String getJavaDocURL() {
        return null;
    }
    
    @Override public boolean isVisible() {
        return false;
    }
    
    @Override public Node getPanel(Stage stage) {
        VBox container = new VBox(IMAGE_SPACING);
        
//        container.setStyle("-fx-background-color: lightblue");
        
        for (String svg : images) {
            try {
                loadImage(svg, container);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return container;
    }

    @Override public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene((Parent)getPanel(primaryStage));
        primaryStage.setScene(scene);
        primaryStage.setWidth(200);
        primaryStage.setHeight((IMAGE_HEIGHT + IMAGE_SPACING) * (images.length) + IMAGE_HEIGHT / 2.0);

        primaryStage.show();
    }
    
    private void loadImage(final String filename, final VBox container) throws Exception {
//        final URL pathToFile = SVGTest.class.getResource(filename).toURI().toURL();
//
//        SVGLoader.loadSVGImage(pathToFile, IMAGE_HEIGHT, -1, new Callback<ImageView, Void>() {
//            @Override public Void call(ImageView image) {
//                container.getChildren().addAll(image);
//                return null;
//            }
//        });
    }
}
