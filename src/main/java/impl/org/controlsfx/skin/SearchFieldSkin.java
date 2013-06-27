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
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.controlsfx.control.SearchField;

public class SearchFieldSkin extends CustomTextFieldSkin {
    
    private static final Duration FADE_DURATION = Duration.millis(350);
    private final FadeTransition fader;
    
    private final SearchField control;
    
    private boolean clearButtonShowing = false;
    private final StackPane clearButtonPane;
    private final Region clearButton;
    
    public SearchFieldSkin(final SearchField control) {
        super(control);
        
        this.control = control;
        
        clearButton = new Region();
        clearButton.getStyleClass().addAll("graphic");
        clearButtonPane = new StackPane(clearButton);
        clearButtonPane.getStyleClass().addAll("clear-button");
        clearButtonPane.setOpacity(0.0);
        clearButtonPane.setCursor(Cursor.DEFAULT);
        
        clearButtonPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                control.clear();
            }
        });
        
        control.setRight(clearButtonPane);
        
        fader = new FadeTransition(FADE_DURATION, clearButtonPane);
        fader.setCycleCount(1);
        
        control.textProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable arg0) {
                updateClearButton();
            }
        });
    }
    
    private void updateClearButton() {
        String text = control.getText();
        
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
}
