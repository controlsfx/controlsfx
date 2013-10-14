package org.controlsfx.samples;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.CheckComboBox;

public class HelloCheckComboBox extends Application implements Sample {
    
    @Override public String getSampleName() {
        return "CheckComboBox";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/CheckComboBox.html";
    }
    
    @Override public boolean includeInSamples() {
        return true;
    }
    
    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        int row = 0;
        
        final ObservableList<String> strings = FXCollections.observableArrayList();
        for (int i = 0; i <= 100; i++) {
            strings.add("Item " + i);
        }

        // normal ComboBox
        grid.add(new Label("Normal ComboBox: "), 0, row);
        grid.add(new ComboBox<String>(strings), 1, row++);
        
        // CheckBoxComboBox
        final CheckComboBox<String> cbcb = new CheckComboBox<String>(strings);
        cbcb.getSelectedIndices().addListener(new ListChangeListener<Integer>() {
            @Override public void onChanged(ListChangeListener.Change<? extends Integer> c) {
                System.out.println(cbcb.getSelectedIndices());
            }
        });
        cbcb.getSelectedItems().addListener(new ListChangeListener<String>() {
            @Override public void onChanged(ListChangeListener.Change<? extends String> c) {
                System.out.println(cbcb.getSelectedItems());
            }
        });
        grid.add(new Label("CheckComboBox: "), 0, row);
        grid.add(cbcb, 1, row++);
        
        return grid;
    }
    
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("CheckComboBox Demo");
        
        
        Scene scene = new Scene((Parent) getPanel(stage), 550, 550);
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
