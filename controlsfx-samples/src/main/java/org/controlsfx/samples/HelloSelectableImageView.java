package org.controlsfx.samples;

import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
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
     * The format used to display all numbers in thext fields.
     */
    private static final DecimalFormat format = new DecimalFormat("0.00");

    /**
     * The names of the displayed images.
     */
    private static final String[] imageNames = new String[] {
        "ControlsFX Logo",
        "Java's Duke in 3D",
        "The Null Image",
    };

    /**
     * the displayed images.
     */
    private static final Image[] images = new Image[] {
        new Image("http://cache.fxexperience.com/wp-content/uploads/2013/05/ControlsFX.png"),
        new Image("http://upload.wikimedia.org/wikipedia/commons/4/45/Duke3D.png"),
        null,
    };

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
        imageView.setPreserveImageRatio(true);
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
        imageNameTestField.setText(imageNames[index]);
        imageView.setImage(images[index]);
    }

    @Override
    public Node getControlPanel() {
        return new VBox(createImageControl(), createSelectionControl(), createRatioControl());
    }

    /**
     * @return a control for all the image related properties
     */
    private Node createImageControl() {
        Label imageNameLabel = new Label("Image Name: ");
        Font currentFont = imageNameTestField.getFont();
        imageNameTestField.setFont(Font.font(currentFont.getFamily(), FontPosture.ITALIC, currentFont.getSize()));
        GridPane imageNamePane = createPaneWithGapAndRow(GAP, imageNameLabel, imageNameTestField);

        CheckBox preserveImageRatio = new CheckBox("Preserve Image Ratio");
        preserveImageRatio.selectedProperty().bindBidirectional(imageView.preserveImageRatioProperty());

        GridPane imageControlPane = createPaneWithGapAndColumn(GAP, imageNamePane, preserveImageRatio, createButtons());
        return new TitledPane("Image", imageControlPane);
    }

    /**
     * @return buttons to show the previous and the next image
     */
    private Node createButtons() {
        Button previousImageButton = new Button("Previous Image");
        previousImageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                // (imageIndex - 1) % 3 produces negative for image index = 0; instead of going down, go up.
                imageIndex = (imageIndex + 2) % 3;
                displayImageAndNameforIndex(imageIndex);
            }
        });

        Button nextImageButton = new Button("Next Image");
        nextImageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                imageIndex = (imageIndex + 1) % 3;
                displayImageAndNameforIndex(imageIndex);
            }
        });

        return createPaneWithGapAndRow(GAP, previousImageButton, nextImageButton);
    }

    /**
     * @return a control for all the selection related properties
     */
    private Node createSelectionControl() {
        // upper left
        TextField upperLeftX = new TextField();
        upperLeftX.setPrefColumnCount(4);
        upperLeftX.setEditable(false);
        TextField upperLeftY = new TextField();
        upperLeftY.setPrefColumnCount(4);
        upperLeftY.setEditable(false);

        // lower right
        TextField lowerRightX = new TextField();
        lowerRightX.setPrefColumnCount(4);
        lowerRightX.setEditable(false);
        TextField lowerRightY = new TextField();
        lowerRightY.setPrefColumnCount(4);
        lowerRightY.setEditable(false);

        // size
        TextField width = new TextField();
        width.setPrefColumnCount(4);
        width.setEditable(false);
        TextField height = new TextField();
        height.setPrefColumnCount(4);
        height.setEditable(false);
        TextField ratio = new TextField();
        ratio.setPrefColumnCount(2);
        ratio.setEditable(false);

        bindTextFieldsToSelection(upperLeftX, upperLeftY, lowerRightX, lowerRightY, width, height, ratio);
        GridPane selectionCoordiantes = createPaneWithGap(GAP);
        selectionCoordiantes.addRow(0, new Label("Upper Left Corner:"), upperLeftX, new Label("/"), upperLeftY);
        selectionCoordiantes.addRow(1, new Label("Lower Right Corner:"), lowerRightX, new Label("/"), lowerRightY);
        selectionCoordiantes.addRow(2,
                new Label("Size (Ratio):"), width, new Label("x"), height, new Label(" ("), ratio, new Label(")"));

        CheckBox selectionValid = new CheckBox("Selection Valid");
        selectionValid.selectedProperty().bind(imageView.selectionValidProperty());
        selectionValid.setDisable(true);

        CheckBox selectionActive = new CheckBox("Selection Active");
        selectionActive.selectedProperty().bindBidirectional(imageView.selectionActiveProperty());

        CheckBox selectionManaged = new CheckBox("Selection Activity Explicitly Managed");
        selectionManaged.selectedProperty().bindBidirectional(imageView.selectionActivityExplicitlyManagedProperty());

        CheckBox selectionChanging = new CheckBox("Selection Changing");
        selectionChanging.selectedProperty().bindBidirectional(imageView.selectionChangingProperty());

        GridPane selectionPane = createPaneWithGapAndColumn(GAP, selectionCoordiantes, selectionValid, selectionActive, selectionManaged);
        return new TitledPane("Selection", selectionPane);
    }

    /**
     * Binds the text fields content to the current selection.
     */
    private void bindTextFieldsToSelection(
            final TextField upperLeftX, final TextField upperLeftY,
            final TextField lowerRightX, final TextField lowerRightY,
            final TextField width, final TextField height, final TextField ratio) {

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
                    upperLeftX.setText(format.format(newValue.getMinX()));
                    upperLeftY.setText(format.format(newValue.getMinY()));
                    lowerRightX.setText(format.format(newValue.getMaxX()));
                    lowerRightY.setText(format.format(newValue.getMaxY()));
                    width.setText(format.format(newValue.getWidth()));
                    height.setText(format.format(newValue.getHeight()));
                    ratio.setText(format.format(newValue.getWidth() / newValue.getHeight()));
                }
            }
        });
    }

    /**
     * @return a control for all the ratio related properties
     */
    private Node createRatioControl() {
        TextField ratioTextField = new TextField();
        ratioTextField.textProperty().bindBidirectional(imageView.fixedSelectionRatioProperty(), new StringConverter<Number>(){
            @Override
            public Number fromString(String value) {
                try {
                    return format.parse(value);
                } catch (ParseException e) {
                    return 1;
                }
            }
            @Override
            public String toString(Number value) {
                return format.format(value);
            }});
        GridPane ratio = createPaneWithGapAndRow(GAP, new Label("Fixed Ratio:"), ratioTextField);

        CheckBox ratioFixed = new CheckBox ("Ratio Fixed");
        ratioFixed.selectedProperty().bindBidirectional(imageView.selectionRatioFixedProperty());

        GridPane ratioPane = createPaneWithGapAndColumn(GAP, ratio, ratioFixed);
        return new TitledPane("Ratio", ratioPane);
    }

    /* ************************************************************************
     *                                                                         *
     * Utility                                                                 *
     *                                                                         *
     **************************************************************************/

    /**
     * @param gap the gap to set
     * @return a {@link GridPane} with the specified horizontal and vertical gap
     */
    private static GridPane createPaneWithGap(double gap) {
        GridPane pane = new GridPane();
        pane.setHgap(gap);
        pane.setVgap(gap);

        return pane;
    }

    /**
     * @return a {@link GridPane} with the specified horizontal and vertical gap
     */
    private static GridPane createPaneWithGapAndColumn(double gap, Node... nodes) {
        GridPane pane = createPaneWithGap(gap);
        pane.addColumn(0, nodes);

        return pane;
    }

    /**
     * @return a {@link GridPane} with the specified horizontal and vertical gap
     */
    private static GridPane createPaneWithGapAndRow(double gap, Node... nodes) {
        GridPane pane = createPaneWithGap(gap);
        pane.addRow(0, nodes);

        return pane;
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
