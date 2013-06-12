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

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.DecorationPane;
import org.controlsfx.decoration.DecorationUtils;
import org.controlsfx.decoration.DefaultDecoration;

public class HelloDecorationPane extends Application implements Sample {
    
    @Override public String getSampleName() {
        return "Decorations";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/DecorationPane.html";
    }
    
    @Override public boolean includeInSamples() {
        return true;
    }
    
    @Override public Node getPanel(final Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setMaxHeight(Double.MAX_VALUE);
        
        final TextField field = new TextField();
		DecorationUtils.registerDecoration( field, 
				new DefaultDecoration(createDecoratorNode(Color.RED),Pos.TOP_LEFT));
		DecorationUtils.registerDecoration( field, 
				new DefaultDecoration(createDecoratorNode(Color.RED),Pos.TOP_CENTER));
		DecorationUtils.registerDecoration( field, 
				new DefaultDecoration(createDecoratorNode(Color.RED),Pos.TOP_RIGHT));
		DecorationUtils.registerDecoration( field, 
				new DefaultDecoration(createDecoratorNode(Color.GREEN),Pos.CENTER_LEFT));
		DecorationUtils.registerDecoration( field, 
				new DefaultDecoration(createDecoratorNode(Color.GREEN),Pos.CENTER));
		DecorationUtils.registerDecoration( field, 
				new DefaultDecoration(createDecoratorNode(Color.GREEN),Pos.CENTER_RIGHT));
		DecorationUtils.registerDecoration( field, 
				new DefaultDecoration(createDecoratorNode(Color.BLUE),Pos.BOTTOM_LEFT));
		DecorationUtils.registerDecoration( field, 
				new DefaultDecoration(createDecoratorNode(Color.BLUE),Pos.BOTTOM_CENTER));
		DecorationUtils.registerDecoration( field, 
				new DefaultDecoration(createDecoratorNode(Color.BLUE),Pos.BOTTOM_RIGHT));
        
        root.getChildren().add(field);
        
        return new DecorationPane(root);
    }
    
    private Node createDecoratorNode(Color color) {
    	Rectangle d = new Rectangle(10,10);
        d.setFill(color);
        return d;
    }
    
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("DecorationPane Demo");
        
        Scene scene = new Scene((Parent)getPanel(stage), 1300, 300);
        scene.setFill(Color.WHITE);
        
        stage.setScene(scene);
        stage.show();
    }
     
    public static void main(String[] args) {
        launch(args);
    }
    
}