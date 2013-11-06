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


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

import org.controlsfx.tools.Borders;
import org.controlsfx.tools.Borders.EmptyBorders;

import fxsampler.SampleBase;

public class BorderBug extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public Node getPanel(Stage stage) {
        Pane root = new Pane();
        
        Button button = new Button("Hello World!");
        
        StackPane wrappedButton = new StackPane(button);
        wrappedButton.setTranslateX(20);
        wrappedButton.setTranslateY(20);

        BorderStroke emptyBorder1 = new BorderStroke(
                Color.TRANSPARENT, 
                BorderStrokeStyle.NONE,
                null, 
                new BorderWidths(50),
                Insets.EMPTY);
        
        BorderStroke emptyBorder2 = new BorderStroke(
                Color.TRANSPARENT, 
                BorderStrokeStyle.SOLID,
                null, 
                new BorderWidths(80),
                Insets.EMPTY);
        
        BorderStroke innerBorder = new BorderStroke(
                Color.AQUA, 
                BorderStrokeStyle.SOLID,  
                new CornerRadii(0, false), 
                new BorderWidths(5),
                Insets.EMPTY);
        
        BorderStroke outerBorder = new BorderStroke(
                Color.BLUE, 
                BorderStrokeStyle.SOLID, 
                null, 
                new BorderWidths(1), 
                Insets.EMPTY);
        
        wrappedButton.setBorder(new Border(emptyBorder2, innerBorder, emptyBorder1, outerBorder));
        
        root.getChildren().add(wrappedButton);
        
        return root;
    }
    
    @Override public void start(Stage stage) {
        stage.setTitle("Borders Demo");

        Scene scene = new Scene((Parent)getPanel(stage), 520, 360);

        stage.setScene(scene);
        stage.show();
    }
}
