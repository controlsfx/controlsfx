/**
 * Copyright (c) 2013, 2016 ControlsFX
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

import java.util.Collections;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import org.controlsfx.control.SegmentedButton;

public class SegmentedButtonSkin extends SkinBase<SegmentedButton> {
        
    private static final String ONLY_BUTTON = "only-button"; //$NON-NLS-1$
    private static final String LEFT_PILL   = "left-pill"; //$NON-NLS-1$
    private static final String CENTER_PILL = "center-pill"; //$NON-NLS-1$
    private static final String RIGHT_PILL  = "right-pill"; //$NON-NLS-1$

    private final HBox container;

    public SegmentedButtonSkin(SegmentedButton control) {
        super(control);
        
        container = new HBox();
        
        getChildren().add(container);
        
        updateButtons();
        getButtons().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable observable) {
                updateButtons();
            }
        });
        
        control.toggleGroupProperty().addListener((observable, oldValue, newValue) -> {
            getButtons().forEach((button) -> {
                button.setToggleGroup(newValue);
            });
        });
    }
    
    private ObservableList<ToggleButton> getButtons() {
        return getSkinnable().getButtons();
    }
    
    private void updateButtons() {
        ObservableList<ToggleButton> buttons = getButtons();
        ToggleGroup group = getSkinnable().getToggleGroup();
        
        container.getChildren().clear();
        
        for (int i = 0; i < getButtons().size(); i++) {
            ToggleButton t = buttons.get(i);
            
            if (group != null) {
                t.setToggleGroup(group);
            }
            
            t.getStyleClass().removeAll(ONLY_BUTTON, LEFT_PILL, CENTER_PILL, RIGHT_PILL);
            container.getChildren().add(t);

            if (i == buttons.size() - 1) {
                if (i == 0) {
                    t.getStyleClass().add(ONLY_BUTTON);
                } else {
                    t.getStyleClass().add(RIGHT_PILL);
                }
            } else if (i == 0) {
                t.getStyleClass().add(LEFT_PILL);
            } else {
                t.getStyleClass().add(CENTER_PILL);
            }
        }
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefWidth(height);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefHeight(width);
    }
}
