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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import org.controlsfx.control.SegmentedButton;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class SegmentedButtonSkin extends BehaviorSkinBase<SegmentedButton, BehaviorBase<SegmentedButton>>{

    private final ToggleGroup group;
    
    private final HBox container;

    /**
     * 
     */
    public SegmentedButtonSkin(SegmentedButton control) {
        super(control, new BehaviorBase<>(control));
        
        group = new ToggleGroup();
        container = new HBox();
        
        getChildren().add(container);
        
        updateButtons();
        getButtons().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable observable) {
                updateButtons();
            }
        });
    }
    
    private ObservableList<ToggleButton> getButtons() {
        return getSkinnable().getButtons();
    }
    
    private void updateButtons() {
        ObservableList<ToggleButton> buttons = getButtons();
        
        for (int i = 0; i < getButtons().size(); i++) {
            ToggleButton t = buttons.get(i);
            t.setToggleGroup(group);
            container.getChildren().add(t);

            if (i == buttons.size() - 1) {
                if(i == 0) {
                    t.getStyleClass().add("only-button");
                } else {
                    t.getStyleClass().add("right-pill");
                }
            } else if (i == 0) {
                t.getStyleClass().add("left-pill");
            } else {
                t.getStyleClass().add("center-pill");
            }
        }
    }
}
