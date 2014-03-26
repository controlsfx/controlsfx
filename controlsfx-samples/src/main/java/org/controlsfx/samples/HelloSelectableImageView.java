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

import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.animation.AnimationTimer;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.SelectableImageView;

/**
 * Demonstrates the {@link SelectableImageView}.
 */
public class HelloSelectableImageView extends ControlsFXSample {

    /* ************************************************************************
     *                                                                         *
     * Attributes & Properties                                                 *
     *                                                                         *
     **************************************************************************/

    // STATIC

    /**
     * The gap used between controls.
     */
    private static final double GAP = 5;

    /**
     * The format used to display all numbers in the text fields.
     */
    private static final DecimalFormat zeroDpFormat = new DecimalFormat("0");
    private static final DecimalFormat twoDpFormat = new DecimalFormat("0.00");

    /**
     * The names of the displayed images.
     */
    private final String[] nodeNames = new String[] {
        "ControlsFX Logo",
        "Java's Duke in 3D",
        "Rotating Node",
        "The Null Image",
    };
    
    /**
     * the displayed nodes.
     */
    private Node[] nodes;

    // INSTANCE

    /**
     * The demoed view.
     */
    private final SelectableImageView imageView = new SelectableImageView();

    /**
     * The label showing the name of the currently displayed image.
     */
    private final Label imageNameTestField = new Label();

    /**
     * The index in the array of images and image names.
     */
    private int imageIndex = 0;

    /* ************************************************************************
     *                                                                         *
     * Displayed Controls                                                      *
     *                                                                         *
     **************************************************************************/

    @Override
    public Node getPanel(Stage stage) {
        Rectangle rotatingRect = new Rectangle(200, 300, Color.GREEN);
        RotateTransition rotator = new RotateTransition(Duration.seconds(3), rotatingRect);
        rotator.setAutoReverse(true);
        rotator.setByAngle(360);
        rotator.setCycleCount(Integer.MAX_VALUE);
        rotator.play();
        
        nodes = new Node[] {
            new ImageView(new Image("http://cache.fxexperience.com/wp-content/uploads/2013/05/ControlsFX.png")),
            new ImageView(new Image("http://upload.wikimedia.org/wikipedia/commons/4/45/Duke3D.png")),
            rotatingRect,
            null,
        };
        
        displayImageAndNameforIndex(imageIndex);
        return imageView;
    }

    /**
     * Displays the image and its name at the specified index.
     * 
     * @param index
     *            the index used to access {@link #images} and {@link #imageNames}
     */
    private void displayImageAndNameforIndex(int index) {
        imageNameTestField.setText(nodeNames[index]);
        imageView.setNode(nodes[index]);
    }

    @Override
    public Node getControlPanel() {
        return new VBox(10, createSettingsControl(), createSelectionControl(), createSnapshot());
    }
    
    private ImageView snapshotImageView;
    
