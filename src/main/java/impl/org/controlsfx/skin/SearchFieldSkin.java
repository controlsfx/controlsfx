/**
 * Copyright (c) 2013, ControlsFX
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
package impl.org.controlsfx.skin;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.controlsfx.control.CustomTextField;
import org.controlsfx.control.SearchField;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class SearchFieldSkin extends BehaviorSkinBase<SearchField, BehaviorBase<SearchField>>{
    
    private static final Duration FADE_DURATION = Duration.millis(350);
    private final FadeTransition fader;
    
    private final CustomTextField customTextField;
    private final TextField textField;
    
    private boolean clearButtonShowing = false;
    private final StackPane clearButtonPane;
    private final Region clearButton;
    
    public SearchFieldSkin(final SearchField control) {
        super(control, new BehaviorBase<>(control));
        
        customTextField = control.getCustomTextField();
        textField = customTextField.getTextField();
        
        clearButton = new Region();
        clearButton.getStyleClass().addAll("graphic");
        clearButtonPane = new StackPane(clearButton);
        clearButtonPane.getStyleClass().addAll("clear-button");
        clearButtonPane.setOpacity(0.0);
        
        clearButtonPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                textField.clear();
            }
        });
        
        customTextField.setRight(clearButtonPane);
        
        fader = new FadeTransition(FADE_DURATION, clearButtonPane);
        fader.setCycleCount(1);
        
        textField.textProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable arg0) {
                updateClearButton();
            }
        });
        
        getChildren().setAll(customTextField);
        
        registerChangeListener(control.focusedProperty(), "FOCUSED");
    }
    
    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if (p == "FOCUSED") {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    textField.requestFocus();
                }
            });
        }
    }
    
    private void updateClearButton() {
        String text = textField.getText();
        
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
    
    @Override protected void layoutChildren(double x, double y, double w, double h) {
        customTextField.resizeRelocate(x, y, w, h);
    }
}
