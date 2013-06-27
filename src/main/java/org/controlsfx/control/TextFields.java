package org.controlsfx.control;

import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class TextFields {
    private static final Duration FADE_DURATION = Duration.millis(350);
    
    private TextFields() {
        // no-op
    }

    public static TextField createSearchField() {
        final CustomTextField searchField = new CustomTextField();
        searchField.getStyleClass().add("search-field");
        
        Region clearButton = new Region();
        clearButton.getStyleClass().addAll("graphic");
        StackPane clearButtonPane = new StackPane(clearButton);
        clearButtonPane.getStyleClass().addAll("clear-button");
        clearButtonPane.setOpacity(0.0);
        clearButtonPane.setCursor(Cursor.DEFAULT);
        
        clearButtonPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                searchField.clear();
            }
        });
        
        searchField.setRight(clearButtonPane);
        
        final FadeTransition fader = new FadeTransition(FADE_DURATION, clearButtonPane);
        fader.setCycleCount(1);
        
        searchField.textProperty().addListener(new InvalidationListener() {
            private boolean clearButtonShowing = false;
            
            @Override public void invalidated(Observable arg0) {
                String text = searchField.getText();
                
                if ((text == null || text.isEmpty()) && clearButtonShowing) {
                    // hide clear button
                    fader.setFromValue(1.0);
                    fader.setToValue(0.0);
                    fader.play();
                    clearButtonShowing = false;
                } else if ((text != null && ! text.isEmpty()) && ! clearButtonShowing) {
                    // show clear button
                    fader.setFromValue(0.0);
                    fader.setToValue(1.0);
                    fader.play();
                    clearButtonShowing = true;
                }
            }
        });
        
        return searchField;
    }
}
