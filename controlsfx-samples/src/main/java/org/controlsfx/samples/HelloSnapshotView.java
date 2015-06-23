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
import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.SnapshotView;
import org.controlsfx.control.SnapshotView.Boundary;

/**
 * Demonstrates the {@link SnapshotView}.
 */
@SuppressWarnings("nls")
public class HelloSnapshotView extends ControlsFXSample {

    /* ************************************************************************
     *                                                                         *
     * Attributes & Properties                                                 *
     *                                                                         *
     **************************************************************************/

    // STATIC

    /**
     * The format used to display all numbers in the text fields.
     */
    private static final DecimalFormat zeroDpFormat = new DecimalFormat("0");
    private static final DecimalFormat twoDpFormat = new DecimalFormat("0.00");

    // INSTANCE

    /**
     * The displayed nodes.
     */
    private final Node[] nodes;

    /**
     * The names of the displayed nodes.
     */
    private final String[] nodeNames = new String[] {
            "ImageView",
            "Fitted ImageView",
            "Transformed ImageView",
            "Rotating Node",
            "Null Node",
    };

    /**
     * The images displayed by the image views.
     */
    private final Image[] images;

    /**
     * The names of the displayed nodes.
     */
    private final String[] imageNames = new String[] {
            "ControlsFX",
            "Java's Duke",
            "Null Image",
    };

    private final IntegerProperty selectedImageIndex = new SimpleIntegerProperty();

    /**
     * The demonstrated view.
     */
    private final SnapshotView snapshotView = new SnapshotView();

    /* ************************************************************************
     *                                                                         *
     * Construction                                                            *
     *                                                                         *
     **************************************************************************/

    public HelloSnapshotView() {
        images = loadImages();
        nodes = createNodes();
    }

    /* ************************************************************************
     *                                                                         *
     * Displayed Controls                                                      *
     *                                                                         *
     **************************************************************************/

    @Override
    public Node getPanel(Stage stage) {
        snapshotView.setNode(nodes[0]);
        return snapshotView;
    }

    /**
     * Loads the displayed images.
     * 
     * @return an array of {@link Image image}s
     */
    private static Image[] loadImages() {
        Image controlsFX = new Image(HelloSnapshotView.class.getResource("ControlsFX.png").toExternalForm());
        Image duke = new Image(HelloSnapshotView.class.getResource("duke_wave.png").toExternalForm());
        return new Image[] { controlsFX, duke, null };
    }

    /**
     * Creates the nodes used by the snapshot view.
     * 
     * @return an array of {@link Node node}s
     */
    private Node[] createNodes() {
        // regular image view
        ImageView imageView = new ImageView(images[0]);
        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> increaseImageIndex());

