package org.controlsfx.samples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.CustomTextField;
import org.controlsfx.control.TextFields;

public class HelloCustomTextField extends Application implements Sample {
    
    private static final Image image = new Image("/org/controlsfx/samples/security-low.png");
    
    @Override public String getSampleName() {
        return "CustomTextField";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/CustomTextField.html";
    }
    
    @Override public boolean includeInSamples() {
        return true;
    }
    
    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        // normal TextField
        grid.add(new Label("Normal TextField: "), 0, 0);
        grid.add(new TextField(), 1, 0);
        
        // SearchField
        grid.add(new Label("SearchField: "), 0, 1);
        grid.add(TextFields.createSearchField(), 1, 1);
        
        // CustomTextField
        grid.add(new Label("CustomTextField (no additional nodes): "), 0, 2);
        grid.add(new CustomTextField(), 1, 2);
        
        // CustomTextField (w/ right node)
        grid.add(new Label("CustomTextField (w/ right node): "), 0, 3);
        CustomTextField customTextField1 = new CustomTextField();
        customTextField1.setRight(new ImageView(image));
        grid.add(customTextField1, 1, 3);
        
        // CustomTextField (w/ left node)
        grid.add(new Label("CustomTextField (w/ left node): "), 0, 4);
        CustomTextField customTextField2 = new CustomTextField();
        customTextField2.setLeft(new ImageView(image));
        grid.add(customTextField2, 1, 4);
        
        // CustomTextField (w/ left + right node)
        grid.add(new Label("CustomTextField (w/ left + right node): "), 0, 5);
        CustomTextField customTextField3 = new CustomTextField();
        ImageView imageView = new ImageView(image);
        customTextField3.setLeft(imageView);
        customTextField3.setRight(new ImageView(image));
        grid.add(customTextField3, 1, 5);
        
        return grid;
    }
    
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("CustomTextField Demo");
        
        
        Scene scene = new Scene((Parent) getPanel(stage), 550, 550);
//        scene.getStylesheets().addAll(SegmentedButton.class.getResource("segmentedbutton.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
