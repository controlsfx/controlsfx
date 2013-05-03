package org.controlsfx.samples;

import java.util.Arrays;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.tools.ButtonBar;
import org.controlsfx.tools.ButtonBar.ButtonType;

import static org.controlsfx.tools.ButtonBar.ButtonType.*;

public class HelloButtonBar extends Application {
    
     @Override public void start(Stage stage) throws Exception {
        stage.setTitle("ButtonBar Demo");
        
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        
        final ButtonBar buttonBar = new ButtonBar();
        
        
        // create toggle button to switch button orders 
        HBox buttonOrderHbox = new HBox(10);
        ToggleGroup group = new ToggleGroup();
        final ToggleButton windowsBtn = new ToggleButton("Windows");
        final ToggleButton macBtn = new ToggleButton("Mac OS");
        final ToggleButton linuxBtn = new ToggleButton("Linux");
        buttonOrderHbox.getChildren().setAll(windowsBtn, macBtn, linuxBtn);
        group.getToggles().setAll(windowsBtn, macBtn, linuxBtn);
        windowsBtn.setSelected(true);
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (windowsBtn.equals(newValue)) {
                    buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_WINDOWS);
                } else if (macBtn.equals(newValue)) {
                    buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_MAC_OS);
                } else if (linuxBtn.equals(newValue)) {
                    buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_LINUX);
                }
            }
        });
        root.setTop(buttonOrderHbox);
        
        // create button bar
        buttonBar.getButtons().addAll(Arrays.asList(
                createButton("OK", OK_DONE),
                createButton("Cancel", CANCEL_CLOSE),
                createButton("Left 1", LEFT),
                createButton("Left 2", LEFT),
                createButton("Left 3", LEFT),
                createButton("Right 1", RIGHT),
                createButton("Unknown 1", OTHER),
                createButton("Help(R)", HELP),
                createButton("Help(L)", HELP_2),
                createButton("Unknown 2", OTHER),
                createButton("Yes", YES),
                createButton("No", NO),
                createButton("Next", NEXT_FORWARD),
                createButton("Unknown 3", OTHER),
                createButton("Back", BACK_PREVIOUS),
                createButton("Right 2", RIGHT),
                createButton("Finish", FINISH),
                createButton("Right 3", RIGHT),
                createButton("Apply", APPLY)
                
        ));
        root.setBottom(buttonBar);
        
        Scene scene = new Scene(root, 1300, 300);
        scene.setFill(Color.WHITE);
        
        stage.setScene(scene);
        stage.show();
    }
     
    private Button createButton( String title, ButtonType type) {
        Button button = new Button(title);
        ButtonBar.setType(button, type);
        return button;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}