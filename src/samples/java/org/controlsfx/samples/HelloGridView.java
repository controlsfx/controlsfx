package org.controlsfx.samples;

import java.util.Random;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.Sample;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.cell.ColorGridCell;
import org.controlsfx.control.cell.ImageGridCell;

public class HelloGridView extends Application implements Sample {

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
    
    @Override public boolean includeInSamples() {
        return true;
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

    @Override public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("GridView");
        Scene scene = new Scene((Parent) getPanel(primaryStage), 540, 210);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    class ActionShowGrid extends AbstractAction {

        GridView<?> grid;
        
        public ActionShowGrid(String text, GridView<?> grid) {
            super(text);
            this.grid = grid;
        }

        @Override public void execute(ActionEvent ae) {
            if ( myGrid != null ) {
                root.getChildren().remove(myGrid);
            }
            myGrid = grid;
            root.getChildren().add(myGrid);
        }
        
    }
    
}