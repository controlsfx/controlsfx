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

import java.util.Random;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.cell.ColorGridCell;
import org.controlsfx.control.cell.ImageGridCell;

public class HelloGridView extends ControlsFXSample {

    private GridView<?> myGrid;
    private final VBox root = new VBox();
    
    public static void main(String[] args) {
        launch();
    }
    
    @Override public String getSampleName() {
        return "GridView";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/GridView.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/gridview.css";
    }
    
    private GridView<?> getColorGrid() {
        final ObservableList<Color> list = FXCollections.<Color>observableArrayList();
        
        GridView<Color> colorGrid = new GridView<>(list);
        
        colorGrid.setCellFactory(new Callback<GridView<Color>, GridCell<Color>>() {
            @Override public GridCell<Color> call(GridView<Color> arg0) {
                return new ColorGridCell();
            }
        });
        Random r = new Random(System.currentTimeMillis());
        for(int i = 0; i < 500; i++) {
            list.add(new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), 1.0));
        }
        return colorGrid;
    }
    
    private GridView<?> getImageGrid( final boolean preserveImageProperties ) {
        
        final Image image = new Image("/org/controlsfx/samples/flowers.png", 200, 0, true, true);
        final ObservableList<Image> list = FXCollections.<Image>observableArrayList();
        
        GridView<Image> colorGrid = new GridView<>(list);
        
        colorGrid.setCellFactory(new Callback<GridView<Image>, GridCell<Image>>() {
            @Override public GridCell<Image> call(GridView<Image> arg0) {
                return new ImageGridCell(preserveImageProperties);
            }
        });
        for(int i = 0; i < 50; i++) {
            list.add(image);
        }
        return colorGrid;
    }    
    
    
    @Override public Node getPanel(Stage stage) {
        SegmentedButton selector = ActionUtils.createSegmentedButton(
            new ActionShowGrid("Colors", getColorGrid()),
            new ActionShowGrid("Images", getImageGrid(false)),
            new ActionShowGrid("Images (preserve properties)", getImageGrid(true))
        );
        root.getChildren().clear();
        root.getChildren().add(new ToolBar(selector));
        selector.getButtons().get(0).fire();
        return root;
    }
    
    class ActionShowGrid extends Action {

        GridView<?> grid;
        
        public ActionShowGrid(String text, GridView<?> grid) {
            super(text);
            this.grid = grid;
            setEventHandler(this::handleAction);
        }

        private void handleAction(ActionEvent ae) {
            if ( myGrid != null ) {
                root.getChildren().remove(myGrid);
            }
            myGrid = grid;
            root.getChildren().add(myGrid);
        }
        
    }
    
}