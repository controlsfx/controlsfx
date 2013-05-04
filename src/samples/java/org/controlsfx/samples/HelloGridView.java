package org.controlsfx.samples;

import java.util.Random;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.Sample;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.cell.ColorGridCell;

public class HelloGridView extends Application implements Sample {

    private GridView<Color> myGrid;
    
    public static void main(String[] args) {
        launch();
    }
    
    @Override public String getSampleName() {
        return "GridView";
    }
    
    @Override public Node getPanel(Stage stage) {
        final ObservableList<Color> list = FXCollections.<Color>observableArrayList();
        myGrid = new GridView<>(list);
        myGrid.setCellFactory(new Callback<GridView<Color>, GridCell<Color>>() {
            @Override public GridCell<Color> call(GridView<Color> arg0) {
                return new ColorGridCell();
            }
        });
        Random r = new Random(System.currentTimeMillis());
        for(int i = 0; i < 500; i++) {
            list.add(new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), 1.0));
        }

        final StackPane root = new StackPane(myGrid);
        return root;
    }

    @Override public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("GridView");
        Scene scene = new Scene((Parent) getPanel(primaryStage), 540, 210);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}