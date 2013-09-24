package org.controlsfx.samples;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.CheckBoxComboBox;
import org.controlsfx.control.CustomTextField;
import org.controlsfx.control.TextFields;

public class HelloCheckBoxComboBox extends Application implements Sample {
    
    @Override public String getSampleName() {
        return "CheckBoxComboBox";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/CheckBoxComboBox.html";
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
        CheckBoxComboBox<String> cbcb = new CheckBoxComboBox<String>(strings);
        grid.add(new Label("CheckBox ComboBox: "), 0, row);
        grid.add(cbcb, 1, row++);
        
        return grid;
    }
    
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("CheckBox ComboBox Demo");
        
        
        Scene scene = new Scene((Parent) getPanel(stage), 550, 550);
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