    /**
     * @return a control for all the image related properties
     */
    private Node createSettingsControl() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));
        
        int row = 0;
        
        // --- Node
        Label nodeTypeLabel = new Label("Node type: ");
        nodeTypeLabel.getStyleClass().add("property");
        grid.add(nodeTypeLabel, 0, row);
        final ChoiceBox<String> graphicOptions = new ChoiceBox<>(FXCollections.observableArrayList(nodeNames));
        graphicOptions.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(graphicOptions, Priority.ALWAYS);
        final SelectionModel<String> sm = graphicOptions.getSelectionModel();
        sm.selectedItemProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable o) {
                imageIndex = sm.getSelectedIndex();
                displayImageAndNameforIndex(imageIndex);
            }
        });
        sm.select(0);
        grid.add(graphicOptions, 1, row++);
        
        // --- fixed ratio
        Label fixedRatioLabel = new Label("Fixed selection ratio: ");
        fixedRatioLabel.getStyleClass().add("property");
        grid.add(fixedRatioLabel, 0, row);
        CheckBox ratioFixed = new CheckBox();
        ratioFixed.selectedProperty().bindBidirectional(imageView.selectionRatioFixedProperty());
        grid.add(ratioFixed, 1, row++);
        
        // --- ratio
        Label ratioLabel = new Label("Fixed ratio: ");
        ratioLabel.getStyleClass().add("property");
        grid.add(ratioLabel, 0, row);
        TextField ratioTextField = new TextField();
        ratioTextField.textProperty().bindBidirectional(imageView.fixedSelectionRatioProperty(), new StringConverter<Number>() {
            @Override public Number fromString(String value) {
                try {
                    return twoDpFormat.parse(value);
                } catch (ParseException e) {
                    return 1;
                }
            }

            @Override public String toString(Number value) {
                return twoDpFormat.format(value);
            }
        });
        grid.add(ratioTextField, 1, row++);
        
        return new TitledPane("Settings", grid);
    }

    private TextField upperLeftX;
    private TextField upperLeftY;
    private TextField lowerRightX;
    private TextField lowerRightY;
    private TextField width;
    private TextField height;
    private TextField ratio;
    
    /**
     * @return a control for all the selection related properties
     */
    private Node createSelectionControl() {
        // upper left
        upperLeftX = new TextField();
        upperLeftX.setPrefColumnCount(3);
        upperLeftX.setEditable(false);
        upperLeftY = new TextField();
        upperLeftY.setPrefColumnCount(3);
        upperLeftY.setEditable(false);

        // lower right
        lowerRightX = new TextField();
        lowerRightX.setPrefColumnCount(3);
        lowerRightX.setEditable(false);
        lowerRightY = new TextField();
        lowerRightY.setPrefColumnCount(3);
        lowerRightY.setEditable(false);

        // size
        width = new TextField();
        width.setPrefColumnCount(3);
        width.setEditable(false);
        height = new TextField();
        height.setPrefColumnCount(3);
        height.setEditable(false);
        ratio = new TextField();
        ratio.setPrefColumnCount(3);
        
        // set up the binding
        imageView.selectionProperty().addListener(new ChangeListener<Rectangle2D>() {
            @Override
            public void changed(
                    ObservableValue<? extends Rectangle2D> observable, Rectangle2D oldValue, Rectangle2D newValue) {
                if (newValue == null) {
                    upperLeftX.setText("");
                    upperLeftY.setText("");
                    lowerRightX.setText("");
                    lowerRightY.setText("");
                    width.setText("");
                    height.setText("");
                    ratio.setText("");
                } else {
                    upperLeftX.setText(zeroDpFormat.format(newValue.getMinX()));
                    upperLeftY.setText(zeroDpFormat.format(newValue.getMinY()));
                    lowerRightX.setText(zeroDpFormat.format(newValue.getMaxX()));
                    lowerRightY.setText(zeroDpFormat.format(newValue.getMaxY()));
                    width.setText(zeroDpFormat.format(newValue.getWidth()));
                    height.setText(zeroDpFormat.format(newValue.getHeight()));
                    ratio.setText(twoDpFormat.format(newValue.getWidth() / newValue.getHeight()));
                }
            }
        });
        
        // put it all together
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));
        
        int row = 0;

        grid.addRow(row++, new Label("Upper Left Corner:"), upperLeftX, new Label("/"), upperLeftY);
        grid.addRow(row++, new Label("Lower Right Corner:"), lowerRightX, new Label("/"), lowerRightY);
        grid.addRow(row++, new Label("Size (Ratio):"), width, new Label("x"), height, new Label(" ("), ratio, new Label(")"));

        CheckBox selectionChanging = new CheckBox();
        selectionChanging.setDisable(true);
        selectionChanging.selectedProperty().bindBidirectional(imageView.selectionChangingProperty());

        CheckBox selectionValid = new CheckBox();
        selectionValid.selectedProperty().bind(imageView.selectionValidProperty());
        selectionValid.setDisable(true);
        
        grid.addRow(row++, new Label("Selection Changing:"), selectionChanging);
        grid.addRow(row++, new Label("Selection Valid:"), selectionValid);

        return new TitledPane("Selection Stats", grid);
    }

    private Node createSnapshot() {
        AnimationTimer timer = new AnimationTimer() {
            @Override public void handle(long arg0) {
                if (imageView.getNode() != null || imageView.getSelection() == null) {
                    SnapshotParameters params = new SnapshotParameters();
                    params.setViewport(imageView.getSelection());
                    imageView.getNode().snapshot(new Callback<SnapshotResult, Void>() {
                        @Override public Void call(SnapshotResult result) {
                            snapshotImageView.setImage(result.getImage());
                            return null;
                        }
                    }, params, null);
                }
            }
        };
        timer.start();
        
        snapshotImageView = new ImageView();
        return new TitledPane("Snapshot", snapshotImageView);
    }


    /* ************************************************************************
     *                                                                         *
     * Boilerplate                                                             *
     *                                                                         *
     **************************************************************************/

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public String getSampleName() {
        return "SelectableImageView";
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE
                + "org/controlsfx/control/SelectableImageView.html";
    }

    @Override
    public String getSampleDescription() {
        return "An image view which allows the user to select a rectangular area of the displayed image. " +
                "The selection's ratio can be fixed so that the user can only make selections with that ratio.";
    }
}