        // fitted image view
        ImageView fittedImageView = new ImageView(images[0]);
        fittedImageView.setPreserveRatio(true);
        fittedImageView.fitWidthProperty().bind(snapshotView.widthProperty());
        fittedImageView.fitHeightProperty().bind(snapshotView.heightProperty());
        fittedImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> increaseImageIndex());

        // transformed image view
        ImageView transformedImageView = new ImageView(images[0]);
        transformedImageView.setScaleX(0.5);
        transformedImageView.setRotate(45);
        transformedImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> increaseImageIndex());

        // rotating rectangle
        Rectangle rotatingRect = new Rectangle(200, 300, Color.GREEN);
        rotatingRect.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Random r = new Random();
            Color newColor = new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble());
            rotatingRect.setFill(newColor);
        });
        RotateTransition rotator = new RotateTransition(Duration.seconds(3), rotatingRect);
        rotator.setAutoReverse(true);
        rotator.setByAngle(360);
        rotator.setCycleCount(Animation.INDEFINITE);
        rotator.play();

        return new Node[] {
                new Pane(imageView), new Pane(fittedImageView), new Pane(transformedImageView),
                new Pane(rotatingRect), null
        };
    }

    private void increaseImageIndex() {
        int currentImageIndex = selectedImageIndex.get();
        // set the next index but leave out the null image (which is assumed to be last in the array)
        int nextImageIndex = (currentImageIndex + 1) % (images.length - 1);
        selectedImageIndex.set(nextImageIndex);
    }

    @Override
    public Node getControlPanel() {
        return new VBox(10,
                createNodeControl(), createSettingsControl(),
                createVisualizationControl(), createSelectionControl(), createSnapshotImageView());
    }

    /**
     * @return a control for all the node related properties
     */
    private Node createNodeControl() {
        GridPane grid = new GridPane();
        grid.setVgap(5);
        grid.setHgap(5);
        grid.setPadding(new Insets(5));

        int row = 0;

        // --- node
        final Label nodeTypeLabel = new Label("Node type: ");
        nodeTypeLabel.getStyleClass().add("property");
        grid.add(nodeTypeLabel, 0, row);

        final ChoiceBox<String> nodeOptions = new ChoiceBox<>(FXCollections.observableArrayList(nodeNames));
        final SelectionModel<String> nodeSelectionModel = nodeOptions.getSelectionModel();
        nodeSelectionModel.selectedIndexProperty().addListener(
                (o, oldNodeIndex, newNodeIndex) -> snapshotView.setNode(nodes[newNodeIndex.intValue()]));
        nodeSelectionModel.select(0);

        nodeOptions.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(nodeOptions, Priority.ALWAYS);
        grid.add(nodeOptions, 1, row++);

        // --- image
        final Label imageLabel = new Label("Image: ");
        imageLabel.getStyleClass().add("property");
        grid.add(imageLabel, 0, row);

        final ChoiceBox<String> imageOptions = new ChoiceBox<>(FXCollections.observableArrayList(imageNames));
        // disable the box if no image view is shown
        imageOptions.disableProperty().bind(Bindings.equal(3, nodeSelectionModel.selectedIndexProperty()));
        // bind 'selectedImageIndex' and the box' selection model together
        final SelectionModel<String> imageSelectionModel = imageOptions.getSelectionModel();
        imageSelectionModel.selectedIndexProperty().addListener(
                (o, oldIndex, newIndex) -> selectedImageIndex.set(newIndex.intValue()));
        selectedImageIndex.addListener((o, oldIndex, newIndex) -> {
            imageSelectionModel.clearAndSelect(newIndex.intValue());
            setImageForAllViews(newIndex.intValue());
        });
        imageSelectionModel.select(0);

        imageOptions.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(imageOptions, Priority.ALWAYS);
        grid.add(imageOptions, 1, row++);

        return new TitledPane("Node", grid);
    }

    private void setImageForAllViews(int index) {
        Image image = images[index];
        for (int i = 0; i < 3; i++) {
            setImageForView(i, image);
        }
    }

    private void setImageForView(int imageViewIndex, Image image) {
        Pane containingPane = (Pane) nodes[imageViewIndex];
        ImageView view = (ImageView) containingPane.getChildren().get(0);
        view.setImage(image);
    }

    /**
     * @return a control for all the view related properties
     */
    private Node createSettingsControl() {
        GridPane grid = new GridPane();
        grid.setVgap(5);
        grid.setHgap(5);
        grid.setPadding(new Insets(5));

        int row = 0;

        // selection active
        CheckBox selectionActive = new CheckBox();
        selectionActive.selectedProperty().bindBidirectional(snapshotView.selectionActiveProperty());
        selectionActive.disableProperty().bind(snapshotView.selectionActivityManagedProperty());
        grid.addRow(row++, new Label("Active:"), selectionActive);

        // selection managed
        CheckBox selectionActivityManaged = new CheckBox();
        selectionActivityManaged.selectedProperty().bindBidirectional(snapshotView.selectionActivityManagedProperty());
        grid.addRow(row++, new Label("Activity Managed:"), selectionActivityManaged);

        // selection mouse transparent
        CheckBox selectionMouseTransparent = new CheckBox();
        selectionMouseTransparent.selectedProperty().bindBidirectional(
                snapshotView.selectionMouseTransparentProperty());
        grid.addRow(row++, new Label("Mouse Transparent:"), selectionMouseTransparent);

        // --- fixed ratio
        Label fixedRatioLabel = new Label("Fixed selection ratio: ");
        fixedRatioLabel.getStyleClass().add("property");
        grid.add(fixedRatioLabel, 0, row);
        CheckBox ratioFixed = new CheckBox();
        ratioFixed.selectedProperty().bindBidirectional(snapshotView.selectionRatioFixedProperty());
        grid.add(ratioFixed, 1, row++);

        // --- ratio
        Label ratioLabel = new Label("Fixed ratio: ");
        ratioLabel.getStyleClass().add("property");
        grid.add(ratioLabel, 0, row);
        TextField ratioTextField = new TextField();
        ratioTextField.textProperty().bindBidirectional(snapshotView.fixedSelectionRatioProperty(),
                new StringConverter<Number>() {
                    @Override
                    public Number fromString(String value) {
                        try {
                            return twoDpFormat.parse(value);
                        } catch (ParseException e) {
                            return 1;
                        }
                    }

                    @Override
                    public String toString(Number value) {
                        return twoDpFormat.format(value);
                    }
                });
        grid.add(ratioTextField, 1, row++);

        // --- selection area boundary
        final Label selectionBoundaryLabel = new Label("Selection Area Boundary: ");
        selectionBoundaryLabel.getStyleClass().add("property");
        grid.add(selectionBoundaryLabel, 0, row);

        final ChoiceBox<Boundary> selectionBoundaryOptions = new ChoiceBox<>(
                FXCollections.observableArrayList(Boundary.CONTROL, Boundary.NODE));
        selectionBoundaryOptions.getSelectionModel().select(Boundary.CONTROL);
        snapshotView.selectionAreaBoundaryProperty().bind(
                selectionBoundaryOptions.getSelectionModel().selectedItemProperty());

        selectionBoundaryOptions.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(selectionBoundaryOptions, Priority.ALWAYS);
        grid.add(selectionBoundaryOptions, 1, row++);

        // --- unselected area boundary
        final Label unselectedBoundaryLabel = new Label("Unselected Area Boundary: ");
        unselectedBoundaryLabel.getStyleClass().add("property");
        grid.add(unselectedBoundaryLabel, 0, row);

        final ChoiceBox<Boundary> unselectedBoundaryOptions = new ChoiceBox<>(
                FXCollections.observableArrayList(Boundary.CONTROL, Boundary.NODE));
        unselectedBoundaryOptions.getSelectionModel().select(Boundary.CONTROL);
        snapshotView.unselectedAreaBoundaryProperty().bind(
                unselectedBoundaryOptions.getSelectionModel().selectedItemProperty());

        unselectedBoundaryOptions.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(unselectedBoundaryOptions, Priority.ALWAYS);
        grid.add(unselectedBoundaryOptions, 1, row++);

        return new TitledPane("Selection Settings", grid);
    }

    /**
     * @return a control for all the visualization related properties
     */
    private Node createVisualizationControl() {
        GridPane grid = new GridPane();
        grid.setVgap(5);
        grid.setHgap(5);
        grid.setPadding(new Insets(5));

        int row = 0;

        // selection fill color
        ColorPicker selectionFillPicker = new ColorPicker((Color) snapshotView.getSelectionAreaFill());
        snapshotView.selectionAreaFillProperty().bind(selectionFillPicker.valueProperty());
        grid.addRow(row++, new Label("Fill Color:"), selectionFillPicker);

        // selection border color
        ColorPicker selectionBorderPaintPicker = new ColorPicker((Color) snapshotView.getSelectionBorderPaint());
        snapshotView.selectionBorderPaintProperty().bind(selectionBorderPaintPicker.valueProperty());
        grid.addRow(row++, new Label("Stroke Color:"), selectionBorderPaintPicker);

        // selection border width
        Slider selectionStrokeWidth = new Slider(0, 25, snapshotView.getSelectionBorderWidth());
        snapshotView.selectionBorderWidthProperty().bindBidirectional(selectionStrokeWidth.valueProperty());
        grid.addRow(row++, new Label("Stroke Width:"), selectionStrokeWidth);

        // unselected area fill color
        ColorPicker unselectedAreaFillPicker = new ColorPicker((Color) snapshotView.getUnselectedAreaFill());
        snapshotView.unselectedAreaFillProperty().bind(unselectedAreaFillPicker.valueProperty());
        grid.addRow(row++, new Label("Outer Color:"), unselectedAreaFillPicker);

        return new TitledPane("Visualization Settings", grid);
    }

    /**
     * @return a control for all the selection related properties
     */
    private Node createSelectionControl() {
        // upper left
        TextField upperLeftX = new TextField();
        upperLeftX.setPrefColumnCount(3);
        upperLeftX.setEditable(false);
        TextField upperLeftY = new TextField();
        upperLeftY.setPrefColumnCount(3);
        upperLeftY.setEditable(false);

        // lower right
        TextField lowerRightX = new TextField();
        lowerRightX.setPrefColumnCount(3);
        lowerRightX.setEditable(false);
        TextField lowerRightY = new TextField();
        lowerRightY.setPrefColumnCount(3);
        lowerRightY.setEditable(false);

        // size
        TextField width = new TextField();
        width.setPrefColumnCount(3);
        width.setEditable(false);
        TextField height = new TextField();
        height.setPrefColumnCount(3);
        height.setEditable(false);
        TextField ratio = new TextField();
        ratio.setPrefColumnCount(3);

        // set up the binding
        snapshotView.selectionProperty().addListener(new ChangeListener<Rectangle2D>() {
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
        grid.setVgap(5);
        grid.setHgap(5);
        grid.setPadding(new Insets(5));

        int row = 0;

        grid.addRow(row++, new Label("Upper Left Corner:"), upperLeftX, new Label("/"), upperLeftY);
        grid.addRow(row++, new Label("Lower Right Corner:"), lowerRightX, new Label("/"), lowerRightY);
        grid.addRow(row++, new Label("Size (Ratio):"), width, new Label("x"), height, new Label(" ("), ratio,
                new Label(")"));

        // selection changing
        CheckBox selectionChanging = new CheckBox();
        selectionChanging.setDisable(true);
        selectionChanging.selectedProperty().bind(snapshotView.selectionChangingProperty());
        grid.addRow(row++, new Label("Selection Changing:"), selectionChanging);

        return new TitledPane("Selection Stats", grid);
    }

    /**
     * @return a control which displays the current snapshot
     */
    private Node createSnapshotImageView() {
        final ImageView snapshotImageView = new ImageView();

        // display snapshots which are constantly taken
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long timestamp) {
                Image snapshot = null;
                if (snapshotView.getNode() != null && snapshotView.hasSelection()) {
                    snapshot = snapshotView.createSnapshot();
                }
                snapshotImageView.setImage(snapshot);
            }
        };
        timer.start();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(snapshotImageView);
        return new TitledPane("Snapshot", scrollPane);
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
        return "SnapshotView";
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE
                + "org/controlsfx/control/SnapshotView.html";
    }

    @Override
    public String getControlStylesheetURL() {
        return "/org/controlsfx/control/snapshot-view.css";
    }

    @Override
    public String getSampleDescription() {
        return "A control which allows the user to select a rectangular area of the displayed node. " +
                "The selection's ratio can be fixed so that the user can only make selections with that ratio. " +
                "The method 'createSnapshot()' returns an Image of the selected area. " +
                "The displayed node can be interacted with if the selection is set to be mouse transparent. ";
    }
}
