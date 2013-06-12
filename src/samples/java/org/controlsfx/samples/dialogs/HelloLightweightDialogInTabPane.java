package org.controlsfx.samples.dialogs;

import java.util.Date;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.samples.Utils;

public class HelloLightweightDialogInTabPane extends Application implements Sample {

    @Override public String getSampleName() {
        return "Lightweight Dialogs";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/dialog/Dialogs.html";
    }
    
    @Override public boolean includeInSamples() {
        return true;
    }
    
    private Stage stage;
    
    
    @Override public Node getPanel(final Stage stage) {
        final Tab tab1 = new Tab("Tab 1");
        buildTab1(tab1);
        
        final Tab tab2 = new Tab("Tab 2");
        buildTab2(tab2);

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        tabPane.getTabs().addAll(tab1, tab2);
        tabPane.setPadding(new Insets(10));
        
        StackPane pane = new StackPane(tabPane);
        return pane;
    }
    
    private void buildTab1(final Tab tab1) {
        Button showDialogBtn = new Button("Show lightweight dialog");
        showDialogBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                Dialogs.create()
                    .lightweight()
                    .owner(tab1)
                    .title("Lightweight Dialog")
                    .message("This should only block Tab 1 - try going to Tab 2")
                    .showInformation();
            }
        });
        
        Button printToConsole = new Button("Print to console");
        printToConsole.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                System.out.println(new Date());
            }
        });
        
        VBox tab1Content = new VBox(10);
        tab1Content.setPadding(new Insets(10));
        tab1Content.getChildren().setAll(showDialogBtn, printToConsole);
        
        tab1.setContent(tab1Content);
    }
    
    private void buildTab2(final Tab tab2) {
        final ListView<String> listView = new ListView<>();
        listView.getItems().setAll("Jonathan", "Eugene", "Hendrik", "Danno", "Paru");
        GridPane.setHgrow(listView, Priority.ALWAYS);
        
        Button showDialogBtn = new Button("Show dialog in list");
        showDialogBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                Dialogs.create()
                    .lightweight()
                    .owner(listView)
                    .title("Lightweight Dialog")
                    .message("This should only block the listview")
                    .showInformation();
            }
        });
        
        Button printToConsole = new Button("Print to console");
        printToConsole.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                System.out.println(new Date());
            }
        });
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        grid.add(listView, 0, 0, 1, 3);
        grid.add(showDialogBtn, 1, 0);
        grid.add(printToConsole, 1, 1);
        
        tab2.setContent(grid);
    }

    @Override public void start(final Stage stage) {
        // setUserAgentStylesheet(STYLESHEET_MODENA);
        this.stage = stage;

        stage.setTitle("Lightweight Dialogs Sample");

        Scene scene = new Scene((Parent)getPanel(stage), 800, 400);
        scene.setFill(Color.WHITE);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
